package com.capital.domain.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.capital.domain.product.Product;

import lombok.Data;

@Data
public class AddProductRequest {
	@NotBlank
    private String sku;
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
    
    @NotNull
    @Min(0)
    private Integer stockQuantity;
    
	public Product convert() {
		Product product = new Product();
		BeanUtils.copyProperties(this, product);
		return product;
	}
}
