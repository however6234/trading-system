package com.capital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capital.domain.merchant.MerchantAccountMonitor;

@Repository
public interface MerchantAccountMonitorRepository  extends JpaRepository<MerchantAccountMonitor, Long> {

}
