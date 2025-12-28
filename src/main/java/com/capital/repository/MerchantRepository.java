package com.capital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capital.domain.merchant.Merchant;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
	
	boolean existsByCode(String code);
	
	boolean existsByName(String name);
}
