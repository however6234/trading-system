package com.capital.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capital.domain.dto.PurchaseRequest;
import com.capital.domain.dto.Result;
import com.capital.exception.StatusCode;
import com.capital.exception.TradingException;
import com.capital.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingController {
	@Autowired
	private TransactionService transactionService;

	@PostMapping("/purchase")
	@Operation(summary = "purchase product", description = "user purchase product")
	public Result purchase(@Valid @RequestBody PurchaseRequest request) {
		try {
			Map<String, Object> result = transactionService.purchase(request.getUserId(), request.getMerchantId(),
					request.getSku(), request.getQuantity());
			return Result.success(StatusCode.SUCCESS, result);
		} catch (TradingException e) {
			return Result.failure(e.toStatusCode());
		}
	}
}
