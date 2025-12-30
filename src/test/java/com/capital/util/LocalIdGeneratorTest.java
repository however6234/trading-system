package com.capital.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocalIdGeneratorTest {

    private LocalIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        idGenerator = new LocalIdGenerator();
        idGenerator.setStartId(10000L);
    }

    @Test
    void testNextId() {
        Long id1 = idGenerator.nextId();
        assertEquals(10000L, id1);

        Long id2 = idGenerator.nextId();
        assertEquals(10001L, id2);
    }

    @Test
    void testNextIdWithStartIdBoundary() {
        idGenerator.setStartId(5L);
        
        Long id1 = idGenerator.nextId();
        assertEquals(5L, id1);
        
        Long id2 = idGenerator.nextId();
        assertEquals(6L, id2);
    }

    @Test
    void testNextIdWithBusinessType() {
        Long id1 = idGenerator.nextId("ORDER");
        assertEquals(10001L, id1);

        Long id2 = idGenerator.nextId("ORDER");
        assertEquals(10002L, id2);

        Long id3 = idGenerator.nextId("USER");
        assertEquals(10001L, id3);
    }

    @Test
    void testNextIdWithPrefix() {
        String id1 = idGenerator.nextIdWithPrefix("ORDER", "ORD_");
        assertEquals("ORD_10001", id1);

        String id2 = idGenerator.nextIdWithPrefix("ORDER", "ORD_");
        assertEquals("ORD_10002", id2);

        String id3 = idGenerator.nextIdWithPrefix("USER", "USR_");
        assertEquals("USR_10001", id3);
    }

    @Test
    void testNextIds() {
        Long[] ids = idGenerator.nextIds(3);
        
        assertNotNull(ids);
        assertEquals(3, ids.length);
        assertEquals(10000L, ids[0]);
        assertEquals(10001L, ids[1]);
        assertEquals(10002L, ids[2]);

        Long[] moreIds = idGenerator.nextIds(2);
        assertEquals(10003L, moreIds[0]);
        assertEquals(10004L, moreIds[1]);
    }

    @Test
    void testNextIdsWithZeroCount() {
        Long[] ids = idGenerator.nextIds(0);
        assertNotNull(ids);
        assertEquals(0, ids.length);
    }

    @Test
    void testNextIdsWithNegativeCount() {
        assertThrows(NegativeArraySizeException.class, () -> {
            idGenerator.nextIds(-1);
        });
    }

    @Test
    void testGetType() {
        String type = idGenerator.getType();
        assertEquals("LOCAL", type);
    }

    @Test
    void testSetStartId() {
        idGenerator.setStartId(50000L);
        
        Long id1 = idGenerator.nextId();
        assertEquals(50000L, id1);
        
        Long id2 = idGenerator.nextId("ORDER");
        assertEquals(50001L, id2);
    }

    @Test
    void testSetStartIdLowerThanCurrent() {
        idGenerator.nextId();
        idGenerator.nextId();
        
        idGenerator.setStartId(5000L);
        
        Long id = idGenerator.nextId();
        assertEquals(5000L, id);
    }

    @Test
    void testReset() {
        idGenerator.nextId();
        idGenerator.nextId("ORDER");
        idGenerator.nextId("USER");
        
        idGenerator.reset();
        
        Long id1 = idGenerator.nextId();
        assertEquals(10000L, id1);
        
        Long id2 = idGenerator.nextId("ORDER");
        assertEquals(10001L, id2);
        
        Long id3 = idGenerator.nextId("USER");
        assertEquals(10001L, id3);
    }

    @Test
    void testResetSpecificBusinessType() {
        idGenerator.nextId("ORDER");
        idGenerator.nextId("ORDER");
        
        idGenerator.reset("ORDER");
        
        Long id = idGenerator.nextId("ORDER");
        assertEquals(10001L, id);
        
        Long userFirstId = idGenerator.nextId("USER");
        assertEquals(10001L, userFirstId);
        
        Long userSecondId = idGenerator.nextId("USER");
        assertEquals(10002L, userSecondId);
        
        idGenerator.reset("ORDER");
        Long userThirdId = idGenerator.nextId("USER");
        assertEquals(10003L, userThirdId);
    }

    @Test
    void testGetCurrentId() {
        assertEquals(9999L, idGenerator.getCurrentId());
        
        idGenerator.nextId();
        assertEquals(10000L, idGenerator.getCurrentId());
        
        idGenerator.nextId();
        idGenerator.nextId();
        assertEquals(10002L, idGenerator.getCurrentId());
    }

    @Test
    void testGetCurrentIdWithBusinessType() {
        assertEquals(9999L, idGenerator.getCurrentId("ORDER"));
        
        idGenerator.nextId("ORDER");
        assertEquals(10001L, idGenerator.getCurrentId("ORDER"));
        
        assertEquals(9999L, idGenerator.getCurrentId("NON_EXISTENT"));
        
        idGenerator.nextId("USER");
        assertEquals(10001L, idGenerator.getCurrentId("USER"));
        assertEquals(10001L, idGenerator.getCurrentId("ORDER"));
    }

    @Test
    void testEdgeCases() {
        Long id1 = idGenerator.nextId("");
        assertEquals(10001L, id1);
        
        assertThrows(NullPointerException.class, () -> {
            idGenerator.nextId(null);
        });
        
        Long id3 = idGenerator.nextId("  ORDER  ");
        assertEquals(10001L, id3);
        
        Long id4 = idGenerator.nextId("");
        assertEquals(10002L, id4);
    }

    @Test
    void testNextIdWithPrefixEdgeCases() {
        // 每个测试使用不同的业务类型
        String id1 = idGenerator.nextIdWithPrefix("PREFIX_TEST_1", "");
        assertEquals("10001", id1);
        
        String id2 = idGenerator.nextIdWithPrefix("PREFIX_TEST_2", null);
        assertEquals("null10001", id2);
        
        String id3 = idGenerator.nextIdWithPrefix("PREFIX_TEST_3", "#@!");
        assertEquals("#@!10001", id3);
        
        String id4 = idGenerator.nextIdWithPrefix("PREFIX_TEST_4", "PRE_");
        assertEquals("PRE_10001", id4);
        
        String id5 = idGenerator.nextIdWithPrefix("  PREFIX_TEST_5  ", "PRE2_");
        assertEquals("PRE2_10001", id5);
        
        assertThrows(NullPointerException.class, () -> {
            idGenerator.nextIdWithPrefix(null, "PRE_");
        });
    }

    @Test
    void testSetStartIdWithNegativeValue() {
        idGenerator.setStartId(-100L);
        
        Long id = idGenerator.nextId();
        assertEquals(-100L, id);
    }

    @Test
    void testSetStartIdWithZero() {
        idGenerator.setStartId(0L);
        
        Long id = idGenerator.nextId();
        assertEquals(0L, id);
    }

    @Test
    void testResetNonExistentBusinessType() {
        assertDoesNotThrow(() -> idGenerator.reset("NON_EXISTENT"));
    }

    @Test
    void testMultipleSetStartId() {
        idGenerator.setStartId(20000L);
        Long id1 = idGenerator.nextId();
        assertEquals(20000L, id1);
        
        idGenerator.setStartId(30000L);
        Long id2 = idGenerator.nextId();
        assertEquals(30000L, id2);
        
        idGenerator.setStartId(40000L);
        Long id3 = idGenerator.nextId();
        assertEquals(40000L, id3);
    }

    @Test
    void testConstructorAndInitialState() {
        LocalIdGenerator newGen = new LocalIdGenerator();
        
        assertEquals(1L, newGen.getCurrentId());
        
        Long id = newGen.nextId();
        assertEquals(10000L, id);
        
        assertEquals(10000L, newGen.getCurrentId());
        
        Long id2 = newGen.nextId();
        assertEquals(10001L, id2);
    }

    @Test
    void testIdGenerationAfterReset() {
        idGenerator.nextId();
        idGenerator.nextId("ORDER");
        idGenerator.nextId("USER");
        
        idGenerator.reset();
        
        Long id1 = idGenerator.nextId();
        assertEquals(10000L, id1);
        
        Long id2 = idGenerator.nextId("ORDER");
        assertEquals(10001L, id2);
        
        Long id3 = idGenerator.nextId("USER");
        assertEquals(10001L, id3);
    }

    @Test
    void testBusinessTypeSequenceIndependence() {
        Long id1Order = idGenerator.nextId("ORDER");
        Long id1User = idGenerator.nextId("USER");
        
        assertEquals(10001L, id1Order);
        assertEquals(10001L, id1User);
        
        Long id2Order = idGenerator.nextId("ORDER");
        Long id2User = idGenerator.nextId("USER");
        
        assertEquals(10002L, id2Order);
        assertEquals(10002L, id2User);
    }

    @Test
    void testLargeBatchGeneration() {
        int count = 1000;
        Long[] ids = idGenerator.nextIds(count);
        
        assertEquals(count, ids.length);
        assertEquals(10000L, ids[0]);
        assertEquals(10000L + count - 1, ids[count - 1]);
        
        Long nextId = idGenerator.nextId();
        assertEquals(10000L + count, nextId);
    }

    @Test
    void testSequencesMapIndependence() {
        Long id1 = idGenerator.nextId("TYPE1");
        Long id2 = idGenerator.nextId("TYPE2");
        
        assertEquals(10001L, id1);
        assertEquals(10001L, id2);
        
        idGenerator.reset("TYPE1");
        Long id3 = idGenerator.nextId("TYPE1");
        Long id4 = idGenerator.nextId("TYPE2");
        
        assertEquals(10001L, id3);
        assertEquals(10002L, id4);
    }

    @Test
    void testNextIdWithVeryLargeStartId() {
        // 测试非常大的起始ID（但不测试溢出）
        long largeStartId = Long.MAX_VALUE - 100;
        idGenerator.setStartId(largeStartId);
        
        Long id1 = idGenerator.nextId();
        assertEquals(largeStartId, id1);
        
        Long id2 = idGenerator.nextId();
        assertEquals(largeStartId + 1, id2);
        
        Long id3 = idGenerator.nextId();
        assertEquals(largeStartId + 2, id3);
    }

    @Test
    void testNextIdsWithLargeCount() {
        int count = 10000;
        Long[] ids = idGenerator.nextIds(count);
        assertEquals(count, ids.length);
        assertEquals(10000L, ids[0]);
        assertEquals(10000L + count - 1, ids[count - 1]);
    }

    @Test
    void testDefaultSequenceAfterMultipleOperations() {
        idGenerator.nextId();
        idGenerator.nextId();
        
        idGenerator.setStartId(5000L);
        Long id1 = idGenerator.nextId();
        
        idGenerator.reset();
        Long id2 = idGenerator.nextId();
        
        assertEquals(5000L, id1);
        assertEquals(5000L, id2);
    }

    @Test
    void testBusinessTypeAfterResetAll() {
        idGenerator.nextId("ORDER");
        idGenerator.nextId("USER");
        
        idGenerator.reset();
        
        Long orderId = idGenerator.nextId("ORDER");
        Long userId = idGenerator.nextId("USER");
        
        assertEquals(10001L, orderId);
        assertEquals(10001L, userId);
    }

    @Test
    void testSetStartIdUpdatesExistingSequences() {
        idGenerator.nextId("EXISTING");
        
        idGenerator.setStartId(50000L);
        
        Long id = idGenerator.nextId("EXISTING");
        assertEquals(50000L, id);
    }

    @Test
    void testGetCurrentIdForNonExistentBusinessType() {
        assertEquals(9999L, idGenerator.getCurrentId("NON_EXISTENT"));
        
        idGenerator.setStartId(50000L);
        assertEquals(49999L, idGenerator.getCurrentId("ANOTHER_NON_EXISTENT"));
    }

    @Test
    void testNextIdMethodWithIfCondition() {
        LocalIdGenerator generator = new LocalIdGenerator();
        
        Long id = generator.nextId();
        assertEquals(10000L, id);
        
        Long id2 = generator.nextId();
        assertEquals(10001L, id2);
    }

    @Test
    void testInitialStateWithoutSetStartId() {
        LocalIdGenerator generator = new LocalIdGenerator();
        
        assertEquals(1L, generator.getCurrentId());
        
        Long id1 = generator.nextId();
        assertEquals(10000L, id1);
        
        Long id2 = generator.nextId();
        assertEquals(10001L, id2);
        
        Long id3 = generator.nextId("TEST");
        assertEquals(10001L, id3);
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final int iterations = 100;
        
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    idGenerator.nextId();
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(9999L + (threadCount * iterations), idGenerator.getCurrentId());
    }

    @Test
    void testNextIdWithLongStringPrefix() {
        String longPrefix = "VERY_LONG_PREFIX_THAT_IS_MUCH_LONGER_THAN_NORMAL_";
        String id = idGenerator.nextIdWithPrefix("LONG_PREFIX_TEST", longPrefix);
        assertEquals(longPrefix + "10001", id);
    }

    @Test
    void testNextIdWithEmptyBusinessTypeAndPrefix() {
        String id = idGenerator.nextIdWithPrefix("", "");
        assertEquals("10001", id);
        
        String id2 = idGenerator.nextIdWithPrefix("", "");
        assertEquals("10002", id2);
    }

    @Test
    void testNextIdWithPrefixMultipleBusinessTypes() {
        String id1 = idGenerator.nextIdWithPrefix("TYPE1", "PREFIX_");
        assertEquals("PREFIX_10001", id1);
        
        String id2 = idGenerator.nextIdWithPrefix("TYPE2", "PREFIX_");
        assertEquals("PREFIX_10001", id2);
        
        String id3 = idGenerator.nextIdWithPrefix("TYPE1", "PREFIX_");
        assertEquals("PREFIX_10002", id3);
    }

    @Test
    void testNextIdWithPrefixAfterReset() {
        String id1 = idGenerator.nextIdWithPrefix("TEST", "PRE_");
        assertEquals("PRE_10001", id1);
        
        idGenerator.reset("TEST");
        
        String id2 = idGenerator.nextIdWithPrefix("TEST", "PRE_");
        assertEquals("PRE_10001", id2);
    }

    @Test
    void testNextIdWithPrefixAfterSetStartId() {
        String id1 = idGenerator.nextIdWithPrefix("TEST", "PRE_");
        assertEquals("PRE_10001", id1);
        
        idGenerator.setStartId(50000L);
        
        String id2 = idGenerator.nextIdWithPrefix("TEST", "PRE_");
        assertEquals("PRE_50000", id2);
    }

    @Test
    void testNextIdWithPrefixSameBusinessTypeDifferentPrefix() {
        String id1 = idGenerator.nextIdWithPrefix("SAME_TYPE", "PRE1_");
        assertEquals("PRE1_10001", id1);
        
        String id2 = idGenerator.nextIdWithPrefix("SAME_TYPE", "PRE2_");
        assertEquals("PRE2_10002", id2);
        
        String id3 = idGenerator.nextIdWithPrefix("SAME_TYPE", "PRE1_");
        assertEquals("PRE1_10003", id3);
    }

    @Test
    void testDefaultSequenceInitialization() {
        // 测试默认序列初始化
        LocalIdGenerator generator = new LocalIdGenerator();
        
        // 不调用setStartId
        assertEquals(1L, generator.getCurrentId());
        
        // 调用setStartId后
        generator.setStartId(10000L);
        assertEquals(9999L, generator.getCurrentId());
    }

    @Test
    void testNextIdWithMaxValue() {
        // 测试Long.MAX_VALUE边界（不测试溢出）
        idGenerator.setStartId(Long.MAX_VALUE - 5);
        
        for (int i = 0; i < 5; i++) {
            Long id = idGenerator.nextId();
            assertEquals(Long.MAX_VALUE - 5 + i, id);
        }
    }
}