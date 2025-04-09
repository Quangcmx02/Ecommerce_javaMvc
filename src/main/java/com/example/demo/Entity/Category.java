package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CategoryId", nullable = false)
    private Long CategoryId;

    private String Name;
    private String Description;
    private String ImageLink;
    private Boolean Status;
}