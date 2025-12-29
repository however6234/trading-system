package com.capital.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.product.Product;
import com.capital.domain.shared.Account;
import com.capital.enums.AccountType;
import com.capital.exception.StatusCode;
import com.capital.exception.TradingException;
import com.capital.repository.AccountRepository;
import com.capital.repository.MerchantRepository;
import com.capital.service.MerchantService;
import com.capital.util.LocalIdGenerator;

@Service
public class MerchantServiceImpl implements MerchantService {
	
	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private LocalIdGenerator localIdGenerator;

	@Override
	public Product addProduct(Product product) {
		Merchant merchant = merchantRepository.findById(product.getMerchantId()).orElse(null);
		if (merchant == null) {
			throw StatusCode.MERCHANT_NOT_FOUND.toException();
		}
		Product pt = merchant.findProductBySku(product.getSku());

		if (pt != null) {
			throw StatusCode.PRODUCT_SKU_EXISTS.toException();
		}
		product.setId(localIdGenerator.nextId("product"));
		merchant.addProduct(product);
		merchantRepository.save(merchant);
		return product;
	}

	@Override
	public Product increaseStock(Long merchantId, String sku, Integer quantity) {
		Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
		if (merchant == null) {
			throw StatusCode.MERCHANT_NOT_FOUND.toException();
		}
		Product product = merchant.findProductBySku(sku);
		if (product != null) {
			product.increaseStock(quantity);
			merchantRepository.save(merchant);
		}
		return product;
	}

	@Override
	public Merchant createMerchant(String name, String code) {
		if (merchantRepository.existsByCode(code)) {
			throw StatusCode.MERCHANT_CODE_ALREADY_EXISTS.toException();
        }
		if (merchantRepository.existsByName(name)) {
			throw StatusCode.MERCHANT_NAME_ALREADY_EXISTS.toException();
        }
		Account account = new Account();
		account.setCurrency("CNY");
		account.setAccountType(AccountType.MERCHANT.toString());
		account.setBalance(BigDecimal.ZERO);
		account.setDailySales(BigDecimal.ZERO);
		account.setActive(true);
		accountRepository.save(account);
		
		Merchant merchant = new Merchant();
        merchant.setName(name);
        merchant.setCode(code);
        merchant.setAccountId(account.getId());
        merchant.setAccount(account);
        merchantRepository.save(merchant);
		return merchant;
	}

	@Override
	public List<Product> findAllProducts(Long merchantId) throws TradingException {
		Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
		if (merchant == null) {
			throw StatusCode.MERCHANT_NOT_FOUND.toException();
		}
		return merchant.getProducts();
	}

	@Override
	public Product getProductBySku(String sku,Long merchantId) throws TradingException {
		Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
		if (merchant == null) {
			throw StatusCode.MERCHANT_NOT_FOUND.toException();
		}
		Product product = merchant.findProductBySku(sku);
        if (product == null) {
        	throw StatusCode.PRODUCT_NOT_FOUND.toException();
        }
        
        return product;
	}

}
