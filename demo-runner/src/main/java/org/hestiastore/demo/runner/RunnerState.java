package org.hestiastore.demo.runner;

public record RunnerState(long recoveredKeys, long nextKey) {
}
