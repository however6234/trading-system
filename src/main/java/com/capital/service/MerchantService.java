package com.capital.service;

import java.util.List;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.product.Product;
import com.capital.exception.TradingException;

public interface MerchantService {
	
	Product addProduct(Product product) throws TradingException ;
	
	List<Product> findAllProducts(Long merchantId) throws TradingException ;
	
	Product getProductBySku(String sku,Long merchantId) throws TradingException ;

	Product increaseStock(Long merchantId, String sku, Integer quantity) throws TradingException ;
	
	Merchant createMerchant(String name, String code) throws TradingException ;
}
