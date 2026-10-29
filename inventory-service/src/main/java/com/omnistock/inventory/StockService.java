
package com.omnistock.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final RestClient productClient;

    public StockService(
            StockRepository stockRepository,
            RestClient.Builder restClientBuilder) {

        this.stockRepository = stockRepository;
        this.productClient = restClientBuilder
                .baseUrl("http://localhost:9002")
                .build();
    }

    @Transactional
    public Stock reserve(
            Long productId,
            String warehouseCode,
            int quantity) {

        if (productId == null || productId <= 0
                || warehouseCode == null || warehouseCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid product or warehouse");
        }

        if (quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1");
        }

        validateProduct(productId);

        Stock stock = stockRepository
                .findForUpdate(productId, warehouseCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Stock record not found"));

        if (stock.getQuantity() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Insufficient stock");
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        return stockRepository.save(stock);
    }

   private void validateProduct(Long productId) {
    try {
        productClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .toBodilessEntity();

    } catch (RestClientResponseException exception) {
        if (exception.getStatusCode().value() == 404) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found");
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Product Service request failed");

    } catch (ResourceAccessException exception) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product Service is unavailable");
    }
}
}
