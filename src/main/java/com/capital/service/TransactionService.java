package com.capital.service;

import java.util.Map;

import com.capital.exception.TradingException;

public interface TransactionService {
	Map<String, Object> purchase(Long userId, Long merchantId, String sku, Integer quantity) throws TradingException;
}
