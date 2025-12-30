package com.capital.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

class ProductTest {
    
    private Product product;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("SKU-001");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setMerchantId(123L);
    }
    
    @Test
    void testGettersAndSetters() {
        // 测试所有字段的getter和setter
        assertEquals(1L, product.getId());
        assertEquals("SKU-001", product.getSku());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(100, product.getStockQuantity());
        assertEquals(123L, product.getMerchantId());
        
        // 测试setter
        product.setId(2L);
        product.setSku("SKU-002");
        product.setName("Updated Product");
        product.setDescription("Updated Description");
        product.setPrice(new BigDecimal("199.99"));
        product.setStockQuantity(50);
        product.setMerchantId(456L);
        
        assertEquals(2L, product.getId());
        assertEquals("SKU-002", product.getSku());
        assertEquals("Updated Product", product.getName());
        assertEquals("Updated Description", product.getDescription());
        assertEquals(new BigDecimal("199.99"), product.getPrice());
        assertEquals(50, product.getStockQuantity());
        assertEquals(456L, product.getMerchantId());
    }
    
    @Test
    void testNoArgsConstructor() {
        Product emptyProduct = new Product();
        
        assertNotNull(emptyProduct);
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getSku());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getDescription());
        assertNull(emptyProduct.getPrice());
        assertNull(emptyProduct.getStockQuantity());
        assertNull(emptyProduct.getMerchantId());
    }
    
    @Test
    void testAllArgsConstructor() {
        Product fullProduct = new Product(
            10L, 
            "SKU-010", 
            "Full Args Product", 
            "Full Description", 
            new BigDecimal("299.99"), 
            200, 
            789L
        );
        
        assertEquals(10L, fullProduct.getId());
        assertEquals("SKU-010", fullProduct.getSku());
        assertEquals("Full Args Product", fullProduct.getName());
        assertEquals("Full Description", fullProduct.getDescription());
        assertEquals(new BigDecimal("299.99"), fullProduct.getPrice());
        assertEquals(200, fullProduct.getStockQuantity());
        assertEquals(789L, fullProduct.getMerchantId());
    }
    
    @Test
    void testReduceStock_Success() {
        // 初始库存100，减少30
        boolean result = product.reduceStock(30);
        
        assertTrue(result);
        assertEquals(70, product.getStockQuantity());
    }
    
    @Test
    void testReduceStock_InsufficientStock() {
        // 初始库存100，尝试减少150
        boolean result = product.reduceStock(150);
        
        assertFalse(result);
        assertEquals(100, product.getStockQuantity()); // 库存不应改变
    }
    
    @Test
    void testReduceStock_EdgeCases() {
        // 测试正好用完库存
        boolean result1 = product.reduceStock(100);
        assertTrue(result1);
        assertEquals(0, product.getStockQuantity());
        
        // 重置库存
        product.setStockQuantity(100);
        
        // 测试减少0个（虽然实际业务可能不允许，但测试边界情况）
        boolean result2 = product.reduceStock(0);
        assertTrue(result2);
        assertEquals(100, product.getStockQuantity());
    }
    
    @Test
    void testReduceStock_NegativeQuantity() {
        // 测试传入负数（应该正确处理或抛出异常，根据实际需求）
        boolean result = product.reduceStock(-10);
        
        // 注意：当前实现中负数会通过条件判断，因为 -10 < stockQuantity(100) 所以返回true
        // 但会导致库存增加，这可能是个bug，需要根据业务需求调整
        assertTrue(result); // 当前实现会返回true
        assertEquals(110, product.getStockQuantity()); // 库存会增加
    }
    
    @Test
    void testIncreaseStock() {
        // 初始库存100，增加50
        product.increaseStock(50);
        
        assertEquals(150, product.getStockQuantity());
    }
    
    @Test
    void testIncreaseStock_NegativeQuantity() {
        // 测试传入负数
        product.increaseStock(-20);
        
        // 当前实现会减少库存
        assertEquals(80, product.getStockQuantity());
    }
    
    @Test
    void testIncreaseStock_ZeroQuantity() {
        product.increaseStock(0);
        assertEquals(100, product.getStockQuantity());
    }
    
    @Test
    void testCalculateTotalPrice() {
        BigDecimal totalPrice = product.calculateTotalPrice(3);
        
        assertEquals(new BigDecimal("299.97"), totalPrice);
        assertEquals(new BigDecimal("99.99"), product.getPrice()); // 确保原价未改变
    }
    
    @Test
    void testCalculateTotalPrice_EdgeCases() {
        // 测试购买0个
        BigDecimal zeroQuantity = product.calculateTotalPrice(0);
        assertEquals(new BigDecimal("0.00"), zeroQuantity);
        
        // 测试购买1个
        BigDecimal oneQuantity = product.calculateTotalPrice(1);
        assertEquals(new BigDecimal("99.99"), oneQuantity);
        
        // 测试大数量
        BigDecimal largeQuantity = product.calculateTotalPrice(1000);
        assertEquals(new BigDecimal("99990.00"), largeQuantity);
    }
    
    @Test
    void testCalculateTotalPrice_ZeroPrice() {
        product.setPrice(BigDecimal.ZERO);
        BigDecimal totalPrice = product.calculateTotalPrice(5);
        
        assertEquals(BigDecimal.ZERO, totalPrice);
    }
    
    @Test
    void testCalculateTotalPrice_Precision() {
        product.setPrice(new BigDecimal("9.99"));
        BigDecimal totalPrice = product.calculateTotalPrice(3);
        
        assertEquals(new BigDecimal("29.97"), totalPrice);
    }
    
    @Test
    void testIsAvailable() {
        // 库存充足
        assertTrue(product.isAvailable(50));
        
        // 库存正好
        assertTrue(product.isAvailable(100));
        
        // 库存不足
        assertFalse(product.isAvailable(150));
        
        // 数量为0
        assertFalse(product.isAvailable(0));
        
        // 数量为负数
        assertFalse(product.isAvailable(-10));
    }
    
    @ParameterizedTest
    @CsvSource({
        "50, true",
        "100, true",
        "150, false",
        "0, false",
        "-10, false"
    })
    void testIsAvailable_Parameterized(int quantity, boolean expected) {
        assertEquals(expected, product.isAvailable(quantity));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100})
    void testIsAvailable_SufficientStock(int quantity) {
        assertTrue(product.isAvailable(quantity));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {101, 200, 1000})
    void testIsAvailable_InsufficientStock(int quantity) {
        assertFalse(product.isAvailable(quantity));
    }
    
    @Test
    void testWithZeroStock() {
        product.setStockQuantity(0);
        
        assertFalse(product.isAvailable(1));
        assertFalse(product.isAvailable(0));
        assertFalse(product.reduceStock(1));
        assertEquals(0, product.getStockQuantity());
    }
    
    @Test
    void testWithNullStock() {
        Product nullStockProduct = new Product();
        nullStockProduct.setPrice(new BigDecimal("10.00"));
        
        // 测试null库存的各种方法
        assertThrows(NullPointerException.class, () -> {
            nullStockProduct.isAvailable(1);
        });
        
        assertThrows(NullPointerException.class, () -> {
            nullStockProduct.reduceStock(1);
        });
        
        assertThrows(NullPointerException.class, () -> {
            nullStockProduct.increaseStock(1);
        });
    }
    
    @Test
    void testWithNullPrice() {
        Product nullPriceProduct = new Product();
        nullPriceProduct.setStockQuantity(10);
        
        assertThrows(NullPointerException.class, () -> {
            nullPriceProduct.calculateTotalPrice(5);
        });
    }
    
    @Test
    void testToString() {
        // 虽然没有显式的toString方法，但Lombok会生成
        String toString = product.toString();
        assertNotNull(toString);
    }
    
    @Test
    void testEqualsAndHashCode() {
        Product sameProduct = new Product(
            1L, 
            "SKU-001", 
            "Test Product", 
            "Test Description", 
            new BigDecimal("99.99"), 
            100, 
            123L
        );
        
        Product differentProduct = new Product(
            2L, 
            "SKU-002", 
            "Different", 
            "Different", 
            new BigDecimal("199.99"), 
            50, 
            456L
        );
        
        // 测试相等性
        assertThat(product).isNotEqualTo(differentProduct);
        
        // 测试hashCode
        assertThat(product.hashCode()).isNotEqualTo(differentProduct.hashCode());
        
        // 测试与null比较
        assertThat(product).isNotEqualTo(null);
        
        // 测试与不同类型对象比较
        assertThat(product).isNotEqualTo("string");
    }
    
    @Test
    void testJsonIgnoreProperties() throws Exception {
        // 验证@JsonIgnoreProperties注解存在
        JsonIgnoreProperties annotation = Product.class.getAnnotation(JsonIgnoreProperties.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{"hibernateLazyInitializer", "handler"}, annotation.value());
    }
    
    @Test
    void testJpaAnnotations() throws Exception {
        // 验证@Entity注解
        assertNotNull(Product.class.getAnnotation(Entity.class));
        
        // 验证@Table注解
        Table tableAnnotation = Product.class.getAnnotation(Table.class);
        assertNotNull(tableAnnotation);
        assertEquals("products", tableAnnotation.name());
        
        // 验证@Id和@GeneratedValue注解
        Field idField = Product.class.getDeclaredField("id");
        assertNotNull(idField.getAnnotation(javax.persistence.Id.class));
        assertNotNull(idField.getAnnotation(javax.persistence.GeneratedValue.class));
        
        // 验证@Column注解
        Field skuField = Product.class.getDeclaredField("sku");
        Column skuColumn = skuField.getAnnotation(Column.class);
        assertNotNull(skuColumn);
        assertEquals("sku", skuColumn.name());
    }
    
    @Test
    void testCompleteBusinessFlow() {
        // 1. 创建产品
        Product testProduct = new Product(
            100L,
            "TEST-SKU",
            "Test Product",
            "Test Description",
            new BigDecimal("49.99"),
            50,
            999L
        );
        
        // 2. 验证初始状态
        assertTrue(testProduct.isAvailable(10));
        assertEquals(50, testProduct.getStockQuantity());
        
        // 3. 销售产品
        boolean saleSuccess = testProduct.reduceStock(10);
        assertTrue(saleSuccess);
        assertEquals(40, testProduct.getStockQuantity());
        
        // 4. 计算销售额
        BigDecimal saleAmount = testProduct.calculateTotalPrice(10);
        assertEquals(new BigDecimal("499.90"), saleAmount);
        
        // 5. 补货
        testProduct.increaseStock(20);
        assertEquals(60, testProduct.getStockQuantity());
        
        // 6. 尝试销售超过库存的数量
        boolean largeSaleSuccess = testProduct.reduceStock(100);
        assertFalse(largeSaleSuccess);
        assertEquals(60, testProduct.getStockQuantity()); // 库存不变
        
        // 7. 验证最终状态
        assertTrue(testProduct.isAvailable(60));
        assertFalse(testProduct.isAvailable(61));
    }
    
    @Test
    void testBoundaryConditions() {
        // 测试Integer边界值
        product.setStockQuantity(Integer.MAX_VALUE);
        assertTrue(product.isAvailable(Integer.MAX_VALUE));
        
        // 注意：测试Integer.MAX_VALUE的reduceStock可能会导致溢出
        // 实际业务中应该避免这种情况
        product.setStockQuantity(100);
        
        // 测试BigDecimal的边界情况
        product.setPrice(new BigDecimal("999999999.99"));
        BigDecimal maxPrice = product.calculateTotalPrice(999);
        assertNotNull(maxPrice);
    }
    
    @Test
    void testConcurrentModificationScenario() {
        // 模拟多次操作
        product.increaseStock(50);  // 150
        product.reduceStock(30);    // 120
        product.increaseStock(10);  // 130
        product.reduceStock(50);    // 80
        
        assertEquals(80, product.getStockQuantity());
        
        // 验证所有操作后的可用性
        assertTrue(product.isAvailable(80));
        assertFalse(product.isAvailable(81));
    }
}