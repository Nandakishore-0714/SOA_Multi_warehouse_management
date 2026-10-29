package com.omnistock.order;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final RestClient inventoryClient;

    public OrderController(
            OrderRepository orderRepository,
            RestClient.Builder restClientBuilder) {
        this.orderRepository = orderRepository;
        this.inventoryClient = restClientBuilder
                .baseUrl("http://localhost:8082")
                .build();
    }

    @GetMapping
    public List<CustomerOrder> list() {
        return orderRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> place(
            @Valid @RequestBody OrderRequest request) {
        try {
            inventoryClient.post()
                    .uri(
                            "/inventory/{productId}/{warehouseCode}/reserve?quantity={quantity}",
                            request.productId(),
                            request.warehouseCode(),
                            request.quantity())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            HttpStatusCode status =
                    HttpStatusCode.valueOf(exception.getStatusCode().value());

            throw new ResponseStatusException(
                    status, "Inventory reservation failed");
        }

        CustomerOrder order = new CustomerOrder(
                request.productId(),
                request.warehouseCode(),
                request.quantity());
        order.setStatus("CONFIRMED");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderRepository.save(order));
    }
}