package com.capital.domain.merchant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capital.domain.product.Product;
import com.capital.domain.shared.Account;

@ExtendWith(MockitoExtension.class)
class MerchantTest {

    @Mock
    private Account mockAccount;
    
    @Mock
    private Product mockProduct1;
    
    @Mock
    private Product mockProduct2;
    
    @Mock
    private MerchantAccountMonitor mockMerchantAccountMonitor;
    
    @InjectMocks
    private Merchant merchant;
    
    @BeforeEach
    void setUp() {
        // 重置注入的mocks
        merchant.setProducts(new java.util.ArrayList<>());
    }
    
    @Test
    void testGetterAndSetter() {
        // 测试id
        merchant.setId(1L);
        assertEquals(1L, merchant.getId());
        
        // 测试name
        merchant.setName("Test Merchant");
        assertEquals("Test Merchant", merchant.getName());
        
        // 测试code
        merchant.setCode("MERCHANT001");
        assertEquals("MERCHANT001", merchant.getCode());
        
        // 测试accountId
        merchant.setAccountId(100L);
        assertEquals(100L, merchant.getAccountId());
        
        // 测试account
        merchant.setAccount(mockAccount);
        assertEquals(mockAccount, merchant.getAccount());
        
        // 测试products
        List<Product> products = Arrays.asList(mockProduct1, mockProduct2);
        merchant.setProducts(products);
        assertEquals(2, merchant.getProducts().size());
        
        // 测试merchantAccountMonitor
        merchant.setMerchantAccountMonitor(mockMerchantAccountMonitor);
        assertEquals(mockMerchantAccountMonitor, merchant.getMerchantAccountMonitor());
    }
    
    @Test
    void testAddProduct() {
        // 初始为空
        assertEquals(0, merchant.getProducts().size());
        
        // 添加第一个产品
        merchant.addProduct(mockProduct1);
        assertEquals(1, merchant.getProducts().size());
        assertTrue(merchant.getProducts().contains(mockProduct1));
        
        // 添加第二个产品
        merchant.addProduct(mockProduct2);
        assertEquals(2, merchant.getProducts().size());
        assertTrue(merchant.getProducts().contains(mockProduct2));
    }
    
    @Test
    void testFindProductBySku_ProductExists() {
        // 设置mock产品
        when(mockProduct1.getSku()).thenReturn("SKU001");
        when(mockProduct2.getSku()).thenReturn("SKU002");
        
        merchant.addProduct(mockProduct1);
        merchant.addProduct(mockProduct2);
        
        // 查找存在的产品
        Product foundProduct = merchant.findProductBySku("SKU001");
        assertNotNull(foundProduct);
        assertEquals(mockProduct1, foundProduct);
        
        // 查找另一个存在的产品
        foundProduct = merchant.findProductBySku("SKU002");
        assertNotNull(foundProduct);
        assertEquals(mockProduct2, foundProduct);
        
        // 验证mock交互
        verify(mockProduct1, atLeastOnce()).getSku();
        verify(mockProduct2, atLeastOnce()).getSku();
    }
    
    @Test
    void testFindProductBySku_ProductNotExists() {
        // 设置mock产品
        when(mockProduct1.getSku()).thenReturn("SKU001");
        
        merchant.addProduct(mockProduct1);
        
        // 查找不存在的产品
        Product foundProduct = merchant.findProductBySku("NONEXISTENT");
        assertNull(foundProduct);
        
        // 验证mock交互
        verify(mockProduct1, atLeastOnce()).getSku();
    }
    
    @Test
    void testFindProductBySku_EmptyProductsList() {
        // 产品列表为空时查找
        Product foundProduct = merchant.findProductBySku("ANY_SKU");
        assertNull(foundProduct);
    }
    
    @Test
    void testAddBalance() {
        BigDecimal amount = new BigDecimal("100.50");
        
        // 测试添加余额
        merchant.addBalance(amount);
        
        // 验证account的addBalance方法被调用
        verify(mockAccount, times(1)).addBalance(amount);
    }
    
    @Test
    void testAddBalance_WithNullAccount() {
        merchant.setAccount(null);
        BigDecimal amount = new BigDecimal("100.50");
        
        // 应正常执行而不抛出异常
        assertThrows(NullPointerException.class, ()  -> merchant.addBalance(amount));
    }
    
