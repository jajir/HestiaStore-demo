package org.hestiastore.demo.monitor;

import picocli.CommandLine.Command;

@Command(
        name = "demo",
        mixinStandardHelpOptions = true,
        subcommands = {MonitorCommand.class},
        description = "HestiaStore demo monitor"
)
public final class DemoMonitorRootCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use subcommand: monitor");
    }
}
