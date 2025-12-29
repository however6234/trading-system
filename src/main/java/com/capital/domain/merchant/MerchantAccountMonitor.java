package com.capital.domain.merchant;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchant_account_monitor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MerchantAccountMonitor {

	@Id
	@Column(name = "merchant_id")
	private Long merchantId;

	@Column(name = "account_id", unique = true)
	private Long accountId;
	@Column(name = "balance", nullable = false)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "currency", nullable = false)
	private String currency = "CNY";
	
	public void resetDailyBalance(BigDecimal preBalance) {
		this.balance = preBalance;
	}
}