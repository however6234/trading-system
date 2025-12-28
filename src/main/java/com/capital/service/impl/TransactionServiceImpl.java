package com.capital.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.product.Product;
import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.exception.StatusCode;
import com.capital.repository.AccountRepository;
import com.capital.repository.MerchantRepository;
import com.capital.repository.UserRepository;
import com.capital.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private MerchantRepository merchantRepository;

	@Override
	public Map<String, Object> purchase(Long userId, Long merchantId, String sku, Integer quantity) {

		User user = userRepository.findByIdAndActive(userId, true);
		if (user == null) {
			throw StatusCode.USER_NOT_FOUND.toException();
		}
		Account account = user.getAccount();
		if (account == null) {
			throw StatusCode.ACCOUNT_NOT_FOUND.toException();
		}
		Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
		if (merchant == null) {
			throw StatusCode.MERCHANT_NOT_FOUND.toException();
		}
		Product product = merchant.findProductBySku(sku);
		if (product == null) {
			throw StatusCode.PRODUCT_NOT_FOUND.toException();
		}

		if (!product.isAvailable(quantity)) {
			throw StatusCode.INSUFFICIENT_STOCK.toException();
		}
		BigDecimal totalCost = product.calculateTotalPrice(quantity);

		if (!account.deduct(totalCost)) {
			throw StatusCode.INSUFFICIENT_BALANCE.toException();
		}

		if (!product.reduceStock(quantity)) {
			throw StatusCode.FAILED_TO_REDUCE_STOCK.toException();
		}

		merchant.addBalance(totalCost);

		accountRepository.save(account);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("totalCost", totalCost);
		result.put("quantity", quantity);
		result.put("product", product);
		return result;
	}

}
