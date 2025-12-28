package com.capital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capital.domain.shared.SettlementWarn;

@Repository
public interface SettlementWarnRepository  extends JpaRepository<SettlementWarn, Long>{

}
