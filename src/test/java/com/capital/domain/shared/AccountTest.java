package com.capital.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AccountTest {
    
    @Test
    void testAccountConstructorAndGetters() {
        // 测试构造函数和getter方法
        Account account = new Account();
        
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCurrency()).isEqualTo("CNY");
        assertThat(account.getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.isActive()).isTrue();
    }
    
    @Test
    void testRecharge() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        
        account.recharge(new BigDecimal("50.00"));
        
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
    
    @Test
    void testRechargeWithZeroAmount() {
        Account account = new Account();
        
        assertThatThrownBy(() -> account.recharge(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Recharge amount must be positive");
            
        assertThatThrownBy(() -> account.recharge(new BigDecimal("-10.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Recharge amount must be positive");
    }
    
    @Test
    void testDeduct() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        
        boolean result = account.deduct(new BigDecimal("30.00"));
        
        assertThat(result).isTrue();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
    }
    
    @Test
    void testDeductInsufficientBalance() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        
        boolean result = account.deduct(new BigDecimal("150.00"));
        
        assertThat(result).isFalse();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
    
    @Test
    void testDeductWithZeroAmount() {
        Account account = new Account();
        
        assertThatThrownBy(() -> account.deduct(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Deduct amount must be positive");
            
        assertThatThrownBy(() -> account.deduct(new BigDecimal("-10.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Deduct amount must be positive");
    }
    
    @Test
    void testAddBalance() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        account.setDailySales(new BigDecimal("50.00"));
        
        account.addBalance(new BigDecimal("30.00"));
        
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(account.getDailySales()).isEqualByComparingTo(new BigDecimal("80.00"));
    }
    
    @Test
    void testAddBalanceWithZeroAmount() {
        Account account = new Account();
        
        assertThatThrownBy(() -> account.addBalance(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Balance amount must be positive");
            
        assertThatThrownBy(() -> account.addBalance(new BigDecimal("-10.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Balance amount must be positive");
    }
    
    @Test
    void testResetDailySales() {
        Account account = new Account();
        account.setDailySales(new BigDecimal("1000.00"));
        
        account.resetDailySales();
        
        assertThat(account.getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void testAllArgsConstructor() {
        // 测试全参构造函数
        Long id = 1L;
        BigDecimal balance = new BigDecimal("1000.00");
        String currency = "USD";
        String accountType = "SAVINGS";
        BigDecimal dailySales = new BigDecimal("500.00");
        boolean active = true;
        
        Account account = new Account(id, balance, currency, accountType, dailySales, active);
        
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getBalance()).isEqualTo(balance);
        assertThat(account.getCurrency()).isEqualTo(currency);
        assertThat(account.getAccountType()).isEqualTo(accountType);
        assertThat(account.getDailySales()).isEqualTo(dailySales);
        assertThat(account.isActive()).isEqualTo(active);
    }
}