package org.jax.sbas2go;

import org.jax.sbas2go.command.BedCommand;
import org.jax.sbas2go.command.DownloadCommand;
import org.jax.sbas2go.command.LaTeXCommand;
import org.jax.sbas2go.command.SbasCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;


@CommandLine.Command(name = "sbas2go", mixinStandardHelpOptions = true, version = "sbas2go 0.1.0",
            description = "SBAS Gene Ontology tool.")
public class Main implements Callable<Integer> {


    public static void main(String[] args) {
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }
        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("go", new SbasCommand())
                .addSubcommand("bed", new BedCommand())
                .addSubcommand("latex", new LaTeXCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }
}
