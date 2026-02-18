package org.hestiastore.demo.core;

import java.util.concurrent.ThreadLocalRandom;

public final class PayloadGenerator {
    private static final int MIN_SIZE = 200;
    private static final int MAX_SIZE = 2000;

    public byte[] generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int size = random.nextInt(MIN_SIZE, MAX_SIZE + 1);
        byte[] payload = new byte[size];

        // Split payload generation between compressible and random bytes.
        if (random.nextBoolean()) {
            byte value = (byte) random.nextInt(16);
            for (int i = 0; i < payload.length; i++) {
                payload[i] = value;
            }
            return payload;
        }

        random.nextBytes(payload);
        return payload;
    }
}
