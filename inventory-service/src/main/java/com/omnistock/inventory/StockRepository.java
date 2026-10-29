package com.omnistock.inventory;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductIdAndWarehouseCode(
            Long productId, String warehouseCode);

    List<Stock> findByQuantityLessThanEqual(int quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from Stock s
        where s.productId = :productId
          and s.warehouseCode = :warehouseCode
        """)
    Optional<Stock> findForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseCode") String warehouseCode);
}