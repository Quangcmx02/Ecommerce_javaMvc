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

        // Lấy Cart
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể thanh toán");
        }

        // Tạo Order
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
}
