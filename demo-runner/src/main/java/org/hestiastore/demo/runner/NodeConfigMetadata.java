package org.hestiastore.demo.runner;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NodeConfigMetadata {
    private NodeConfigMetadata() {
    }

    public static Map<String, String> descriptions() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("maxNumberOfKeysInSegmentCache", "Maximum number of keys kept in in-memory segment read cache.");
        map.put("maxNumberOfKeysInSegmentWriteCache", "Maximum number of keys buffered in segment write cache during normal operation.");
        map.put("maxNumberOfKeysInSegmentWriteCacheDuringMaintenance", "Maximum write-cache key capacity while maintenance mode is active.");
        map.put("maxNumberOfKeysInSegmentChunk", "Maximum number of keys per segment chunk unit.");
        map.put("maxNumberOfDeltaCacheFiles", "Maximum number of delta cache files retained.");
        map.put("maxNumberOfKeysInSegment", "Upper bound of keys in a single segment.");
        map.put("maxNumberOfSegmentsInCache", "Maximum number of segments held in cache simultaneously.");
        map.put("bloomFilterNumberOfHashFunctions", "Number of hash functions used by Bloom filter.");
        map.put("bloomFilterIndexSizeInBytes", "Size of Bloom filter index. Use same formatting style as HEAP USED.");
        map.put("bloomFilterProbabilityOfFalsePositive", "Expected Bloom filter false positive probability.");
        map.put("diskIoBufferSizeInBytes", "Buffer size used for disk I/O operations.");
        map.put("indexWorkerThreadCount", "Number of worker threads for index operations.");
        map.put("numberOfIoThreads", "Number of threads dedicated to I/O processing.");
        map.put("numberOfSegmentIndexMaintenanceThreads", "Number of threads for segment-index maintenance tasks.");
        map.put("numberOfIndexMaintenanceThreads", "Number of threads for general index maintenance tasks.");
        map.put("numberOfRegistryLifecycleThreads", "Number of threads used for registry lifecycle operations.");
        map.put("indexBusyBackoffMillis", "Backoff interval before retry when index is busy.");
        map.put("indexBusyTimeoutMillis", "Timeout waiting for index busy state to clear.");
        map.put("segmentMaintenanceAutoEnabled", "Whether automatic segment maintenance is enabled.");
        map.put("indexName", "Logical index identifier.");
        map.put("keyClass", "Java type used for key serialization/handling.");
        map.put("valueClass", "Java type used for value serialization/handling.");
        map.put("keyTypeDescriptor", "Descriptor string for key type.");
        map.put("valueTypeDescriptor", "Descriptor string for value type.");
        map.put("contextLoggingEnabled", "Whether context-aware logging is enabled.");
        return map;
    }
}
