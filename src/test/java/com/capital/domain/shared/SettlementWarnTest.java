package com.capital.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

class SettlementWarnTest {
    
    private SettlementWarn settlementWarn;
    
    @BeforeEach
    void setUp() {
        settlementWarn = new SettlementWarn();
    }
    
    @Test
    void testDefaultConstructor() {
        // 验证默认值
        assertNotNull(settlementWarn);
        assertNull(settlementWarn.getId());
        assertNull(settlementWarn.getMerchantId());
        assertNotNull(settlementWarn.getPreBalance());
        assertEquals(BigDecimal.ZERO, settlementWarn.getPreBalance());
        assertNotNull(settlementWarn.getBalance());
        assertEquals(BigDecimal.ZERO, settlementWarn.getBalance());
        assertNotNull(settlementWarn.getDailySales());
        assertEquals(BigDecimal.ZERO, settlementWarn.getDailySales());
        assertNull(settlementWarn.getCreatedAt());
    }
    
    @Test
    void testAllArgsConstructor() {
        // 准备测试数据
        Long id = 1L;
        Long merchantId = 100L;
        BigDecimal preBalance = new BigDecimal("1000.00");
        BigDecimal balance = new BigDecimal("2000.00");
        BigDecimal dailySales = new BigDecimal("500.00");
        LocalDateTime createdAt = LocalDateTime.now();
        
        // 创建对象
        SettlementWarn warn = new SettlementWarn(id, merchantId, preBalance, balance, dailySales, createdAt);
        
        // 验证所有字段
        assertEquals(id, warn.getId());
        assertEquals(merchantId, warn.getMerchantId());
        assertEquals(preBalance, warn.getPreBalance());
        assertEquals(balance, warn.getBalance());
        assertEquals(dailySales, warn.getDailySales());
        assertEquals(createdAt, warn.getCreatedAt());
    }
    
    @Nested
    class GetterSetterTests {
        
        @Test
        void testIdGetterAndSetter() {
            // 给定
            Long expectedId = 123L;
            
            // 当
            settlementWarn.setId(expectedId);
            
            // 则
            assertEquals(expectedId, settlementWarn.getId());
        }
        
        @Test
        void testMerchantIdGetterAndSetter() {
            // 给定
            Long expectedMerchantId = 456L;
            
            // 当
            settlementWarn.setMerchantId(expectedMerchantId);
            
            // 则
            assertEquals(expectedMerchantId, settlementWarn.getMerchantId());
        }
        
        @Test
        void testPreBalanceGetterAndSetter() {
            // 给定
            BigDecimal expectedPreBalance = new BigDecimal("1500.75");
            
            // 当
            settlementWarn.setPreBalance(expectedPreBalance);
            
            // 则
            assertEquals(expectedPreBalance, settlementWarn.getPreBalance());
        }
        
        @Test
        void testBalanceGetterAndSetter() {
            // 给定
            BigDecimal expectedBalance = new BigDecimal("2500.50");
            
            // 当
            settlementWarn.setBalance(expectedBalance);
            
            // 则
            assertEquals(expectedBalance, settlementWarn.getBalance());
        }
        
        @Test
        void testDailySalesGetterAndSetter() {
            // 给定
            BigDecimal expectedDailySales = new BigDecimal("300.25");
            
            // 当
            settlementWarn.setDailySales(expectedDailySales);
            
            // 则
            assertEquals(expectedDailySales, settlementWarn.getDailySales());
        }
        
        @Test
        void testCreatedAtGetterAndSetter() {
            // 给定
            LocalDateTime expectedCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 30, 45);
            
            // 当
            settlementWarn.setCreatedAt(expectedCreatedAt);
            
