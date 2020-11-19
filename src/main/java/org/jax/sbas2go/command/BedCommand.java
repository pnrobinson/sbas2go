package org.jax.sbas2go.command;

import org.jax.sbas2go.analysis.Sbas2Bed;
import org.jax.sbas2go.gtf.FromGtf;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "bed",
        mixinStandardHelpOptions = true,
        description = "Extract BED files for motif analysis")
public class BedCommand implements Callable<Integer> {

    @CommandLine.Option(names={"-s","--sbas"}, description ="directory to sbas data" )
    private String sbasdir;


    @Override
    public Integer call() throws Exception {
        Sbas2Bed s2b = new Sbas2Bed(this.sbasdir);
        Set<FromGtf> significantEvents = s2b.getSignificantGtfSet();
        Set<FromGtf> controlGtfSet = s2b.getControlGtfSet();
        outputGtfSet(significantEvents, "das-significant.bed");
        outputGtfSet(controlGtfSet, "das-control.bed");

        return 0;
    }

    /**
     * Note that we subtract 1 from the genomic start in tthe GTF file to correspond to BED format
     * @param gtfset
     * @param fname
     */
    public void outputGtfSet(Set<FromGtf> gtfset, String fname) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            for (var gtf : gtfset) {
                String name = String.format("%s-%d(%s)", gtf.getSpliceType(), gtf.getId(), gtf.getSymbol());
                String line = String.format("%s\t%d\t%d\t%s\t0\t%s\n",
                        gtf.getChr(),
                        gtf.getExonStart() - 1,
                        gtf.getExonEnd(),
                        name,
                        gtf.getStrand());
                bw.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
