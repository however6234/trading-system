package com.capital.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capital.domain.shared.Account;
import com.capital.domain.user.User;
import com.capital.enums.AccountType;
import com.capital.exception.TradingException;
import com.capital.repository.AccountRepository;
import com.capital.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Account testAccount;
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USER_NAME = "testUser";
    private final String TEST_EMAIL = "test@example.com";
    private final BigDecimal TEST_AMOUNT = new BigDecimal("1000.00");
    private final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;
    private final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");
    private final BigDecimal SMALL_AMOUNT = new BigDecimal("0.01");

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setCurrency("CNY");
        testAccount.setAccountType(AccountType.User.toString());
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setActive(true);
        testAccount.setDailySales(BigDecimal.ZERO);

        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUserName(TEST_USER_NAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setAccountId(testAccount.getId());
        testUser.setAccount(testAccount);
        testUser.setActive(true);
    }

    @Test
    void recharge_Success() throws TradingException {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        BigDecimal originalBalance = testAccount.getBalance();

        // Act
        Account result = userService.recharge(TEST_USER_ID, TEST_AMOUNT);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_AMOUNT.add(originalBalance), result.getBalance());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository).save(testUser);
    }

    @Test
    void recharge_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(null);

        // Act & Assert
        TradingException exception = assertThrows(TradingException.class, 
                () -> userService.recharge(TEST_USER_ID, TEST_AMOUNT));
        
        assertNotNull(exception.getCode());
        assertNotNull(exception.getMessage());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recharge_UserHasNoAccount_ShouldThrowIllegalStateException() {
        // Arrange
        testUser.setAccount(null);
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.recharge(TEST_USER_ID, TEST_AMOUNT));
        
        assertEquals("User has no associated account", exception.getMessage());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recharge_WithZeroAmount_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);

        // Act & Assert - Account.recharge()会抛出IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.recharge(TEST_USER_ID, ZERO_AMOUNT));
        
        assertEquals("Recharge amount must be positive", exception.getMessage());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recharge_WithNegativeAmount_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.recharge(TEST_USER_ID, NEGATIVE_AMOUNT));
        
        assertEquals("Recharge amount must be positive", exception.getMessage());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recharge_WithSmallPositiveAmount_Success() throws TradingException {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Account result = userService.recharge(TEST_USER_ID, SMALL_AMOUNT);

        // Assert
        assertNotNull(result);
        assertEquals(SMALL_AMOUNT, result.getBalance());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository).save(testUser);
    }

    @Test
    void recharge_UserInactive_ShouldThrowException() {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(null);

        // Act & Assert
        TradingException exception = assertThrows(TradingException.class, 
                () -> userService.recharge(TEST_USER_ID, TEST_AMOUNT));
        
        assertNotNull(exception.getCode());
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_Success() throws TradingException {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        // 模拟account保存
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setCurrency("CNY");
        savedAccount.setAccountType(AccountType.User.toString());
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setActive(true);
        savedAccount.setDailySales(BigDecimal.ZERO);
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 模拟user保存
        User savedUser = new User();
        savedUser.setId(TEST_USER_ID);
        savedUser.setUserName(TEST_USER_NAME);
        savedUser.setEmail(TEST_EMAIL);
        savedUser.setAccountId(savedAccount.getId());
        savedUser.setAccount(savedAccount);
        savedUser.setActive(true);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(TEST_USER_NAME, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_NAME, result.getUserName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertTrue(result.isActive());
        assertNotNull(result.getAccount());
        assertEquals("CNY", result.getAccount().getCurrency());
        assertEquals(AccountType.User.toString(), result.getAccount().getAccountType());
        assertEquals(BigDecimal.ZERO, result.getAccount().getBalance());
        assertTrue(result.getAccount().isActive());
        assertEquals(BigDecimal.ZERO, result.getAccount().getDailySales());
        
        verify(userRepository).existsByUserName(TEST_USER_NAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        
        // 验证account的保存参数
        verify(accountRepository).save(argThat(account -> 
            "CNY".equals(account.getCurrency()) &&
            AccountType.User.toString().equals(account.getAccountType()) &&
            BigDecimal.ZERO.compareTo(account.getBalance()) == 0 &&
            account.isActive() &&
            BigDecimal.ZERO.compareTo(account.getDailySales()) == 0
        ));
        
        verify(userRepository).save(argThat(user ->
            TEST_USER_NAME.equals(user.getUserName()) &&
            TEST_EMAIL.equals(user.getEmail()) &&
            user.isActive() &&
            user.getAccount() != null
        ));
    }

    @Test
    void createUser_UserNameAlreadyExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(true);

        // Act & Assert
        TradingException exception = assertThrows(TradingException.class,
                () -> userService.createUser(TEST_USER_NAME, TEST_EMAIL));
        
        assertNotNull(exception.getCode());
        verify(userRepository).existsByUserName(TEST_USER_NAME);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(accountRepository, never()).save(any(Account.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(true);

        // Act & Assert
        TradingException exception = assertThrows(TradingException.class,
                () -> userService.createUser(TEST_USER_NAME, TEST_EMAIL));
        
        assertNotNull(exception.getCode());
        verify(userRepository).existsByUserName(TEST_USER_NAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(accountRepository, never()).save(any(Account.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyUserName() throws TradingException {
        // Arrange
        String emptyUserName = "";
        when(userRepository.existsByUserName(eq(emptyUserName))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setCurrency("CNY");
        savedAccount.setAccountType(AccountType.User.toString());
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setActive(true);
        savedAccount.setDailySales(BigDecimal.ZERO);
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        User savedUser = new User();
        savedUser.setUserName(emptyUserName);
        savedUser.setEmail(TEST_EMAIL);
        savedUser.setAccount(savedAccount);
        savedUser.setActive(true);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(emptyUserName, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(emptyUserName, result.getUserName());
        verify(userRepository).existsByUserName(emptyUserName);
    }

    @Test
    void createUser_AccountSaveFailed_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(TEST_USER_NAME, TEST_EMAIL));
        
        assertEquals("Database error", exception.getMessage());
        verify(userRepository).existsByUserName(TEST_USER_NAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(accountRepository).save(any(Account.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_UserSaveFailed_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setCurrency("CNY");
        savedAccount.setAccountType(AccountType.User.toString());
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setActive(true);
        savedAccount.setDailySales(BigDecimal.ZERO);
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(TEST_USER_NAME, TEST_EMAIL));
        
        assertEquals("Database error", exception.getMessage());
        verify(userRepository).existsByUserName(TEST_USER_NAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(accountRepository).save(any(Account.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void recharge_NullUserId_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class,
                () -> userService.recharge(null, TEST_AMOUNT));
        assertNotNull(exception);
    }

    @Test
    void recharge_NullAmount_ShouldThrowException() {
        // Arrange
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);

        // Act & Assert
        Exception exception = assertThrows(Exception.class,
                () -> userService.recharge(TEST_USER_ID, null));
        assertNotNull(exception);
    }

    @Test
    void recharge_VerifyTransactionRollback() {
        // Arrange - 模拟在保存时发生异常
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.recharge(TEST_USER_ID, TEST_AMOUNT));
        
        assertEquals("Database error", exception.getMessage());
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository).save(testUser);
    }

    @Test
    void recharge_LargeAmount_Success() throws TradingException {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("999999999.99");
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Account result = userService.recharge(TEST_USER_ID, largeAmount);

        // Assert
        assertNotNull(result);
        assertEquals(largeAmount, result.getBalance());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository).save(testUser);
    }
    @Test
    void createUser_VerifyAccountAndUserRelationship() throws TradingException {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        // 模拟Account保存 - 创建一个新的Account对象
        Account mockAccount = new Account();
        mockAccount.setId(100L);
        mockAccount.setCurrency("CNY");
        mockAccount.setAccountType(AccountType.User.toString());
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setActive(true);
        mockAccount.setDailySales(BigDecimal.ZERO);
        
        // 关键：确保accountRepository.save返回正确的对象
        when(accountRepository.save(any(Account.class)))
            .thenReturn(mockAccount);
        
        // 模拟User保存 - 创建一个新的User对象
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setUserName(TEST_USER_NAME);
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setAccountId(mockAccount.getId());
        mockUser.setAccount(mockAccount);
        mockUser.setActive(true);
        
        // 关键：确保userRepository.save返回正确的对象
        when(userRepository.save(any(User.class)))
            .thenReturn(mockUser);

        // Act
        User result = userService.createUser(TEST_USER_NAME, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccount());
        assertTrue(result.isActive());
        assertTrue(result.getAccount().isActive());
        assertEquals(BigDecimal.ZERO, result.getAccount().getDailySales());
    }

    @Test
    void createUser_AccountInitialization() throws TradingException {
        // Arrange
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        when(accountRepository.save(argThat(account ->
            "CNY".equals(account.getCurrency()) &&
            AccountType.User.toString().equals(account.getAccountType()) &&
            BigDecimal.ZERO.compareTo(account.getBalance()) == 0 &&
            account.isActive() &&
            BigDecimal.ZERO.compareTo(account.getDailySales()) == 0
        ))).thenReturn(testAccount);
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(TEST_USER_NAME, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testAccountDeductMethod() {
        // 这个测试验证Account的deduct方法
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        
        // 测试正常扣款
        boolean success = account.deduct(new BigDecimal("50.00"));
        assertTrue(success);
        assertEquals(new BigDecimal("50.00"), account.getBalance());
        
        // 测试扣款失败（余额不足）
        success = account.deduct(new BigDecimal("100.00"));
        assertFalse(success);
        assertEquals(new BigDecimal("50.00"), account.getBalance());
        
        // 测试非法金额
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> account.deduct(BigDecimal.ZERO));
        assertEquals("Deduct amount must be positive", exception.getMessage());
    }

    @Test
    void testAccountAddBalanceMethod() {
        // 测试Account的addBalance方法
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        account.setDailySales(new BigDecimal("50.00"));
        
        // 正常添加余额
        account.addBalance(new BigDecimal("30.00"));
        assertEquals(new BigDecimal("130.00"), account.getBalance());
        assertEquals(new BigDecimal("80.00"), account.getDailySales());
        
        // 测试非法金额
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> account.addBalance(BigDecimal.ZERO));
        assertEquals("Balance amount must be positive", exception.getMessage());
    }

    @Test
    void testAccountResetDailySalesMethod() {
        // 测试重置每日销售额
        Account account = new Account();
        account.setDailySales(new BigDecimal("1000.50"));
        
        account.resetDailySales();
        assertEquals(BigDecimal.ZERO, account.getDailySales());
    }

    @Test
    void recharge_ExistingBalance_Success() throws TradingException {
        // Arrange
        BigDecimal existingBalance = new BigDecimal("500.00");
        testAccount.setBalance(existingBalance);
        testUser.setAccount(testAccount);
        
        when(userRepository.findByIdAndActive(eq(TEST_USER_ID), eq(true)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Account result = userService.recharge(TEST_USER_ID, TEST_AMOUNT);

        // Assert
        assertNotNull(result);
        assertEquals(existingBalance.add(TEST_AMOUNT), result.getBalance());
        
        verify(userRepository).findByIdAndActive(TEST_USER_ID, true);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_EmailWithDifferentCase_ShouldBeTreatedAsDifferent() throws TradingException {
        // Arrange - 测试大小写不同的邮箱
        String upperCaseEmail = TEST_EMAIL.toUpperCase();
        when(userRepository.existsByUserName(eq(TEST_USER_NAME))).thenReturn(false);
        when(userRepository.existsByEmail(eq(upperCaseEmail))).thenReturn(false);
        
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setCurrency("CNY");
        savedAccount.setAccountType(AccountType.User.toString());
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setActive(true);
        savedAccount.setDailySales(BigDecimal.ZERO);
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        User savedUser = new User();
        savedUser.setUserName(TEST_USER_NAME);
        savedUser.setEmail(upperCaseEmail);
        savedUser.setAccount(savedAccount);
        savedUser.setActive(true);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(TEST_USER_NAME, upperCaseEmail);

        // Assert
        assertNotNull(result);
        assertEquals(upperCaseEmail, result.getEmail());
        verify(userRepository).existsByEmail(upperCaseEmail);
    }

    @Test
    void createUser_WithSpecialCharactersInUsername() throws TradingException {
        // Arrange - 测试特殊字符的用户名
        String specialUsername = "user_name-123.test";
        when(userRepository.existsByUserName(eq(specialUsername))).thenReturn(false);
        when(userRepository.existsByEmail(eq(TEST_EMAIL))).thenReturn(false);
        
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setCurrency("CNY");
        savedAccount.setAccountType(AccountType.User.toString());
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setActive(true);
        savedAccount.setDailySales(BigDecimal.ZERO);
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        User savedUser = new User();
        savedUser.setUserName(specialUsername);
        savedUser.setEmail(TEST_EMAIL);
        savedUser.setAccount(savedAccount);
        savedUser.setActive(true);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(specialUsername, TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(specialUsername, result.getUserName());
        verify(userRepository).existsByUserName(specialUsername);
    }
}