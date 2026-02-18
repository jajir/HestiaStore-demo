package org.hestiastore.demo.monitor;

import picocli.CommandLine;

public final class DemoMonitorMain {
    private DemoMonitorMain() {
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new DemoMonitorRootCommand());
        commandLine.setExecutionExceptionHandler((exception, cmd, parseResult) -> {
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = "monitor startup failed";
            }
            cmd.getErr().println("ERROR: " + message);
            return cmd.getCommandSpec().exitCodeOnExecutionException();
        });

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
