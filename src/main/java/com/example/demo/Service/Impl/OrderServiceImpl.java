package com.example.demo.Service.Impl;

import com.example.demo.Entity.*;
import com.example.demo.Repository.OrderRepository;
import com.example.demo.Service.CartService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public Order placeOrder(Long userId, String address) {
        loggerService.logInfo("Placing order for userId: " + userId + ", address: " + address);


        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể thanh toán");
        }


        Order order = new Order();
        order.setUser(cart.getUser());
        order.setAddress(address);
        order.setStatus(Status.PENDING);
        order.setTotalAmount(0.0);
        List<OrderDetail> orderDetails = new ArrayList<>();

        // Khóa từng sản phẩm để kiểm tra và cập nhật tồn kho
        for (CartItem cartItem : cart.getCartItems()) {
            Long productId = cartItem.getProduct().getProductId();
            RLock lock = redissonClient.getLock("product:lock:" + productId);

            try {
                // Thử khóa trong 10 giây
                if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Không thể khóa sản phẩm " + productId + ", vui lòng thử lại");
                }

                // Lấy Product và kiểm tra tồn kho
                Product product = productService.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại: " + productId));
                int requestedQuantity = cartItem.getQuantity();

                if (requestedQuantity > product.getQuantity()) {
                    throw new IllegalStateException("Số lượng tồn kho không đủ cho sản phẩm " + product.getName() + ": Còn lại " + product.getQuantity());
                }

                // Trừ tồn kho
                product.setQuantity(product.getQuantity() - requestedQuantity);
                productService.save(product);

                // Tạo OrderDetail
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setProduct(product);
                orderDetail.setQuantity(requestedQuantity);
                orderDetail.setPrice(cartItem.getPrice() / requestedQuantity); // Giá đơn vị
                orderDetail.setAmount(cartItem.getPrice()); // Tổng giá
                orderDetails.add(orderDetail);

                // Cập nhật totalAmount
                order.setTotalAmount(order.getTotalAmount() + orderDetail.getAmount());

                loggerService.logInfo("Processed productId: " + productId + ", quantity: " + requestedQuantity + " for order");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Lỗi khi khóa sản phẩm " + productId, e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        // Gán OrderDetail và lưu Order
        order.setOrderDetails(orderDetails);
        Order savedOrder = orderRepository.save(order);

        // Xóa Cart
        cartService.clearCart(cart.getCartId());
        loggerService.logInfo("Order placed successfully for userId: " + userId + ", orderId: " + savedOrder.getOrderId());

        return savedOrder;
    }
    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        loggerService.logInfo("Fetching orders for userId: " + userId);
        return orderRepository.findByUserUserId(userId, pageable);
    }

    @Override
    public Order getOrderById(Long orderId) {
        loggerService.logInfo("Fetching order with orderId: " + orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Đơn hàng không tồn tại: " + orderId));
    }
    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        loggerService.logInfo("Cancelling orderId: " + orderId + " for userId: " + userId);

        // Lấy Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Đơn hàng không tồn tại: " + orderId));

        // Kiểm tra quyền
        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền hủy đơn hàng này");
        }

        // Kiểm tra trạng thái
        if (order.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái Đang chờ xử lý");
        }

        // Khôi phục số lượng sản phẩm
        for (OrderDetail detail : order.getOrderDetails()) {
            Long productId = detail.getProduct().getProductId();
            RLock lock = redissonClient.getLock("product:lock:" + productId);

            try {
                // Thử khóa trong 10 giây
                if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Không thể khóa sản phẩm " + productId + ", vui lòng thử lại");
                }

                // Lấy Product
                Product product = productService.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại: " + productId));

                // Khôi phục số lượng
                product.setQuantity(product.getQuantity() + detail.getQuantity());
                productService.save(product);

                loggerService.logInfo("Restored quantity: " + detail.getQuantity() + " for productId: " + productId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Lỗi khi khóa sản phẩm " + productId, e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        // Cập nhật trạng thái
        order.setStatus(Status.CANCELLED);
        orderRepository.save(order);

        loggerService.logInfo("OrderId: " + orderId + " cancelled successfully for userId: " + userId);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        loggerService.logInfo("Fetching all orders");
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Status newStatus) {
        loggerService.logInfo("Updating status for orderId: " + orderId + " to " + newStatus);

        // Lấy Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Đơn hàng không tồn tại: " + orderId));

        // Kiểm tra trạng thái hợp lệ
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException("Không thể chuyển từ " + order.getStatus().getDisplayName() + " sang " + newStatus.getDisplayName());
        }

        // Cập nhật trạng thái
        order.setStatus(newStatus);
        orderRepository.save(order);

        loggerService.logInfo("OrderId: " + orderId + " status updated to " + newStatus);
    }

    private boolean isValidStatusTransition(Status currentStatus, Status newStatus) {
        if (currentStatus == Status.CANCELLED || newStatus == Status.CANCELLED) {
            return false;
        }
        switch (currentStatus) {
            case PENDING:
                return newStatus == Status.PAID || newStatus == Status.DELIVERING || newStatus == Status.SHIPPED;
            case PAID:
                return newStatus == Status.DELIVERING || newStatus == Status.SHIPPED;
            case DELIVERING:
                return newStatus == Status.SHIPPED;
            case SHIPPED:
                return false;
            default:
                return false;
        }
    }
    @Override
    public long count() {
        try {
            return orderRepository.count();
        } catch (Exception e) {
            loggerService.logError("Lỗi khi đếm tổng số đơn hàng", e);
            return 0;
        }
    }
}
