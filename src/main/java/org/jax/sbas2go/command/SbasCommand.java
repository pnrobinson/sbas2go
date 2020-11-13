package org.jax.sbas2go.command;

import org.jax.sbas2go.parsers.HgncParser;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "go",
        mixinStandardHelpOptions = true,
        description = "GO")
public class SbasCommand implements Callable<Integer> {
    @CommandLine.Option(names={"-s","--sbas"}, description ="directory to sbas data" )
    private String sbasdir;

    @Override
    public Integer call() throws Exception {
        //GoParser goParser = new GoParser("data");
        //SbasParser sbasParser = new SbasParser(sbasdir);
        HgncParser hgncParser = new HgncParser();


        return 0;
    }
}
