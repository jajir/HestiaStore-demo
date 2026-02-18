package org.hestiastore.demo.core;

import java.util.concurrent.ThreadLocalRandom;

public final class ZipfLikeKeySelector {
    private final double skew;

    public ZipfLikeKeySelector(double skew) {
        this.skew = skew;
    }

    public long nextKey(long upperBoundExclusive) {
        if (upperBoundExclusive <= 1) {
            return 0L;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double u = random.nextDouble();
        double value = Math.pow(u, skew);
        long key = (long) (value * upperBoundExclusive);
        if (key >= upperBoundExclusive) {
            return upperBoundExclusive - 1;
        }
        return Math.max(0L, key);
    }
}
