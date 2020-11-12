package org.jax.sbas2go.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "download",
        mixinStandardHelpOptions = true,
        description = "Download files for prositometry")
public class SbasCommand implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        return null;
    }
}
