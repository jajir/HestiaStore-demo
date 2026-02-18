package org.hestiastore.demo.runner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RunnerStateStore {
    private static final String STATE_FILE_NAME = "runner-state.json";

    private final ObjectMapper objectMapper;

    public RunnerStateStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RunnerState load(Path directory) {
        Path file = directory.resolve(STATE_FILE_NAME);
        if (!Files.exists(file)) {
            return new RunnerState(0L, 0L);
        }

        try {
            return objectMapper.readValue(file.toFile(), RunnerState.class);
        } catch (IOException e) {
            return new RunnerState(0L, 0L);
        }
    }

    public void save(Path directory, RunnerState state) throws IOException {
        Files.createDirectories(directory);
        Path file = directory.resolve(STATE_FILE_NAME);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), state);
    }
}
