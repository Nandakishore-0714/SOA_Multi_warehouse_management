package com.omnistock.inventory;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stock",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"product_id", "warehouse_code"}
    )
)
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(nullable = false)
    private int quantity;

    protected Stock() {}

    public Stock(Long productId, String warehouseCode, int quantity) {
        this.productId = productId;
        this.warehouseCode = warehouseCode;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getWarehouseCode() { return warehouseCode; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}