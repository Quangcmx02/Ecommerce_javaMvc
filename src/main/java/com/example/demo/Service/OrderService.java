package com.example.demo.Service;

import com.example.demo.Entity.Order;

public interface OrderService {
    Order placeOrder(Long userId, String address);
}
