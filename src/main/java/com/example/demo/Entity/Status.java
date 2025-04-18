package com.example.demo.Entity;

public enum Status {
    PENDING("Đang chờ xử lý"),
    PAID("Đã thanh toán"),
    DELIVERING("Đang giao hàng"),
    SHIPPED("Đã giao hàng"),
    CANCELLED("Đã hủy");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}