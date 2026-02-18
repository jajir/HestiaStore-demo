package org.hestiastore.demo.core;

public record ValueRecord(long version, long timestamp, byte[] payload) {
}
