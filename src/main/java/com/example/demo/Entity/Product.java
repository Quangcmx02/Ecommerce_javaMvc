package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "size")
    private String size;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_link")
    private String imageLink;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "price")
    private Float price;

    @Column(name = "status")
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
