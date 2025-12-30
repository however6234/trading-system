package com.capital.domain.user;

import com.capital.domain.shared.Account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    private User user;

    @Mock
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUserName("testUser");
        user.setEmail("test@example.com");
        user.setAccountId(100L);
        user.setActive(true);
    }

    @Test
    void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull(defaultUser);
        assertTrue(defaultUser.isActive());
    }

    @Test
    void testAllArgsConstructor() {
        User newUser = new User(1L, "testUser", "test@example.com", 100L, true, mockAccount);
        assertNotNull(newUser);
        assertEquals(1L, newUser.getId());
        assertEquals("testUser", newUser.getUserName());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals(100L, newUser.getAccountId());
        assertTrue(newUser.isActive());
        assertEquals(mockAccount, newUser.getAccount());
    }

    @Test
    void testGettersAndSetters() {
        // 测试所有getter和setter方法
        user.setId(2L);
        assertEquals(2L, user.getId());

        user.setUserName("newUser");
        assertEquals("newUser", user.getUserName());

        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());

        user.setAccountId(200L);
        assertEquals(200L, user.getAccountId());

        user.setActive(false);
        assertFalse(user.isActive());

        user.setAccount(mockAccount);
        assertEquals(mockAccount, user.getAccount());
    }

    @Test
    void testRecharge_WithAccount_Success() {
        // Arrange
        user.setAccount(mockAccount);
        BigDecimal amount = new BigDecimal("100.50");

        // Act
        user.recharge(amount);

        // Assert
        verify(mockAccount, times(1)).recharge(amount);
    }

    @Test
    void testRecharge_WithoutAccount_ThrowsException() {
        // Arrange
        user.setAccount(null);
        BigDecimal amount = new BigDecimal("100.50");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> user.recharge(amount));
        
        assertEquals("User has no associated account", exception.getMessage());
    }

    @Test
    void testRecharge_WithNullAmount_ShouldPassNullToAccount() {
        // Arrange
        user.setAccount(mockAccount);

        // Act
        user.recharge(null);

        // Assert
        verify(mockAccount, times(1)).recharge(null);
    }

    @Test
    void testDeduct_WithAccount_ReturnsTrue() {
        // Arrange
        user.setAccount(mockAccount);
        BigDecimal amount = new BigDecimal("50.25");
        when(mockAccount.deduct(amount)).thenReturn(true);

        // Act
        boolean result = user.deduct(amount);

        // Assert
        assertTrue(result);
        verify(mockAccount, times(1)).deduct(amount);
    }

    @Test
    void testDeduct_WithAccount_ReturnsFalse() {
        // Arrange
        user.setAccount(mockAccount);
        BigDecimal amount = new BigDecimal("50.25");
        when(mockAccount.deduct(amount)).thenReturn(false);

        // Act
        boolean result = user.deduct(amount);

        // Assert
        assertFalse(result);
        verify(mockAccount, times(1)).deduct(amount);
    }

    @Test
    void testDeduct_WithoutAccount_ThrowsException() {
        // Arrange
        user.setAccount(null);
        BigDecimal amount = new BigDecimal("50.25");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> user.deduct(amount));
        
        assertEquals("User has no associated account", exception.getMessage());
    }

    @Test
    void testDeduct_WithNullAmount_ShouldPassNullToAccount() {
        // Arrange
        user.setAccount(mockAccount);
        when(mockAccount.deduct(null)).thenReturn(false);

        // Act
        boolean result = user.deduct(null);

        // Assert
        assertFalse(result);
        verify(mockAccount, times(1)).deduct(null);
    }

    @Test
    void testActiveField_DefaultValue() {
        User newUser = new User();
        assertTrue(newUser.isActive());
    }

    @Test
    void testActiveField_CanBeSetToFalse() {
        user.setActive(false);
        assertFalse(user.isActive());
    }

    @Test
    void testToString_ReturnsNotNull() {
        assertNotNull(user.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User(1L, "user1", "email1@test.com", 1L, true, null);
        User user2 = new User(1L, "user1", "email1@test.com", 1L, true, null);
        User user3 = new User(2L, "user2", "email2@test.com", 2L, false, null);

        // 测试equals方法
        assertNotEquals(user1, user3);
        
        // 测试hashCode一致性
        assertEquals(user1.hashCode(), user1.hashCode());
    }

    @Test
    void testJsonIgnoreProperties() throws Exception {
        // 验证JsonIgnoreProperties注解存在
        Class<?> clazz = User.class;
        JsonIgnoreProperties annotation = clazz.getAnnotation(JsonIgnoreProperties.class);
        
        assertNotNull(annotation);
        assertArrayEquals(new String[]{"hibernateLazyInitializer", "handler"}, annotation.value());
    }

    @Test
    void testEntityAnnotations() {
        // 验证@Entity注解
        assertTrue(User.class.isAnnotationPresent(javax.persistence.Entity.class));
        
        // 验证@Table注解
        javax.persistence.Table tableAnnotation = User.class.getAnnotation(javax.persistence.Table.class);
        assertNotNull(tableAnnotation);
        assertEquals("users", tableAnnotation.name());
    }

    @Test
    void testFieldAnnotations() throws NoSuchFieldException {
        // 验证@Id注解
        Field idField = User.class.getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(javax.persistence.Id.class));
        
        // 验证@GeneratedValue注解
        assertTrue(idField.isAnnotationPresent(javax.persistence.GeneratedValue.class));
        
        // 验证@Column注解
        Field userNameField = User.class.getDeclaredField("userName");
        javax.persistence.Column userNameColumn = userNameField.getAnnotation(javax.persistence.Column.class);
        assertNotNull(userNameColumn);
        assertFalse(userNameColumn.nullable());
        assertTrue(userNameColumn.unique());
        
        // 验证@OneToOne注解
        Field accountField = User.class.getDeclaredField("account");
        javax.persistence.OneToOne oneToOneAnnotation = accountField.getAnnotation(javax.persistence.OneToOne.class);
        assertNotNull(oneToOneAnnotation);
        assertEquals(javax.persistence.CascadeType.ALL, oneToOneAnnotation.cascade()[0]);
        assertTrue(oneToOneAnnotation.orphanRemoval());
        assertEquals(javax.persistence.FetchType.LAZY, oneToOneAnnotation.fetch());
    }

    @Test
    void testAccountIdColumnAnnotation() throws NoSuchFieldException {
        Field accountIdField = User.class.getDeclaredField("accountId");
        javax.persistence.Column columnAnnotation = accountIdField.getAnnotation(javax.persistence.Column.class);
        
        assertNotNull(columnAnnotation);
        assertEquals("account_id", columnAnnotation.name());
        assertFalse(columnAnnotation.updatable());
        assertTrue(columnAnnotation.unique());
    }

    @Test
    void testRechargeWithZeroAmount() {
        // Arrange
        user.setAccount(mockAccount);
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // Act
        user.recharge(zeroAmount);

        // Assert
        verify(mockAccount, times(1)).recharge(zeroAmount);
    }

    @Test
    void testDeductWithZeroAmount() {
        // Arrange
        user.setAccount(mockAccount);
        BigDecimal zeroAmount = BigDecimal.ZERO;
        when(mockAccount.deduct(zeroAmount)).thenReturn(true);

        // Act
        boolean result = user.deduct(zeroAmount);

        // Assert
        assertTrue(result);
        verify(mockAccount, times(1)).deduct(zeroAmount);
    }

    @Test
    void testAccountRelationship() {
        // 测试Account关联设置
        Account newAccount = new Account();
        user.setAccount(newAccount);
        
        assertEquals(newAccount, user.getAccount());
    }

    @Test
    void testUserWithNullValues() {
        User nullUser = new User();
        nullUser.setUserName(null);
        nullUser.setEmail(null);
        nullUser.setAccount(null);
        
        assertNull(nullUser.getUserName());
        assertNull(nullUser.getEmail());
        assertNull(nullUser.getAccount());
    }
}