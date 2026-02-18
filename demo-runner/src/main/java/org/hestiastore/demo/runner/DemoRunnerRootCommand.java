package org.hestiastore.demo.runner;

import picocli.CommandLine.Command;

@Command(
        name = "demo",
        mixinStandardHelpOptions = true,
        subcommands = {RunCommand.class},
        description = "HestiaStore demo runner"
)
public final class DemoRunnerRootCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommand: run");
    }
}
