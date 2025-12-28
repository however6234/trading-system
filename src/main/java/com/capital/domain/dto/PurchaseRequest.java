package com.capital.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurchaseRequest {
	@NotNull
	private Long userId;

	@NotNull
	private Long merchantId;

	@NotBlank
	private String sku;

	@NotNull
	@Min(value = 1, message = "The minimum purchase quantity must not lower than 1")
	private Integer quantity;
}