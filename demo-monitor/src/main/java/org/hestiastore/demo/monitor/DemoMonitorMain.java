package org.hestiastore.demo.monitor;

import picocli.CommandLine;

public final class DemoMonitorMain {
    private DemoMonitorMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DemoMonitorRootCommand()).execute(args);
        System.exit(exitCode);
    }
}
