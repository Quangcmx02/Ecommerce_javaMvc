package com.example.demo.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.CartItem;

import java.util.Optional;

public interface CartService {
    Cart getCartByUserId(Long userId);

    CartItem updateCartItemQuantity(Long cartItemId, int quantity);

    void removeCartItem(Long cartItemId);
    CartItem addToCart(Long userId, Long productId, int quantity);
    void clearCart(Long cartId);
}