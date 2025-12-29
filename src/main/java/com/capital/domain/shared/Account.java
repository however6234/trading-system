package com.capital.domain.shared;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.capital.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class Account {
	@Id
	@GeneratedValue(generator = "local-id-generator")
    @GenericGenerator(
        name = "local-id-generator",
        strategy = "com.capital.util.LocalIdIdentifierGenerator"
    )
	private Long id;

	@Column(name = "balance",nullable = false)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "currency",nullable = false)
	private String currency = "CNY";
	
	@Column(name = "account_type",nullable = false)
	private String accountType;
	
	@Column(name = "daily_sales")
	private BigDecimal dailySales = BigDecimal.ZERO;
	
	@Column(name = "is_active")
	private boolean active = true;
	
	public void recharge(BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Recharge amount must be positive");
		}
		this.balance = this.balance.add(amount);
	}

	public boolean deduct(BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Deduct amount must be positive");
		}
		if (this.balance.compareTo(amount) >= 0) {
			this.balance = this.balance.subtract(amount);
			return true;
		}
		return false;
	}
	public void addBalance(BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Balance amount must be positive");
		}
        this.balance = this.balance.add(amount);
        this.dailySales = this.dailySales.add(amount);
    }
	public void resetDailySales() {
        this.dailySales = BigDecimal.ZERO;
    }
}