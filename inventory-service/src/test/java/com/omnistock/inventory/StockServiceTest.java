
package com.omnistock.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.server.ResponseStatusException;

class StockServiceTest {

    private StockRepository stockRepository;
    private RestClient.Builder restClientBuilder;
    private RestClient productClient;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        restClientBuilder = mock(RestClient.Builder.class);
        productClient = mock(RestClient.class);

        when(restClientBuilder.baseUrl(anyString()))
                .thenReturn(restClientBuilder);
        when(restClientBuilder.build())
                .thenReturn(productClient);

        stockService = new StockService(
                stockRepository, restClientBuilder);
    }

    @Test
    void reserve_whenQuantityIsZero_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stockService.reserve(1L, "HYD-01", 0));

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode());

        verifyNoInteractions(stockRepository);
    }

    @Test
    void reserve_whenStockIsInsufficient_throwsConflict() {
        Stock stock = new Stock(1L, "HYD-01", 10);

        mockProductExists();

        when(stockRepository.findForUpdate(1L, "HYD-01"))
                .thenReturn(Optional.of(stock));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stockService.reserve(1L, "HYD-01", 20));

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode());

        assertEquals(10, stock.getQuantity());

        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void reserve_whenStockIsAvailable_deductsAndSavesStock() {
        Stock stock = new Stock(1L, "HYD-01", 50);

        mockProductExists();

        when(stockRepository.findForUpdate(1L, "HYD-01"))
                .thenReturn(Optional.of(stock));

        when(stockRepository.save(stock)).thenReturn(stock);

        Stock result = stockService.reserve(1L, "HYD-01", 5);

        assertEquals(45, result.getQuantity());
        assertEquals(45, stock.getQuantity());

        verify(stockRepository).save(stock);
    }

    @Test
    void reserve_whenStockRecordIsMissing_throwsNotFound() {
        mockProductExists();

        when(stockRepository.findForUpdate(1L, "HYD-01"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stockService.reserve(1L, "HYD-01", 5));

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(stockRepository, never()).save(any(Stock.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockProductExists() {
        RequestHeadersUriSpec requestSpec =
                mock(RequestHeadersUriSpec.class);

        ResponseSpec responseSpec =
                mock(ResponseSpec.class);

        doReturn(requestSpec).when(productClient).get();

        doReturn(requestSpec)
                .when(requestSpec)
                .uri("/api/products/{id}", 1L);

        doReturn(responseSpec).when(requestSpec).retrieve();

        doReturn(ResponseEntity.ok().build())
                .when(responseSpec).toBodilessEntity();
    }
}
