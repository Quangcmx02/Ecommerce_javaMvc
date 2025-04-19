package com.example.demo.Service;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Order placeOrder(Long userId, String address);
    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);
    Order getOrderById(Long orderId);
    void cancelOrder(Long orderId, Long userId);
    Page<Order> getAllOrders(Pageable pageable);
    void updateOrderStatus(Long orderId, Status newStatus);
    long count();
}
