package com.capital.domain.merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

class MerchantAccountMonitorTest {

    private MerchantAccountMonitor monitor;
    private static final Long TEST_MERCHANT_ID = 10001L;
    private static final Long TEST_ACCOUNT_ID = 20001L;
    private static final BigDecimal TEST_BALANCE = new BigDecimal("1000.50");
    private static final String TEST_CURRENCY = "USD";

    @BeforeEach
    void setUp() {
        monitor = new MerchantAccountMonitor();
    }

    @Test
    void testNoArgsConstructor() {
        assertNotNull(monitor);
        assertEquals(BigDecimal.ZERO, monitor.getBalance());
        assertEquals("CNY", monitor.getCurrency());
        assertNull(monitor.getMerchantId());
        assertNull(monitor.getAccountId());
    }

    @Test
    void testAllArgsConstructor() {
        MerchantAccountMonitor fullMonitor = new MerchantAccountMonitor(
            TEST_MERCHANT_ID,
            TEST_ACCOUNT_ID,
            TEST_BALANCE,
            TEST_CURRENCY
        );

        assertEquals(TEST_MERCHANT_ID, fullMonitor.getMerchantId());
        assertEquals(TEST_ACCOUNT_ID, fullMonitor.getAccountId());
        assertEquals(TEST_BALANCE, fullMonitor.getBalance());
        assertEquals(TEST_CURRENCY, fullMonitor.getCurrency());
    }

    @Test
    void testMerchantIdSetterAndGetter() {
        monitor.setMerchantId(TEST_MERCHANT_ID);
        assertEquals(TEST_MERCHANT_ID, monitor.getMerchantId());
        
        // 测试null值
        monitor.setMerchantId(null);
        assertNull(monitor.getMerchantId());
    }

    @Test
    void testAccountIdSetterAndGetter() {
        monitor.setAccountId(TEST_ACCOUNT_ID);
        assertEquals(TEST_ACCOUNT_ID, monitor.getAccountId());
        
        // 测试null值
        monitor.setAccountId(null);
        assertNull(monitor.getAccountId());
    }

    @Test
    void testBalanceSetterAndGetter() {
        monitor.setBalance(TEST_BALANCE);
        assertEquals(TEST_BALANCE, monitor.getBalance());
        
        // 测试零值
        monitor.setBalance(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, monitor.getBalance());
        
        // 测试负值
        BigDecimal negativeBalance = new BigDecimal("-500.75");
        monitor.setBalance(negativeBalance);
        assertEquals(negativeBalance, monitor.getBalance());
        
        // 测试null值
        monitor.setBalance(null);
        assertNull(monitor.getBalance());
    }

    @Test
    void testCurrencySetterAndGetter() {
        monitor.setCurrency(TEST_CURRENCY);
        assertEquals(TEST_CURRENCY, monitor.getCurrency());
        
        // 测试空字符串
        monitor.setCurrency("");
        assertEquals("", monitor.getCurrency());
        
        // 测试null值
        monitor.setCurrency(null);
        assertNull(monitor.getCurrency());
        
        // 测试其他货币
        monitor.setCurrency("EUR");
        assertEquals("EUR", monitor.getCurrency());
        
        monitor.setCurrency("GBP");
        assertEquals("GBP", monitor.getCurrency());
    }

    @Test
    void testResetDailyBalanceWithPositiveValue() {
        // 先设置一个初始余额
        monitor.setBalance(new BigDecimal("500.00"));
        
        // 重置为新的余额
        BigDecimal newBalance = new BigDecimal("1000.00");
        monitor.resetDailyBalance(newBalance);
        
        assertEquals(newBalance, monitor.getBalance());
    }

    @Test
    void testResetDailyBalanceWithZero() {
        monitor.setBalance(new BigDecimal("500.00"));
        monitor.resetDailyBalance(BigDecimal.ZERO);
        
        assertEquals(BigDecimal.ZERO, monitor.getBalance());
    }

    @Test
    void testResetDailyBalanceWithNegativeValue() {
        monitor.setBalance(new BigDecimal("500.00"));
        BigDecimal negativeBalance = new BigDecimal("-200.00");
        monitor.resetDailyBalance(negativeBalance);
        
        assertEquals(negativeBalance, monitor.getBalance());
    }

    @Test
    void testResetDailyBalanceWithLargeNumber() {
        BigDecimal largeBalance = new BigDecimal("999999999.99");
        monitor.resetDailyBalance(largeBalance);
        
        assertEquals(largeBalance, monitor.getBalance());
    }

    @Test
    void testResetDailyBalanceWithNull() {
        // 如果传入null，应该抛出异常或设置为null
        // 根据方法的实现，这里期望设置为null
        monitor.setBalance(new BigDecimal("500.00"));
        monitor.resetDailyBalance(null);
        
        assertNull(monitor.getBalance());
    }

