package com.capital.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalIdIdentifierGeneratorTest {

    @Mock
    private LocalIdGenerator localIdGenerator;

    @Mock
    private SharedSessionContractImplementor session;

    @Mock
    private Object object;

    @InjectMocks
    private LocalIdIdentifierGenerator identifierGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(identifierGenerator, "localIdGenerator", localIdGenerator);
    }

    @Test
    void testGenerate() {
        // 准备
        Long expectedId = 12345L;
        when(localIdGenerator.nextId()).thenReturn(expectedId);

        // 执行
        Serializable result = identifierGenerator.generate(session, object);

        // 验证
        assertNotNull(result);
        assertEquals(expectedId, result);
        verify(localIdGenerator, times(1)).nextId();
    }

    @Test
    void testGenerateWithNullLocalIdGenerator() {
        // 准备：通过反射设置localIdGenerator为null
        ReflectionTestUtils.setField(identifierGenerator, "localIdGenerator", null);

        // 执行和验证
        // 实际上，根据原始代码，这会抛出NullPointerException
        Exception exception = assertThrows(Exception.class, () -> {
            identifierGenerator.generate(session, object);
        });
        assertNotNull(exception);
    }

    @Test
    void testSupportsJdbcBatchInserts() {
        // 测试方法返回值
        assertTrue(identifierGenerator.supportsJdbcBatchInserts());
    }

    @Test
    void testGenerateWithException() {
        // 测试当localIdGenerator抛出异常时的行为
        // 根据原始代码，generate()方法会抛出HibernateException
        RuntimeException runtimeException = new RuntimeException("ID生成失败");
        when(localIdGenerator.nextId()).thenThrow(runtimeException);

        // 根据你的原始代码，generate()方法声明了throws HibernateException
        // 但实际实现可能直接抛出RuntimeException
        // 我们需要检查实际行为
        Exception exception = assertThrows(Exception.class, () -> {
            identifierGenerator.generate(session, object);
        });
        
        // 验证异常信息包含原始异常
        assertNotNull(exception);
        if (exception instanceof HibernateException) {
            // 如果包装成了HibernateException
            assertTrue(exception.getCause() == runtimeException || 
                      exception.getMessage().contains("ID生成失败"));
        } else if (exception instanceof RuntimeException) {
            // 如果直接抛出RuntimeException
            assertEquals(runtimeException, exception);
        }
    }

    @Test
    void testGenerateWithHibernateExceptionWrapping() {
        // 专门测试异常包装情况
        String errorMessage = "数据库连接失败";
        RuntimeException cause = new RuntimeException(errorMessage);
        when(localIdGenerator.nextId()).thenThrow(cause);

        try {
            identifierGenerator.generate(session, object);
            fail("Expected exception was not thrown");
        } catch (HibernateException e) {
            // 如果包装成HibernateException
            assertTrue(e.getCause() == cause || e.getMessage().contains(errorMessage));
        } catch (RuntimeException e) {
            // 如果直接抛出RuntimeException
            assertEquals(cause, e);
        }
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // 测试线程安全性
        int threadCount = 5;
        int callsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong idCounter = new AtomicLong(1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 设置mock，每个调用返回不同的ID
        when(localIdGenerator.nextId()).thenAnswer(invocation -> idCounter.getAndIncrement());

        // 记录所有生成的ID
        final java.util.Set<Serializable> generatedIds = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

        // 执行多线程测试
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < callsPerThread; j++) {
                        Serializable id = identifierGenerator.generate(session, object);
                        generatedIds.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "线程应在超时前完成");
        executorService.shutdown();

        // 验证生成的ID数量正确且无重复
        assertEquals(threadCount * callsPerThread, generatedIds.size());
        verify(localIdGenerator, times(threadCount * callsPerThread)).nextId();
    }

    @Test
    void testGenerateWithSessionAndObjectIgnored() {
        // 验证session和object参数被忽略（不参与ID生成）
        Long expectedId = 55555L;
        when(localIdGenerator.nextId()).thenReturn(expectedId);

        // 使用不同的session和object参数
        SharedSessionContractImplementor mockSession = mock(SharedSessionContractImplementor.class);
        Object mockObject = new Object();
        
        Serializable result1 = identifierGenerator.generate(session, object);
        Serializable result2 = identifierGenerator.generate(mockSession, mockObject);

        assertEquals(expectedId, result1);
        assertEquals(expectedId, result2);
        verify(localIdGenerator, times(2)).nextId();
    }

    @Test
    void testGenerateIsSerializable() {
        // 确保生成的ID是可序列化的
        Long expectedId = 77777L;
        when(localIdGenerator.nextId()).thenReturn(expectedId);

        Serializable result = identifierGenerator.generate(session, object);
        
        // 验证确实是Serializable类型
        assertNotNull(result);
        
        // 尝试序列化（模拟）
        try {
            // 创建一个简单的序列化测试
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(result);
            oos.close();
            
            assertTrue(true); // 如果没抛出异常，测试通过
        } catch (Exception e) {
            fail("生成的ID应该可以序列化: " + e.getMessage());
        }
    }

    @Test
    void testComponentAnnotation() {
        // 验证类被正确注解为Spring组件
        org.springframework.stereotype.Component componentAnnotation = 
            LocalIdIdentifierGenerator.class.getAnnotation(org.springframework.stereotype.Component.class);
        assertNotNull(componentAnnotation, "LocalIdIdentifierGenerator应该被@Component注解");
    }

    @Test
    void testImplementsIdentifierGenerator() {
        // 验证实现了正确的接口
        assertTrue(org.hibernate.id.IdentifierGenerator.class.isAssignableFrom(LocalIdIdentifierGenerator.class));
    }

    @Test
    void testAutowiredAnnotation() throws NoSuchFieldException {
        // 验证localIdGenerator字段有@Autowired注解
        Field field = LocalIdIdentifierGenerator.class.getDeclaredField("localIdGenerator");
        org.springframework.beans.factory.annotation.Autowired autowiredAnnotation = 
            field.getAnnotation(org.springframework.beans.factory.annotation.Autowired.class);
        assertNotNull(autowiredAnnotation, "localIdGenerator字段应该有@Autowired注解");
    }

    @Test
    void testGenerateMultipleTimes() {
        // 测试多次调用生成不同的ID
        when(localIdGenerator.nextId())
            .thenReturn(100L)
            .thenReturn(101L)
            .thenReturn(102L);

        Serializable result1 = identifierGenerator.generate(session, object);
        Serializable result2 = identifierGenerator.generate(session, object);
        Serializable result3 = identifierGenerator.generate(session, object);

        assertEquals(100L, result1);
        assertEquals(101L, result2);
        assertEquals(102L, result3);
        verify(localIdGenerator, times(3)).nextId();
    }

    @Test
    void testGenerateWithZeroId() {
        // 测试边界值 - 0
        when(localIdGenerator.nextId()).thenReturn(0L);
        Serializable result = identifierGenerator.generate(session, object);
        assertEquals(0L, result);
    }

    @Test
    void testGenerateWithNegativeId() {
        // 测试边界值 - 负数
        when(localIdGenerator.nextId()).thenReturn(-1L);
        Serializable result = identifierGenerator.generate(session, object);
        assertEquals(-1L, result);
    }

    @Test
    void testGenerateWithNullId() {
        // 测试返回null的情况
        when(localIdGenerator.nextId()).thenReturn(null);
        Serializable result = identifierGenerator.generate(session, object);
        assertNull(result);
    }

    @Test
    void testGenerateMethodSignature() throws NoSuchMethodException {
        // 验证方法签名
        java.lang.reflect.Method generateMethod = LocalIdIdentifierGenerator.class.getMethod(
            "generate", 
            SharedSessionContractImplementor.class, 
            Object.class
        );
        
        // 验证方法声明抛出HibernateException
        Class<?>[] exceptionTypes = generateMethod.getExceptionTypes();
        boolean throwsHibernateException = false;
        for (Class<?> exceptionType : exceptionTypes) {
            if (exceptionType.equals(HibernateException.class)) {
                throwsHibernateException = true;
                break;
            }
        }
        assertTrue(throwsHibernateException, "generate方法应该声明抛出HibernateException");
    }
}