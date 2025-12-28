package com.capital.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capital.domain.dto.AddProductRequest;
import com.capital.domain.dto.CreateMerchantRequest;
import com.capital.domain.dto.IncreaseStockRequest;
import com.capital.domain.dto.Result;
import com.capital.domain.merchant.Merchant;
import com.capital.domain.product.Product;
import com.capital.exception.StatusCode;
import com.capital.exception.TradingException;
import com.capital.service.MerchantService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

	@Autowired
	private MerchantService merchantService;

	@PostMapping("/{merchantId}/products/add")
	public Result addProduct(@PathVariable Long merchantId, @Valid @RequestBody AddProductRequest request) {

		Product product = request.convert();
		product.setMerchantId(merchantId);

		try {
			merchantService.addProduct(product);
			return Result.success(StatusCode.SUCCESS, product);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}

	@PostMapping("/{merchantId}/products/increaseStock")
	public Result increaseStock(@PathVariable Long merchantId, @Valid @RequestBody IncreaseStockRequest request) {
		try {
			Product product = merchantService.increaseStock(merchantId, request.getSku(), request.getQuantity());
			return Result.success(StatusCode.SUCCESS, product);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}

	@PostMapping("/createMerchant")
	@Operation(summary = "create merchant", description = "create a new merchant")
	public Result createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
		try {
			Merchant merchant = merchantService.createMerchant(request.getName(), request.getCode());
			return Result.success(StatusCode.SUCCESS, merchant);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}

	@GetMapping("/merchant/findAllProducts/{merchantId}")
	public Result findAllProducts(@PathVariable Long merchantId) {
		try {
			List<Product> products = merchantService.findAllProducts(merchantId);
			return Result.success(StatusCode.SUCCESS, products);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}

	@GetMapping("/{sku}/merchant/{merchantId}")
	public Result getProductBySku(@PathVariable String sku, @PathVariable Long merchantId) {
		try {
			Product product = merchantService.getProductBySku(sku, merchantId);
			return Result.success(StatusCode.SUCCESS, product);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}
}