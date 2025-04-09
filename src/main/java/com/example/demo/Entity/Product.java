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
    @Column(name = "ProductId", nullable = false)
    private Long ProductId;

    private String Name;

    private int Quantity;

    private String Size;

    private String Description;

    private String ImageLink;

    private Float Price;
    private Boolean Status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}