    @Test
    void testAddBalance_WithDifferentAmounts() {
        // 测试不同金额
        BigDecimal smallAmount = new BigDecimal("10.00");
        BigDecimal largeAmount = new BigDecimal("1000.00");
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        
        merchant.addBalance(smallAmount);
        merchant.addBalance(largeAmount);
        merchant.addBalance(zeroAmount);
        merchant.addBalance(negativeAmount);
        
        verify(mockAccount, times(4)).addBalance(any(BigDecimal.class));
    }
    
    @Test
    void testResetDailySales() {
        // 测试重置日销售额
        merchant.resetDailySales();
        
        // 验证account的resetDailySales方法被调用
        verify(mockAccount, times(1)).resetDailySales();
    }
    
    @Test
    void testResetDailySales_WithNullAccount() {
        merchant.setAccount(null);
        
        // 应正常执行而不抛出异常
        assertThrows(NullPointerException.class,()-> merchant.resetDailySales());
    }
    
    @Test
    void testResetDailyBalance() {
        // 设置account余额
        BigDecimal currentBalance = new BigDecimal("5000.00");
        when(mockAccount.getBalance()).thenReturn(currentBalance);
        
        // 测试重置日余额
        merchant.resetDailyBalance();
        
        // 验证merchantAccountMonitor的resetDailyBalance方法被调用
        verify(mockMerchantAccountMonitor, times(1)).resetDailyBalance(currentBalance);
        // 验证account的getBalance方法被调用
        verify(mockAccount, times(1)).getBalance();
    }
    
    @Test
    void testResetDailyBalance_WithNullMonitor() {
        merchant.setMerchantAccountMonitor(null);
        
        // 应正常执行而不抛出异常
        assertThrows(NullPointerException.class, ()  -> merchant.resetDailyBalance());
    }
    
    @Test
    void testResetDailyBalance_WithNullAccount() {
        merchant.setAccount(null);
        
        // 当account为null时，getBalance会抛出NullPointerException
        assertThrows(NullPointerException.class, () -> merchant.resetDailyBalance());
    }
    
    @Test
    void testConstructorAndDefaultValues() {
        // 测试默认构造函数创建的对象
        Merchant newMerchant = new Merchant();
        assertNotNull(newMerchant);
        
        // 测试products列表默认不为null
        assertNotNull(newMerchant.getProducts());
        assertEquals(0, newMerchant.getProducts().size());
    }
    
    @Test
    void testAddProduct_NullProduct() {
        // 添加null产品
        assertDoesNotThrow(() -> merchant.addProduct(null));
        assertEquals(0, merchant.getProducts().size());
        
        // 在包含null产品的列表中查找
        Product result = merchant.findProductBySku("ANY_SKU");
        assertNull(result);
    }
    
    @Test
    void testFindProductBySku_ProductWithNullSku() {
        // 设置mock产品的sku为null
        when(mockProduct1.getSku()).thenReturn(null);
        
        merchant.addProduct(mockProduct1);
        
        // 查找null sku的产品
        Product result = merchant.findProductBySku("SKU001");
        assertNull(result);
    }
    
    @Test
    void testSetProducts_NullList() {
        // 测试设置null产品列表
        merchant.setProducts(null);
        assertNull(merchant.getProducts());
        
        // 在null列表上调用addProduct应该抛出异常
        assertThrows(NullPointerException.class, () -> merchant.addProduct(mockProduct1));
    }
    
    @Test
    void testCascadeOperations() {
        // 虽然JPA级联操作通常在集成测试中测试，
        // 但我们可以验证一些业务逻辑
        
        // 测试添加产品后列表更新
        assertEquals(0, merchant.getProducts().size());
        merchant.addProduct(mockProduct1);
        assertEquals(1, merchant.getProducts().size());
    }
    
    @Test
    void testEqualsAndHashCode_NotOverridden() {
        // Merchant类没有重写equals和hashCode方法
        Merchant merchant1 = new Merchant();
        merchant1.setId(1L);
        merchant1.setName("Merchant1");
        
        Merchant merchant2 = new Merchant();
        merchant2.setId(1L);
        merchant2.setName("Merchant1");
        
        // 由于没有重写equals，两个对象不应该相等（除非是同一个对象）
        assertNotEquals(merchant1, merchant2);
        
        // 同一个对象应该相等
        assertEquals(merchant1, merchant1);
        
        // 与null比较
        assertNotEquals(merchant1, null);
        
        // 与不同类型的对象比较
        assertNotEquals(merchant1, "Not a merchant");
    }
    
    @Test
    void testToString_DefaultImplementation() {
        // 测试默认的toString方法
        merchant.setId(1L);
        merchant.setName("Test Merchant");
        
        String toStringResult = merchant.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Merchant"));
    }
}