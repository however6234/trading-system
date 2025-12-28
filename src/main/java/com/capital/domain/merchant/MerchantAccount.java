package com.capital.domain.merchant;

import java.math.BigDecimal;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAccount {
    private BigDecimal balance;
    private String currency;
    private BigDecimal dailySales;
    
    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.dailySales = this.dailySales.add(amount);
    }
    
    public void resetDailySales() {
        this.dailySales = BigDecimal.ZERO;
    }
}