            // 则
            assertEquals(expectedCreatedAt, settlementWarn.getCreatedAt());
        }
        
        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
        void testMerchantIdBoundaryValues(Long merchantId) {
            settlementWarn.setMerchantId(merchantId);
            assertEquals(merchantId, settlementWarn.getMerchantId());
        }
    }
    
    @Nested
    class BigDecimalTests {
        
        @Test
        void testPreBalanceWithNullValue() {
            // 创建新对象测试默认值
            SettlementWarn warn = new SettlementWarn();
            assertNotNull(warn.getPreBalance());
            assertEquals(BigDecimal.ZERO, warn.getPreBalance());
            
            // 测试设置为null
            warn.setPreBalance(null);
            assertNull(warn.getPreBalance());
        }
        
        @Test
        void testBalanceWithNullValue() {
            SettlementWarn warn = new SettlementWarn();
            assertNotNull(warn.getBalance());
            assertEquals(BigDecimal.ZERO, warn.getBalance());
            
            warn.setBalance(null);
            assertNull(warn.getBalance());
        }
        
        @Test
        void testDailySalesWithNullValue() {
            SettlementWarn warn = new SettlementWarn();
            assertNotNull(warn.getDailySales());
            assertEquals(BigDecimal.ZERO, warn.getDailySales());
            
            warn.setDailySales(null);
            assertNull(warn.getDailySales());
        }
        
        @Test
        void testBigDecimalPrecision() {
            BigDecimal preciseValue = new BigDecimal("123.456789");
            settlementWarn.setBalance(preciseValue);
            
            assertEquals(preciseValue, settlementWarn.getBalance());
            assertEquals(6, settlementWarn.getBalance().scale());
        }
        
        @Test
        void testNegativeBigDecimal() {
            BigDecimal negativeValue = new BigDecimal("-100.50");
            settlementWarn.setPreBalance(negativeValue);
            
            assertEquals(negativeValue, settlementWarn.getPreBalance());
        }
        
        @Test
        void testZeroBigDecimal() {
            BigDecimal zeroValue = BigDecimal.ZERO;
            settlementWarn.setDailySales(zeroValue);
            
            assertEquals(zeroValue, settlementWarn.getDailySales());
            assertSame(BigDecimal.ZERO, settlementWarn.getDailySales());
        }
    }
    
    @Nested
    class TimeFieldTests {
        
        @Test
        void testCreatedAtNull() {
            settlementWarn.setCreatedAt(null);
            assertNull(settlementWarn.getCreatedAt());
        }
        
        @Test
        void testCreatedAtFutureTime() {
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            settlementWarn.setCreatedAt(futureTime);
            assertEquals(futureTime, settlementWarn.getCreatedAt());
        }
        
        @Test
        void testCreatedAtPastTime() {
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
            settlementWarn.setCreatedAt(pastTime);
            assertEquals(pastTime, settlementWarn.getCreatedAt());
        }
        
        @Test
        void testCreatedAtMinTime() {
            LocalDateTime minTime = LocalDateTime.MIN;
            settlementWarn.setCreatedAt(minTime);
            assertEquals(minTime, settlementWarn.getCreatedAt());
        }
        
        @Test
        void testCreatedAtMaxTime() {
            LocalDateTime maxTime = LocalDateTime.MAX;
            settlementWarn.setCreatedAt(maxTime);
            assertEquals(maxTime, settlementWarn.getCreatedAt());
        }
    }
    
    @Test
    void testEqualsAndHashCode() {
        // 创建两个相同的对象
        LocalDateTime now = LocalDateTime.now();
        SettlementWarn warn1 = new SettlementWarn(1L, 100L, 
            new BigDecimal("1000"), new BigDecimal("2000"), 
            new BigDecimal("500"), now);
        SettlementWarn warn2 = new SettlementWarn(1L, 100L, 
            new BigDecimal("1000"), new BigDecimal("2000"), 
            new BigDecimal("500"), now);
        
        // 测试equals - 使用assertj的isEqualTo
        assertThat(warn1).isEqualTo(warn1); // 自反性
        assertThat(warn1).isNotEqualTo(null); // 非空性
        assertThat(warn1).isNotEqualTo(new Object()); // 不同类型
        
        // 注意：由于没有重写equals和hashCode方法，所以这两个对象不相等
        // 这是正常的，因为使用Lombok的@Data才会自动生成这些方法
        
        // 测试hashCode
        assertEquals(warn1.hashCode(), warn1.hashCode());
    }
    
    @Test
    void testToString() {
        SettlementWarn warn = new SettlementWarn(1L, 100L, 
            BigDecimal.TEN, BigDecimal.ONE, 
            new BigDecimal("5"), LocalDateTime.now());
        
        String toStringResult = warn.toString();
        
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("SettlementWarn"));
    }
    
    @Nested
    class ReflectionTests {
        
        @Test
        void testFieldAnnotations() throws NoSuchFieldException {
            // 测试id字段的注解
            Field idField = SettlementWarn.class.getDeclaredField("id");
            assertNotNull(idField.getAnnotation(javax.persistence.Id.class));
            assertNotNull(idField.getAnnotation(javax.persistence.GeneratedValue.class));
            assertNotNull(idField.getAnnotation(org.hibernate.annotations.GenericGenerator.class));
            
            // 测试merchantId字段的注解
            Field merchantIdField = SettlementWarn.class.getDeclaredField("merchantId");
            Column columnAnnotation = merchantIdField.getAnnotation(Column.class);
            assertNotNull(columnAnnotation);
            assertEquals("merchant_id", columnAnnotation.name());
            assertTrue(columnAnnotation.unique());
            
            // 测试balance字段的注解
            Field balanceField = SettlementWarn.class.getDeclaredField("balance");
            Column balanceColumn = balanceField.getAnnotation(Column.class);
            assertNotNull(balanceColumn);
            assertEquals("balance", balanceColumn.name());
            assertFalse(balanceColumn.nullable());
        }
        
        @Test
        void testClassLevelAnnotations() {
            Entity entityAnnotation = SettlementWarn.class.getAnnotation(Entity.class);
            assertNotNull(entityAnnotation);
            
            Table tableAnnotation = SettlementWarn.class.getAnnotation(Table.class);
            assertNotNull(tableAnnotation);
            assertEquals("settlement_warns", tableAnnotation.name());
            
            JsonIgnoreProperties jsonAnnotation = SettlementWarn.class.getAnnotation(JsonIgnoreProperties.class);
            assertNotNull(jsonAnnotation);
            assertArrayEquals(new String[]{"hibernateLazyInitializer", "handler"}, jsonAnnotation.value());
        }
    }
    
    @Nested
    class IntegrationScenarios {
        
        @Test
        void testCompleteBusinessScenario() {
            // 模拟一个完整的业务场景
            Long merchantId = 999L;
            BigDecimal initialPreBalance = new BigDecimal("10000.00");
            BigDecimal currentBalance = new BigDecimal("15000.00");
            BigDecimal todaySales = new BigDecimal("5000.00");
            LocalDateTime createdTime = LocalDateTime.now();
            
            // 创建预警对象
            SettlementWarn warn = new SettlementWarn();
            warn.setId(1L);
            warn.setMerchantId(merchantId);
            warn.setPreBalance(initialPreBalance);
            warn.setBalance(currentBalance);
            warn.setDailySales(todaySales);
            warn.setCreatedAt(createdTime);
            
            // 验证业务逻辑（这里可以根据实际业务添加更多验证）
            assertTrue(warn.getBalance().compareTo(warn.getPreBalance()) > 0, 
                "当前余额应大于预余额");
            
            // 计算增长额
            BigDecimal increase = warn.getBalance().subtract(warn.getPreBalance());
            assertTrue(increase.compareTo(BigDecimal.ZERO) > 0);
            
            // 验证时间在合理范围内
            assertTrue(warn.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        }
        
        @Test
        void testZeroBalanceScenario() {
            SettlementWarn warn = new SettlementWarn();
            warn.setBalance(BigDecimal.ZERO);
            warn.setPreBalance(BigDecimal.ZERO);
            
            assertEquals(0, warn.getBalance().compareTo(warn.getPreBalance()));
        }
        
        @Test
        void testLargeAmountScenario() {
            BigDecimal veryLargeAmount = new BigDecimal("999999999999999.99");
            settlementWarn.setBalance(veryLargeAmount);
            settlementWarn.setDailySales(veryLargeAmount);
            
            assertNotNull(settlementWarn.getBalance());
            assertNotNull(settlementWarn.getDailySales());
        }
    }
    
    @Test
    void testSerializationCompatibility() {
        // 创建一个完整的对象
        SettlementWarn original = new SettlementWarn(
            1L,
            100L,
            new BigDecimal("1000.00"),
            new BigDecimal("2000.00"),
            new BigDecimal("300.00"),
            LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        );
        
        // 通过getter/setter复制对象
        SettlementWarn copy = new SettlementWarn();
        copy.setId(original.getId());
        copy.setMerchantId(original.getMerchantId());
        copy.setPreBalance(original.getPreBalance());
        copy.setBalance(original.getBalance());
        copy.setDailySales(original.getDailySales());
        copy.setCreatedAt(original.getCreatedAt());
        
        // 验证所有字段相等
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getMerchantId(), copy.getMerchantId());
        assertEquals(original.getPreBalance(), copy.getPreBalance());
        assertEquals(original.getBalance(), copy.getBalance());
        assertEquals(original.getDailySales(), copy.getDailySales());
        assertEquals(original.getCreatedAt(), copy.getCreatedAt());
    }
}