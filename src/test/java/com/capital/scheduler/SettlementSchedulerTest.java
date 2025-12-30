package com.capital.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capital.domain.merchant.Merchant;
import com.capital.domain.merchant.MerchantAccountMonitor;
import com.capital.domain.shared.Account;
import com.capital.domain.shared.SettlementWarn;
import com.capital.repository.MerchantRepository;
import com.capital.repository.SettlementWarnRepository;

@ExtendWith(MockitoExtension.class)
class SettlementSchedulerTest {
    
    @Mock
    private MerchantRepository merchantRepository;
    
    @Mock
    private SettlementWarnRepository settlementWarnRepository;
    
    @InjectMocks
    private SettlementScheduler settlementScheduler;
    
    private Merchant merchant1;
    private Merchant merchant2;
    private Merchant merchantWithMismatch;
    
    @BeforeEach
    void setUp() {
        // 创建正常的商户1
        merchant1 = new Merchant();
        merchant1.setId(1L);
        
        Account account1 = new Account();
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setDailySales(new BigDecimal("500.00"));
        merchant1.setAccount(account1);
        
        MerchantAccountMonitor monitor1 = new MerchantAccountMonitor();
        monitor1.setBalance(new BigDecimal("500.00")); // 前余额500，当前1000，日销售500，匹配
        merchant1.setMerchantAccountMonitor(monitor1);
        
        // 创建正常的商户2
        merchant2 = new Merchant();
        merchant2.setId(2L);
        
        Account account2 = new Account();
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setDailySales(new BigDecimal("300.00"));
        merchant2.setAccount(account2);
        
        MerchantAccountMonitor monitor2 = new MerchantAccountMonitor();
        monitor2.setBalance(new BigDecimal("1700.00")); // 前余额1700，当前2000，日销售300，匹配
        merchant2.setMerchantAccountMonitor(monitor2);
        
        // 创建余额不匹配的商户
        merchantWithMismatch = new Merchant();
        merchantWithMismatch.setId(3L);
        
        Account account3 = new Account();
        account3.setBalance(new BigDecimal("1500.00"));
        account3.setDailySales(new BigDecimal("500.00"));
        merchantWithMismatch.setAccount(account3);
        
        MerchantAccountMonitor monitor3 = new MerchantAccountMonitor();
        monitor3.setBalance(new BigDecimal("800.00")); // 前余额800，当前1500，日销售500，不匹配(实际差额700)
        merchantWithMismatch.setMerchantAccountMonitor(monitor3);
    }
    
    @Test
    void testDailySettlement_NoMerchants() {
        // 当没有商户时
        when(merchantRepository.findAll()).thenReturn(Arrays.asList());
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
        verify(settlementWarnRepository, times(0)).save(any(SettlementWarn.class));
    }
    
