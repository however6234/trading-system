package com.capital.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地自增ID生成器
 * 最简单，适合单机应用
 */
@Component
@Slf4j
public class LocalIdGenerator implements IdGenerator {
    
    private final AtomicLong defaultSequence = new AtomicLong(1);
    
    private final Map<String, AtomicLong> sequences = new ConcurrentHashMap<>();
    
    private long startId = 10000L;  // 从10000开始，避免ID太小
    
    @Override
    public Long nextId() {
        long id = defaultSequence.incrementAndGet();
        if (id < startId) {
            id = startId;
            defaultSequence.set(startId);
        }
        log.debug("Generated local ID: {}", id);
        return id;
    }
    public Long nextId(String businessType) {
        AtomicLong sequence = sequences.computeIfAbsent(
            businessType, 
            k -> new AtomicLong(startId)
        );
        
        long id = sequence.incrementAndGet();
        log.debug("Generated local ID for {}: {}", businessType, id);
        return id;
    }
    
    public String nextIdWithPrefix(String businessType, String prefix) {
        Long id = nextId(businessType);
        return prefix + id;
    }
    
    public Long[] nextIds(int count) {
        Long[] ids = new Long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }
    
    @Override
    public String getType() {
        return "LOCAL";
    }
    
    public void setStartId(long startId) {
        this.startId = startId;
        defaultSequence.set(startId - 1);
        sequences.values().forEach(seq -> seq.set(startId - 1));
        log.info("Local ID Generator start ID set to: {}", startId);
    }
    
    public void reset() {
        defaultSequence.set(startId - 1);
        sequences.clear();
        log.info("Local ID Generator reset");
    }
    
    public void reset(String businessType) {
        sequences.remove(businessType);
        log.info("Reset sequence for business type: {}", businessType);
    }
    
    public Long getCurrentId() {
        return defaultSequence.get();
    }
    
    public Long getCurrentId(String businessType) {
        AtomicLong sequence = sequences.get(businessType);
        return sequence != null ? sequence.get() : startId - 1;
    }
}