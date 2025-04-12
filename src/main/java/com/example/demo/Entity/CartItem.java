package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CartItem_Id", nullable = false)
    private Long cartItem_Id;
    // 1 cart có nhiueu item
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    // Mỗi CartItem gắn với 1 Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
    private double price;

}