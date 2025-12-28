package com.capital.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.shared.SettlementWarn;
import com.capital.repository.MerchantRepository;
import com.capital.repository.SettlementWarnRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SettlementScheduler {
	private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private SettlementWarnRepository settlementWarnRepository;

//	@Scheduled(cron = "0 0 0 * * ?")
	@Scheduled(fixedRate = 6000) 
	@Transactional
	public void dailySettlement() {
		log.info("Starting daily settlement...");

		List<Merchant> merchants = merchantRepository.findAll();

		for (Merchant merchant : merchants) {
			BigDecimal dailySales = merchant.getAccount().getDailySales();
			BigDecimal balance = merchant.getAccount().getBalance();

			if (balance.compareTo(dailySales) != 0) {
				SettlementWarn settlementWarn = new SettlementWarn();
				settlementWarn.setBalance(balance);
				settlementWarn.setDailySales(dailySales);
				settlementWarn.setMerchantId(merchant.getId());
				settlementWarn.setCreatedAt(LocalDateTime.now());
				settlementWarnRepository.save(settlementWarn);
				log.error("Mismatch detected for merchant {}: balance={}, dailySales={}", merchant.getId(), balance,
						dailySales);
			}

			merchant.resetDailySales();
		}

		log.info("Daily settlement completed.");
	}

}
