package com.capital.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.product.Product;
import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.exception.TradingException;
import com.capital.repository.AccountRepository;
import com.capital.repository.MerchantRepository;
import com.capital.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User mockUser;
    private Account mockUserAccount;
    private Merchant mockMerchant;
    private Product mockProduct;
    private Account mockMerchantAccount;

    @BeforeEach
    void setUp() {
        // 初始化用户账户
        mockUserAccount = new Account();
        mockUserAccount.setId(1L);
        mockUserAccount.setBalance(new BigDecimal("1000.00"));
        mockUserAccount.setActive(true);
        mockUserAccount.setCurrency("CNY");
        mockUserAccount.setAccountType("USER");

        // 初始化用户
        mockUser = new User();
        mockUser.setId(100L);
        mockUser.setActive(true);
        mockUser.setAccount(mockUserAccount);

        // 初始化商户账户
        mockMerchantAccount = new Account();
        mockMerchantAccount.setId(2L);
        mockMerchantAccount.setBalance(new BigDecimal("5000.00"));
        mockMerchantAccount.setActive(true);
        mockMerchantAccount.setCurrency("CNY");
        mockMerchantAccount.setAccountType("MERCHANT");
        mockMerchantAccount.setDailySales(BigDecimal.ZERO);

        // 初始化产品
        mockProduct = new Product();
        mockProduct.setId(300L);
        mockProduct.setSku("TEST_SKU");
        mockProduct.setName("Test Product");
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setStockQuantity(50);
        mockProduct.setMerchantId(200L);

        // 初始化商户（使用mock对象）
        mockMerchant = mock(Merchant.class);
//        when(mockMerchant.getId()).thenReturn(200L);
//        when(mockMerchant.getAccount()).thenReturn(mockMerchantAccount);
//        when(mockMerchant.getAccountId()).thenReturn(mockMerchantAccount.getId());
    }

    @Test
    void testPurchase_Success() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行
        Map<String, Object> result = transactionService.purchase(100L, 200L, "TEST_SKU", 2);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.get("totalCost"));
        assertEquals(2, result.get("quantity"));
        assertSame(mockProduct, result.get("product"));

        // 验证账户余额减少
        assertEquals(new BigDecimal("800.00"), mockUserAccount.getBalance());
        // 验证库存减少
        assertEquals(48, mockProduct.getStockQuantity());
        // 验证商户余额和日销售额增加
        assertEquals(new BigDecimal("5200.00"), mockMerchantAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("200.00"));
    }

    @Test
    void testPurchase_UserNotFound() {
        // 准备
        when(userRepository.findByIdAndActive(999L, true)).thenReturn(null);

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(999L, 200L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(999L, true);
        verify(merchantRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_UserNotActive() {
        // 准备 - 用户不活跃
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(null);

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_AccountNotFound() {
        // 准备 - 用户没有账户
        User userWithoutAccount = new User();
        userWithoutAccount.setId(100L);
        userWithoutAccount.setActive(true);
        userWithoutAccount.setAccount(null);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(userWithoutAccount);

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_MerchantNotFound() {
        // 准备
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 999L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_ProductNotFound() {
        // 准备
        when(mockMerchant.findProductBySku("INVALID_SKU")).thenReturn(null);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 使用不存在的SKU
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "INVALID_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("INVALID_SKU");
    }

    @Test
    void testPurchase_InsufficientStock() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 购买数量超过库存
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 100);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("TEST_SKU");
    }

    @Test
    void testPurchase_ZeroQuantity() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 购买数量为0
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 0);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("TEST_SKU");
    }

    @Test
    void testPurchase_NegativeQuantity() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 购买数量为负数
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", -1);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("TEST_SKU");
    }

    @Test
    void testPurchase_InsufficientBalance() {
        // 准备 - 用户余额不足
        mockUserAccount.setBalance(new BigDecimal("50.00")); // 余额只有50
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 购买2件产品需要200元，但余额只有50
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("TEST_SKU");
    }

    @Test
    void testPurchase_NullSku() {
        // 准备
        when(mockMerchant.findProductBySku(null)).thenReturn(null);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, null, 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(mockMerchant, times(1)).findProductBySku(null);
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_EmptySku() {
        // 准备
        when(mockMerchant.findProductBySku("")).thenReturn(null);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(mockMerchant, times(1)).findProductBySku("");
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_NullQuantity() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", null);
        });

        assertNotNull(exception);
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("TEST_SKU");
    }

    @Test
    void testPurchase_NullUserId() {
        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(null, 200L, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, never()).findByIdAndActive(anyLong(), anyBoolean());
        verify(merchantRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_NullMerchantId() {
        // 准备
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, null, "TEST_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testPurchase_ExactBalance() {
        // 准备 - 用户余额正好等于总价
        mockUserAccount.setBalance(new BigDecimal("200.00"));
        
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行
        Map<String, Object> result = transactionService.purchase(100L, 200L, "TEST_SKU", 2);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.get("totalCost"));
        assertEquals(0, mockUserAccount.getBalance().compareTo(BigDecimal.ZERO)); // 余额应为0
        assertEquals(48, mockProduct.getStockQuantity());
        assertEquals(new BigDecimal("5200.00"), mockMerchantAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("200.00"));
    }

    @Test
    void testPurchase_SingleItem() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行 - 购买1件
        Map<String, Object> result = transactionService.purchase(100L, 200L, "TEST_SKU", 1);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.get("totalCost"));
        assertEquals(1, result.get("quantity"));
        assertEquals(new BigDecimal("900.00"), mockUserAccount.getBalance()); // 1000 - 100
        assertEquals(49, mockProduct.getStockQuantity()); // 50 - 1
        assertEquals(new BigDecimal("5100.00"), mockMerchantAccount.getBalance()); // 5000 + 100
        assertEquals(new BigDecimal("100.00"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("100.00"));
    }

    @Test
    void testPurchase_AccountSaveFailure() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));

        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.purchase(100L, 200L, "TEST_SKU", 2);
        });

        assertEquals("Database error", exception.getMessage());
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("200.00"));
    }

    @Test
    void testPurchase_ResultMapStructure() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行
        Map<String, Object> result = transactionService.purchase(100L, 200L, "TEST_SKU", 3);

        // 验证返回的Map结构
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("totalCost"));
        assertTrue(result.containsKey("quantity"));
        assertTrue(result.containsKey("product"));
        
        assertEquals(new BigDecimal("300.00"), result.get("totalCost"));
        assertEquals(3, result.get("quantity"));
        assertSame(mockProduct, result.get("product"));
    }

    @Test
    void testPurchase_FailedToReduceStock() {
        // 准备 - 创建一个会返回false的reduceStock方法的产品
        Product problematicProduct = new Product() {
            @Override
            public boolean reduceStock(Integer quantity) {
                return false; // 总是返回false，模拟减少库存失败
            }
        };
        problematicProduct.setId(301L);
        problematicProduct.setSku("PROBLEM_SKU");
        problematicProduct.setName("Problem Product");
        problematicProduct.setPrice(new BigDecimal("100.00"));
        problematicProduct.setStockQuantity(50);
        problematicProduct.setMerchantId(200L);
        
        // 为这个特殊SKU设置返回有问题的产品
        when(mockMerchant.findProductBySku("PROBLEM_SKU")).thenReturn(problematicProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证
        TradingException exception = assertThrows(TradingException.class, () -> {
            transactionService.purchase(100L, 200L, "PROBLEM_SKU", 2);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, times(1)).findProductBySku("PROBLEM_SKU");
    }

    @Test
    void testPurchase_WithDecimalPrice() {
        // 准备 - 创建小数价格的产品
        Product decimalProduct = new Product();
        decimalProduct.setId(302L);
        decimalProduct.setSku("DECIMAL_SKU");
        decimalProduct.setName("Decimal Product");
        decimalProduct.setPrice(new BigDecimal("19.99"));
        decimalProduct.setStockQuantity(20);
        decimalProduct.setMerchantId(200L);
        
        when(mockMerchant.findProductBySku("DECIMAL_SKU")).thenReturn(decimalProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行 - 购买3件小数价格产品
        Map<String, Object> result = transactionService.purchase(100L, 200L, "DECIMAL_SKU", 3);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("59.97"), result.get("totalCost")); // 19.99 * 3 = 59.97
        assertEquals(3, result.get("quantity"));
        assertEquals(new BigDecimal("940.03"), mockUserAccount.getBalance()); // 1000 - 59.97
        assertEquals(17, decimalProduct.getStockQuantity()); // 20 - 3
        assertEquals(new BigDecimal("5059.97"), mockMerchantAccount.getBalance()); // 5000 + 59.97
        assertEquals(new BigDecimal("59.97"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("59.97"));
    }

    @Test
    void testPurchase_MaxQuantity() {
        // 准备 - 购买全部库存
        // 首先增加用户余额，使其足够支付50件商品（100 * 50 = 5000）
        mockUserAccount.setBalance(new BigDecimal("5000.00"));
        
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行 - 购买全部50件
        Map<String, Object> result = transactionService.purchase(100L, 200L, "TEST_SKU", 50);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.get("totalCost")); // 100 * 50
        assertEquals(50, result.get("quantity"));
        assertEquals(new BigDecimal("0.00"), mockUserAccount.getBalance()); // 5000 - 5000（正确，不会变负数）
        assertEquals(0, mockProduct.getStockQuantity()); // 50 - 50
        assertEquals(new BigDecimal("10000.00"), mockMerchantAccount.getBalance()); // 5000 + 5000
        assertEquals(new BigDecimal("5000.00"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("5000.00"));
    }

    @Test
    void testPurchase_MultiplePurchases() {
        // 准备
        when(mockMerchant.findProductBySku("TEST_SKU")).thenReturn(mockProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 重置产品和账户状态
        mockProduct.setStockQuantity(50);
        mockUserAccount.setBalance(new BigDecimal("1000.00"));
        mockMerchantAccount.setBalance(new BigDecimal("5000.00"));
        mockMerchantAccount.setDailySales(BigDecimal.ZERO);

        // 第一次购买
        Map<String, Object> result1 = transactionService.purchase(100L, 200L, "TEST_SKU", 2);
        assertNotNull(result1);
        assertEquals(new BigDecimal("200.00"), result1.get("totalCost"));
        assertEquals(new BigDecimal("800.00"), mockUserAccount.getBalance()); // 1000 - 200
        assertEquals(48, mockProduct.getStockQuantity()); // 50 - 2

        // 第二次购买
        Map<String, Object> result2 = transactionService.purchase(100L, 200L, "TEST_SKU", 3);
        assertNotNull(result2);
        assertEquals(new BigDecimal("300.00"), result2.get("totalCost"));
        assertEquals(new BigDecimal("500.00"), mockUserAccount.getBalance()); // 800 - 300
        assertEquals(45, mockProduct.getStockQuantity()); // 48 - 3

        // 第三次购买
        Map<String, Object> result3 = transactionService.purchase(100L, 200L, "TEST_SKU", 5);
        assertNotNull(result3);
        assertEquals(new BigDecimal("500.00"), result3.get("totalCost"));
        assertEquals(new BigDecimal("0.00"), mockUserAccount.getBalance()); // 500 - 500
        assertEquals(40, mockProduct.getStockQuantity()); // 45 - 5

        // 验证商户余额和日销售额
        assertEquals(new BigDecimal("6000.00"), mockMerchantAccount.getBalance()); // 5000 + 1000
        assertEquals(new BigDecimal("1000.00"), mockMerchantAccount.getDailySales()); // 200 + 300 + 500

        verify(userRepository, times(3)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(3)).findById(200L);
        verify(accountRepository, times(3)).save(mockUserAccount);
        verify(mockMerchant, times(3)).addBalance(any(BigDecimal.class));
    }

    @Test
    void testPurchase_ProductWithZeroPrice() {
        // 准备 - 创建免费产品
        Product freeProduct = new Product();
        freeProduct.setId(303L);
        freeProduct.setSku("FREE_SKU");
        freeProduct.setName("Free Product");
        freeProduct.setPrice(BigDecimal.ZERO);
        freeProduct.setStockQuantity(10);
        freeProduct.setMerchantId(200L);
        
        when(mockMerchant.findProductBySku("FREE_SKU")).thenReturn(freeProduct);
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));

        // 执行和验证 - 购买免费产品会抛出IllegalArgumentException
        // 因为Account.deduct()要求金额必须为正数
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.purchase(100L, 200L, "FREE_SKU", 5);
        });

        assertEquals("Deduct amount must be positive", exception.getMessage());
        
        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, never()).save(any());
        verify(mockMerchant, never()).addBalance(any(BigDecimal.class));
        verify(mockMerchant, times(1)).findProductBySku("FREE_SKU");
    }

    @Test
    void testPurchase_ProductWithVerySmallPrice() {
        // 准备 - 创建价格非常小的产品
        Product smallPriceProduct = new Product();
        smallPriceProduct.setId(304L);
        smallPriceProduct.setSku("SMALL_PRICE_SKU");
        smallPriceProduct.setName("Small Price Product");
        smallPriceProduct.setPrice(new BigDecimal("0.01")); // 1分钱
        smallPriceProduct.setStockQuantity(100);
        smallPriceProduct.setMerchantId(200L);
        
        when(mockMerchant.findProductBySku("SMALL_PRICE_SKU")).thenReturn(smallPriceProduct);
        doAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            mockMerchantAccount.addBalance(amount);
            return null;
        }).when(mockMerchant).addBalance(any(BigDecimal.class));
        
        when(userRepository.findByIdAndActive(100L, true)).thenReturn(mockUser);
        when(merchantRepository.findById(200L)).thenReturn(Optional.of(mockMerchant));
        when(accountRepository.save(any(Account.class))).thenReturn(mockUserAccount);

        // 执行 - 购买100件非常便宜的产品
        Map<String, Object> result = transactionService.purchase(100L, 200L, "SMALL_PRICE_SKU", 100);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("1.00"), result.get("totalCost")); // 0.01 * 100 = 1.00
        assertEquals(100, result.get("quantity"));
        assertEquals(new BigDecimal("999.00"), mockUserAccount.getBalance()); // 1000 - 1.00
        assertEquals(0, smallPriceProduct.getStockQuantity()); // 100 - 100
        assertEquals(new BigDecimal("5001.00"), mockMerchantAccount.getBalance()); // 5000 + 1.00
        assertEquals(new BigDecimal("1.00"), mockMerchantAccount.getDailySales());

        verify(userRepository, times(1)).findByIdAndActive(100L, true);
        verify(merchantRepository, times(1)).findById(200L);
        verify(accountRepository, times(1)).save(mockUserAccount);
        verify(mockMerchant, times(1)).addBalance(new BigDecimal("1.00"));
    }
}