    @Test
    void testObjectEquality() {
        MerchantAccountMonitor monitor1 = new MerchantAccountMonitor(
            TEST_MERCHANT_ID,
            TEST_ACCOUNT_ID,
            TEST_BALANCE,
            TEST_CURRENCY
        );
        
        MerchantAccountMonitor monitor2 = new MerchantAccountMonitor(
            TEST_MERCHANT_ID,
            TEST_ACCOUNT_ID,
            TEST_BALANCE,
            TEST_CURRENCY
        );
        
        // 测试相同的值
        assertEquals(monitor1.getMerchantId(), monitor2.getMerchantId());
        assertEquals(monitor1.getAccountId(), monitor2.getAccountId());
        assertEquals(monitor1.getBalance(), monitor2.getBalance());
        assertEquals(monitor1.getCurrency(), monitor2.getCurrency());
    }

    @Test
    void testToString() {
        monitor.setMerchantId(TEST_MERCHANT_ID);
        monitor.setAccountId(TEST_ACCOUNT_ID);
        monitor.setBalance(TEST_BALANCE);
        monitor.setCurrency(TEST_CURRENCY);
        
        String toStringResult = monitor.toString();
        assertNotNull(toStringResult);
    }

    @Test
    void testEquals() {
        MerchantAccountMonitor monitor1 = new MerchantAccountMonitor();
        MerchantAccountMonitor monitor2 = new MerchantAccountMonitor();
        
        // 测试自反性
        assertTrue(monitor1.equals(monitor1));
        
        // 测试对称性
        monitor1.setMerchantId(TEST_MERCHANT_ID);
        monitor2.setMerchantId(TEST_MERCHANT_ID);
        
        assertEquals(monitor1.equals(monitor2), monitor2.equals(monitor1));
        
        // 测试与null比较
        assertFalse(monitor1.equals(null));
        
        // 测试与不同类型对象比较
        assertFalse(monitor1.equals("String Object"));
        
        // 测试不同值的对象
        MerchantAccountMonitor monitor3 = new MerchantAccountMonitor();
        monitor3.setMerchantId(99999L);
        assertFalse(monitor1.equals(monitor3));
    }

    @Test
    void testEntityAnnotations() throws NoSuchFieldException {
        // 测试@Id注解
        Field merchantIdField = MerchantAccountMonitor.class.getDeclaredField("merchantId");
        assertNotNull(merchantIdField.getAnnotation(javax.persistence.Id.class));
        
        // 测试@Column注解
        Field accountIdField = MerchantAccountMonitor.class.getDeclaredField("accountId");
        javax.persistence.Column accountIdColumn = accountIdField.getAnnotation(javax.persistence.Column.class);
        assertNotNull(accountIdColumn);
        assertTrue(accountIdColumn.unique());
        
        // 测试balance字段的@Column注解
        Field balanceField = MerchantAccountMonitor.class.getDeclaredField("balance");
        javax.persistence.Column balanceColumn = balanceField.getAnnotation(javax.persistence.Column.class);
        assertNotNull(balanceColumn);
        assertFalse(balanceColumn.nullable());
        
        // 测试currency字段的@Column注解
        Field currencyField = MerchantAccountMonitor.class.getDeclaredField("currency");
        javax.persistence.Column currencyColumn = currencyField.getAnnotation(javax.persistence.Column.class);
        assertNotNull(currencyColumn);
        assertFalse(currencyColumn.nullable());
    }

    @Test
    void testJsonIgnorePropertiesAnnotation() {
        JsonIgnoreProperties annotation = MerchantAccountMonitor.class
            .getAnnotation(JsonIgnoreProperties.class);
        
        assertNotNull(annotation);
        assertEquals(2, annotation.value().length);
        assertEquals("hibernateLazyInitializer", annotation.value()[0]);
        assertEquals("handler", annotation.value()[1]);
    }

    @Test
    void testEntityAndTableAnnotations() {
        // 测试@Entity注解
        assertNotNull(MerchantAccountMonitor.class.getAnnotation(javax.persistence.Entity.class));
        
        // 测试@Table注解
        javax.persistence.Table tableAnnotation = MerchantAccountMonitor.class
            .getAnnotation(javax.persistence.Table.class);
        assertNotNull(tableAnnotation);
        assertEquals("merchant_account_monitor", tableAnnotation.name());
    }

    @Test
    void testLombokAnnotations() {
        // 通过反射验证字段的存在性
        try {
            MerchantAccountMonitor.class.getDeclaredField("merchantId");
            MerchantAccountMonitor.class.getDeclaredField("accountId");
            MerchantAccountMonitor.class.getDeclaredField("balance");
            MerchantAccountMonitor.class.getDeclaredField("currency");
        } catch (NoSuchFieldException e) {
            fail("实体类字段不存在: " + e.getMessage());
        }
    }

    @Test
    void testBoundaryValues() {
        // 测试Long的最大最小值
        monitor.setMerchantId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, monitor.getMerchantId());
        
        monitor.setMerchantId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, monitor.getMerchantId());
        
        // 测试BigDecimal的边界值
        BigDecimal maxDecimal = new BigDecimal("999999999999999999999999999999.999999");
        monitor.setBalance(maxDecimal);
        assertEquals(maxDecimal, monitor.getBalance());
        
        BigDecimal minDecimal = new BigDecimal("-999999999999999999999999999999.999999");
        monitor.setBalance(minDecimal);
        assertEquals(minDecimal, monitor.getBalance());
    }
}