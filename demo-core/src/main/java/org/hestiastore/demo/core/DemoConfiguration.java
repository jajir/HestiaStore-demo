package org.hestiastore.demo.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DemoConfiguration {
    private final int threads;
    private final long targetKeys;
    private final double gteRatio;
    private final double putRatio;
    private final double deleteRatio;

    private final Integer maxNumberOfKeysInSegmentCache;
    private final Integer maxNumberOfKeysInSegmentWriteCache;
    private final Integer maxNumberOfKeysInSegmentWriteCacheDuringMaintenance;
    private final Integer maxNumberOfKeysInSegmentChunk;
    private final Integer maxNumberOfDeltaCacheFiles;
    private final Integer maxNumberOfKeysInSegment;
    private final Integer maxNumberOfSegmentsInCache;

    private final Integer bloomFilterNumberOfHashFunctions;
    private final Integer bloomFilterIndexSizeInBytes;
    private final Double bloomFilterProbabilityOfFalsePositive;

    private final Integer diskIoBufferSizeInBytes;
    private final Integer indexWorkerThreadCount;
    private final Integer numberOfIoThreads;
    private final Integer numberOfSegmentIndexMaintenanceThreads;
    private final Integer numberOfIndexMaintenanceThreads;
    private final Integer numberOfRegistryLifecycleThreads;
    private final Integer indexBusyBackoffMillis;
    private final Integer indexBusyTimeoutMillis;
    private final Boolean segmentMaintenanceAutoEnabled;

    private final String indexName;
    private final Class<?> keyClass;
    private final Class<?> valueClass;
    private final String keyTypeDescriptor;
    private final String valueTypeDescriptor;
    private final Boolean contextLoggingEnabled;

    public DemoConfiguration(
            int threads,
            long targetKeys,
            double gteRatio,
            double putRatio,
            double deleteRatio,
            Integer maxNumberOfKeysInSegmentCache,
            Integer maxNumberOfKeysInSegmentWriteCache,
            Integer maxNumberOfKeysInSegmentWriteCacheDuringMaintenance,
            Integer maxNumberOfKeysInSegmentChunk,
            Integer maxNumberOfDeltaCacheFiles,
            Integer maxNumberOfKeysInSegment,
            Integer maxNumberOfSegmentsInCache,
            Integer bloomFilterNumberOfHashFunctions,
            Integer bloomFilterIndexSizeInBytes,
            Double bloomFilterProbabilityOfFalsePositive,
            Integer diskIoBufferSizeInBytes,
            Integer indexWorkerThreadCount,
            Integer numberOfIoThreads,
            Integer numberOfSegmentIndexMaintenanceThreads,
            Integer numberOfIndexMaintenanceThreads,
            Integer numberOfRegistryLifecycleThreads,
            Integer indexBusyBackoffMillis,
            Integer indexBusyTimeoutMillis,
            Boolean segmentMaintenanceAutoEnabled,
            String indexName,
            Class<?> keyClass,
            Class<?> valueClass,
            String keyTypeDescriptor,
            String valueTypeDescriptor,
            Boolean contextLoggingEnabled
    ) {
        this.threads = threads;
        this.targetKeys = targetKeys;
        this.gteRatio = gteRatio;
        this.putRatio = putRatio;
        this.deleteRatio = deleteRatio;
        this.maxNumberOfKeysInSegmentCache = maxNumberOfKeysInSegmentCache;
        this.maxNumberOfKeysInSegmentWriteCache = maxNumberOfKeysInSegmentWriteCache;
        this.maxNumberOfKeysInSegmentWriteCacheDuringMaintenance = maxNumberOfKeysInSegmentWriteCacheDuringMaintenance;
        this.maxNumberOfKeysInSegmentChunk = maxNumberOfKeysInSegmentChunk;
        this.maxNumberOfDeltaCacheFiles = maxNumberOfDeltaCacheFiles;
        this.maxNumberOfKeysInSegment = maxNumberOfKeysInSegment;
        this.maxNumberOfSegmentsInCache = maxNumberOfSegmentsInCache;
        this.bloomFilterNumberOfHashFunctions = bloomFilterNumberOfHashFunctions;
        this.bloomFilterIndexSizeInBytes = bloomFilterIndexSizeInBytes;
        this.bloomFilterProbabilityOfFalsePositive = bloomFilterProbabilityOfFalsePositive;
        this.diskIoBufferSizeInBytes = diskIoBufferSizeInBytes;
        this.indexWorkerThreadCount = indexWorkerThreadCount;
        this.numberOfIoThreads = numberOfIoThreads;
        this.numberOfSegmentIndexMaintenanceThreads = numberOfSegmentIndexMaintenanceThreads;
        this.numberOfIndexMaintenanceThreads = numberOfIndexMaintenanceThreads;
        this.numberOfRegistryLifecycleThreads = numberOfRegistryLifecycleThreads;
        this.indexBusyBackoffMillis = indexBusyBackoffMillis;
        this.indexBusyTimeoutMillis = indexBusyTimeoutMillis;
        this.segmentMaintenanceAutoEnabled = segmentMaintenanceAutoEnabled;
        this.indexName = indexName;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.keyTypeDescriptor = keyTypeDescriptor;
        this.valueTypeDescriptor = valueTypeDescriptor;
        this.contextLoggingEnabled = contextLoggingEnabled;
    }

    public static DemoConfiguration defaults(int threads, long targetKeys, double gteRatio, double putRatio, double deleteRatio) {
        return new DemoConfiguration(
                threads,
                targetKeys,
                gteRatio,
                putRatio,
                deleteRatio,
                500_000,
                250_000,
                100_000,
                50_000,
                128,
                1_000_000,
                16,
                7,
                134_217_728,
                0.01,
                1_048_576,
                Math.max(2, threads),
                4,
                2,
                2,
                2,
                10,
                500,
                true,
                "hestia-demo-index",
                Long.class,
                byte[].class,
                "LONG",
                "BINARY",
                false
        );
    }

    public void validateRatios() {
        double sum = gteRatio + putRatio + deleteRatio;
        if (Math.abs(sum - 1.0d) > 0.000_001d) {
            throw new IllegalArgumentException("Operation ratios must sum to 1.0");
        }
    }

    public int threads() {
        return threads;
    }

    public long targetKeys() {
        return targetKeys;
    }

    public double gteRatio() {
        return gteRatio;
    }

    public double putRatio() {
        return putRatio;
    }

    public double deleteRatio() {
        return deleteRatio;
    }

    public Map<String, Object> toNodeConfigMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("maxNumberOfKeysInSegmentCache", maxNumberOfKeysInSegmentCache);
        map.put("maxNumberOfKeysInSegmentWriteCache", maxNumberOfKeysInSegmentWriteCache);
        map.put("maxNumberOfKeysInSegmentWriteCacheDuringMaintenance", maxNumberOfKeysInSegmentWriteCacheDuringMaintenance);
        map.put("maxNumberOfKeysInSegmentChunk", maxNumberOfKeysInSegmentChunk);
        map.put("maxNumberOfDeltaCacheFiles", maxNumberOfDeltaCacheFiles);
        map.put("maxNumberOfKeysInSegment", maxNumberOfKeysInSegment);
        map.put("maxNumberOfSegmentsInCache", maxNumberOfSegmentsInCache);
        map.put("bloomFilterNumberOfHashFunctions", bloomFilterNumberOfHashFunctions);
        map.put("bloomFilterIndexSizeInBytes", bloomFilterIndexSizeInBytes);
        map.put("bloomFilterProbabilityOfFalsePositive", bloomFilterProbabilityOfFalsePositive);
        map.put("diskIoBufferSizeInBytes", diskIoBufferSizeInBytes);
        map.put("indexWorkerThreadCount", indexWorkerThreadCount);
        map.put("numberOfIoThreads", numberOfIoThreads);
        map.put("numberOfSegmentIndexMaintenanceThreads", numberOfSegmentIndexMaintenanceThreads);
        map.put("numberOfIndexMaintenanceThreads", numberOfIndexMaintenanceThreads);
        map.put("numberOfRegistryLifecycleThreads", numberOfRegistryLifecycleThreads);
        map.put("indexBusyBackoffMillis", indexBusyBackoffMillis);
        map.put("indexBusyTimeoutMillis", indexBusyTimeoutMillis);
        map.put("segmentMaintenanceAutoEnabled", segmentMaintenanceAutoEnabled);
        map.put("indexName", indexName);
        map.put("keyClass", keyClass == null ? null : keyClass.getName());
        map.put("valueClass", valueClass == null ? null : valueClass.getName());
        map.put("keyTypeDescriptor", keyTypeDescriptor);
        map.put("valueTypeDescriptor", valueTypeDescriptor);
        map.put("contextLoggingEnabled", contextLoggingEnabled);
        return map;
    }
}
