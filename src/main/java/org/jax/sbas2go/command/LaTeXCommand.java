package org.jax.sbas2go.command;


import org.jax.sbas2go.latex.ResultWriter;
import org.jax.sbas2go.latex.SbasLaTeXParser;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "latex",
        mixinStandardHelpOptions = true,
        description = "LaTeX")
public class LaTeXCommand implements Callable<Integer> {
    @CommandLine.Option(names={"-p","--prefix"}, description ="outfile prefix" )
    private String prefix="sbas";

    public LaTeXCommand(){

    }

    @Override
    public Integer call() throws Exception {
        SbasLaTeXParser parser = new SbasLaTeXParser();
        ResultWriter writer = new ResultWriter(this.prefix, parser.getResultRowList());
        writer.printSummary();
        writer.outputLatexTable();
        writer.outputSupplement();
        writer.outputDasTable();
        return 0;
    }
}
