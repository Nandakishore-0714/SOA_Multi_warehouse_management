package com.omnistock.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull Long productId,
        @NotBlank String warehouseCode,
        @Min(1) int quantity) {
}	