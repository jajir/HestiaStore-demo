package org.hestiastore.demo.runner;

import picocli.CommandLine;

public final class DemoRunnerMain {
    private DemoRunnerMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DemoRunnerRootCommand()).execute(args);
        System.exit(exitCode);
    }
}
