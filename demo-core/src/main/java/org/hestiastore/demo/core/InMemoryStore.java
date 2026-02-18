package org.hestiastore.demo.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryStore {
    private final ConcurrentMap<Long, ValueRecord> map = new ConcurrentHashMap<>();
    private final AtomicLong nextKey = new AtomicLong();

    public InMemoryStore(long initialKeys) {
        nextKey.set(initialKeys);
    }

    public ValueRecord get(long key) {
        return map.get(key);
    }

    public void put(long key, ValueRecord value) {
        map.put(key, value);
    }

    public ValueRecord delete(long key) {
        return map.remove(key);
    }

    public long allocateKey() {
        return nextKey.getAndIncrement();
    }

    public long size() {
        return map.size();
    }

    public long nextKey() {
        return nextKey.get();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void seed(long key, ValueRecord value) {
        map.put(key, value);
    }
}
