package com.omnistock.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/inventory")
public class StockController {
    private final StockRepository stockRepository;
    private final StockService stockService;

    public StockController(
            StockRepository stockRepository,
            StockService stockService) {
        this.stockRepository = stockRepository;
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> list() {
        return stockRepository.findAll();
    }

    @GetMapping("/low-stock")
    public List<Stock> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        if (threshold < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Threshold cannot be negative");
        }

        return stockRepository.findByQuantityLessThanEqual(threshold);
    }

    @GetMapping("/{productId}/{warehouseCode}")
    public Stock get(
            @PathVariable Long productId,
            @PathVariable String warehouseCode) {
        return stockRepository
                .findByProductIdAndWarehouseCode(productId, warehouseCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Stock record not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Stock receive(@RequestBody Stock stock) {
        if (stock.getQuantity() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        return stockRepository
                .findByProductIdAndWarehouseCode(
                        stock.getProductId(), stock.getWarehouseCode())
                .map(existing -> {
                    existing.setQuantity(
                            existing.getQuantity() + stock.getQuantity());
                    return stockRepository.save(existing);
                })
                .orElseGet(() -> stockRepository.save(stock));
    }

    @PostMapping("/{productId}/{warehouseCode}/reserve")
    public Stock reserve(
            @PathVariable Long productId,
            @PathVariable String warehouseCode,
            @RequestParam int quantity) {
        return stockService.reserve(productId, warehouseCode, quantity);
    }
}