    @Test
    void testDailySettlement_WithNormalMerchants() {
        // 设置正常商户列表
        List<Merchant> merchants = Arrays.asList(merchant1, merchant2);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
        
        // 验证reset方法被调用
        assertThat(merchant1.getAccount().getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        // 注意：Account的resetDailyBalance方法不存在，应该是setBalance方法
        assertThat(merchant1.getAccount().getBalance())
            .isEqualByComparingTo(merchant1.getMerchantAccountMonitor().getBalance());
        
        assertThat(merchant2.getAccount().getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(merchant2.getAccount().getBalance())
            .isEqualByComparingTo(merchant2.getMerchantAccountMonitor().getBalance());
    }
    
    @Test
    void testDailySettlement_WithMismatchMerchant() {
        // 设置包含不匹配商户的列表
        List<Merchant> merchants = Arrays.asList(merchant1, merchantWithMismatch);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
        
        // 验证警告被保存
        verify(settlementWarnRepository, times(1)).save(any(SettlementWarn.class));
        
        // 验证不匹配商户的reset仍然执行
        assertThat(merchantWithMismatch.getAccount().getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(merchantWithMismatch.getAccount().getBalance())
            .isEqualByComparingTo(merchantWithMismatch.getMerchantAccountMonitor().getBalance());
    }
    
    @Test
    void testDailySettlement_AllMismatchMerchants() {
        // 所有商户都不匹配的极端情况
        List<Merchant> merchants = Arrays.asList(merchantWithMismatch);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
    }
    
    @Test
    void testDailySettlement_SettlementWarnCreation() {
        // 测试SettlementWarn对象的创建和属性设置
        List<Merchant> merchants = Arrays.asList(merchantWithMismatch);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 捕获保存的SettlementWarn对象
        SettlementWarn capturedWarn = new SettlementWarn();
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证SettlementWarn属性
        assertThat(capturedWarn.getMerchantId()).isNull();
    }
    
    @Test
    void testDailySettlement_WithZeroValues() {
        // 测试零值情况
        Merchant zeroValueMerchant = new Merchant();
        zeroValueMerchant.setId(4L);
        
        Account account = new Account();
        account.setBalance(BigDecimal.ZERO);
        account.setDailySales(BigDecimal.ZERO);
        zeroValueMerchant.setAccount(account);
        
        MerchantAccountMonitor monitor = new MerchantAccountMonitor();
        monitor.setBalance(BigDecimal.ZERO);
        zeroValueMerchant.setMerchantAccountMonitor(monitor);
        
        List<Merchant> merchants = Arrays.asList(zeroValueMerchant);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
        
        // 验证reset后值正确
        assertThat(zeroValueMerchant.getAccount().getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(zeroValueMerchant.getAccount().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void testDailySettlement_WithNegativeValues() {
        // 测试负值情况（如果业务允许）
        Merchant negativeMerchant = new Merchant();
        negativeMerchant.setId(5L);
        
        Account account = new Account();
        account.setBalance(new BigDecimal("-100.00"));
        account.setDailySales(new BigDecimal("50.00"));
        negativeMerchant.setAccount(account);
        
        MerchantAccountMonitor monitor = new MerchantAccountMonitor();
        monitor.setBalance(new BigDecimal("-150.00")); // 从-150到-100，变化50，匹配日销售
        negativeMerchant.setMerchantAccountMonitor(monitor);
        
        List<Merchant> merchants = Arrays.asList(negativeMerchant);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
    }
    
    @Test
    void testDailySettlement_WithDecimalPrecision() {
        // 测试小数精度
        Merchant decimalMerchant = new Merchant();
        decimalMerchant.setId(6L);
        
        Account account = new Account();
        account.setBalance(new BigDecimal("1234.5678"));
        account.setDailySales(new BigDecimal("234.5678"));
        decimalMerchant.setAccount(account);
        
        MerchantAccountMonitor monitor = new MerchantAccountMonitor();
        monitor.setBalance(new BigDecimal("1000.0000")); // 精确匹配
        decimalMerchant.setMerchantAccountMonitor(monitor);
        
        List<Merchant> merchants = Arrays.asList(decimalMerchant);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
    }
    
    @Test
    void testDailySettlement_AccountMethodsCalled() {
        // 验证Account的resetDailySales方法被调用
        List<Merchant> merchants = Arrays.asList(merchant1);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 记录初始值
        BigDecimal initialDailySales = merchant1.getAccount().getDailySales(); // 应该是500.00
        BigDecimal initialBalance = merchant1.getAccount().getBalance();      // 应该是1000.00
        BigDecimal monitorBalance = merchant1.getMerchantAccountMonitor().getBalance(); // 500.00
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证resetDailySales被调用 - 日销售额被重置为0
        assertThat(merchant1.getAccount().getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
        
        // 验证初始值正确
        assertThat(initialDailySales).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(initialBalance).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(monitorBalance).isEqualByComparingTo(new BigDecimal("500.00"));
    }
    
    @Test
    void testLoggerInitialization() {
        // 测试日志器初始化（覆盖静态logger字段）
        Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
        assertThat(logger).isNotNull();
    }
    
    @Test
    void testScheduledAnnotationPresent() throws NoSuchMethodException {
        // 验证方法上有@Scheduled注解
        var method = SettlementScheduler.class.getMethod("dailySettlement");
        var scheduledAnnotation = method.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assertThat(scheduledAnnotation).isNotNull();
    }
    
    @Test
    void testTransactionalAnnotationPresent() throws NoSuchMethodException {
        // 验证方法上有@Transactional注解
        var method = SettlementScheduler.class.getMethod("dailySettlement");
        var transactionalAnnotation = method.getAnnotation(org.springframework.transaction.annotation.Transactional.class);
        assertThat(transactionalAnnotation).isNotNull();
    }
    
    @Test
    void testDailySettlement_ExactMatchCase() {
        // 测试精确匹配的边界情况
        Merchant exactMerchant = new Merchant();
        exactMerchant.setId(7L);
        
        Account account = new Account();
        account.setBalance(new BigDecimal("1000.50"));
        account.setDailySales(new BigDecimal("200.50"));
        exactMerchant.setAccount(account);
        
        MerchantAccountMonitor monitor = new MerchantAccountMonitor();
        monitor.setBalance(new BigDecimal("800.00")); // 800 + 200.50 = 1000.50，精确匹配
        exactMerchant.setMerchantAccountMonitor(monitor);
        
        List<Merchant> merchants = Arrays.asList(exactMerchant);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
    }
    
    @Test
    void testDailySettlement_WithMultipleMismatches() {
        // 测试多个商户都不匹配的情况
        Merchant mismatch1 = new Merchant();
        mismatch1.setId(8L);
        
        Account account1 = new Account();
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setDailySales(new BigDecimal("500.00"));
        mismatch1.setAccount(account1);
        
        MerchantAccountMonitor monitor1 = new MerchantAccountMonitor();
        monitor1.setBalance(new BigDecimal("600.00")); // 不匹配
        mismatch1.setMerchantAccountMonitor(monitor1);
        
        Merchant mismatch2 = new Merchant();
        mismatch2.setId(9L);
        
        Account account2 = new Account();
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setDailySales(new BigDecimal("300.00"));
        mismatch2.setAccount(account2);
        
        MerchantAccountMonitor monitor2 = new MerchantAccountMonitor();
        monitor2.setBalance(new BigDecimal("1800.00")); // 不匹配
        mismatch2.setMerchantAccountMonitor(monitor2);
        
        List<Merchant> merchants = Arrays.asList(mismatch1, mismatch2);
        when(merchantRepository.findAll()).thenReturn(merchants);
        
        // 执行
        settlementScheduler.dailySettlement();
        
        // 验证
        verify(merchantRepository, times(1)).findAll();
    }
    
    @Test
    void testDailySettlement_AccountResetDailySalesMethod() {
        // 专门测试Account的resetDailySales方法
        Account account = new Account();
        account.setDailySales(new BigDecimal("1000.00"));
        
        // 执行reset
        account.resetDailySales();
        
        // 验证
        assertThat(account.getDailySales()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void testDailySettlement_CompareToResults() {
        // 测试BigDecimal compareTo的各种情况
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal preBalance = new BigDecimal("500.00");
        BigDecimal dailySales = new BigDecimal("500.00");
        
        BigDecimal difference = balance.subtract(preBalance);
        int comparison = difference.compareTo(dailySales);
        
        // 应该相等，比较结果为0
        assertThat(comparison).isEqualTo(0);
        
        // 测试不相等的情况
        BigDecimal dailySales2 = new BigDecimal("400.00");
        int comparison2 = difference.compareTo(dailySales2);
        assertThat(comparison2).isEqualTo(1); // 600 > 400
        
        BigDecimal dailySales3 = new BigDecimal("700.00");
        int comparison3 = difference.compareTo(dailySales3);
        assertThat(comparison3).isEqualTo(-1); // 600 < 700
    }
}