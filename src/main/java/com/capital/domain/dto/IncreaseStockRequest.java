package com.capital.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class IncreaseStockRequest {
    @NotBlank
    private String sku;
    
    @NotNull
    @Min(1)
    private Integer quantity;
}
