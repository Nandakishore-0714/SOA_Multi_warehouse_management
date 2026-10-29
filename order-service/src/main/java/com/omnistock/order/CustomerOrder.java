package com.omnistock.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String warehouseCode;
    private int quantity;
    private String status = "CREATED";

    protected CustomerOrder() {
    }

    public CustomerOrder(Long productId, String warehouseCode, int quantity) {
        this.productId = productId;
        this.warehouseCode = warehouseCode;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}