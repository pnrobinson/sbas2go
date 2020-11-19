package org.jax.sbas2go.analysis;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;
import org.jax.sbas2go.parsers.FromGtfParser;
import org.jax.sbas2go.parsers.SbasDasParser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sbas2Bed {
    /**
     * Key-one of the five RMATS splice types
     * Value-a map whose key is the event number and the value is a {@link FromGtf} object.
     */
    private final Map<SpliceType, Map<Integer, FromGtf>> gtfmap;

    private final Map<String, List<Das>> dasMap;

    private final Set<FromGtf> significantGtfSet;
    private final Set<FromGtf> controlGtfSet;

    public Sbas2Bed(String sbasdir){
        File directory = new File(sbasdir);
        Path fromGtfPath = Paths.get(directory.getAbsolutePath(), "fromGTF");
        File fromGtfFile = fromGtfPath.toFile();
        if (! fromGtfFile.exists() && fromGtfFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find fromGTF directory");
        }
        FromGtfParser fromGtfParser = new FromGtfParser(fromGtfFile);
        this.gtfmap = fromGtfParser.getGtfMap();
        Path dasPath = Paths.get(directory.getAbsolutePath(), "as");
        File dasFile = dasPath.toFile();
        if (! dasFile.exists() && dasFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        boolean skipNonSignificant = false;
        SbasDasParser dasParser = new SbasDasParser(dasFile, skipNonSignificant);
        dasMap = dasParser.getTissue2asMap();
        significantGtfSet = new HashSet<>();
        controlGtfSet = new HashSet<>();
        extractSignificantDAS();
        extractControlDAS();
    }


    public void extractSignificantDAS() {
        for (var e : dasMap.entrySet()) {
            String tissue = e.getKey();
            List<Das> dasList = e.getValue();
            for (Das d : dasList) {
               if (d.getAdjPval() < 0.05) {
                   SpliceType stype = d.getStype();
                   int id = d.getId();
                   FromGtf fgtf = this.gtfmap.get(stype).get(id);
                   if (fgtf == null) {
                       System.err.printf("[ERROR] COuld not retrieve GTF for %s:%d", stype, id);
                   } else {
                       significantGtfSet.add(fgtf);
                   }
               }
            }
        }
        System.out.printf("[INFO] We got %d significant GTF events\n", significantGtfSet.size());
    }

    public void extractControlDAS() {
        for (var e : dasMap.entrySet()) {
            String tissue = e.getKey();
            List<Das> dasList = e.getValue();
            for (Das d : dasList) {
                if (d.getAdjPval() > 0.95) {
                    SpliceType stype = d.getStype();
                    int id = d.getId();
                    FromGtf fgtf = this.gtfmap.get(stype).get(id);
                    if (significantGtfSet.contains(fgtf)) {
                        continue;  // some events are significant in one tissue, but not in others.
                        // discard such events, it is enough to be significant in one tissue!
                    }
                    if (fgtf == null) {
                        System.err.printf("[ERROR] COuld not retrieve GTF for %s:%d", stype, id);
                    } else {
                        controlGtfSet.add(fgtf);
                    }
                }
            }
        }
        System.out.printf("[INFO] We got %d control GTF events\n", controlGtfSet.size());
    }

    public Set<FromGtf> getSignificantGtfSet() {
        return significantGtfSet;
    }

    public Set<FromGtf> getControlGtfSet() {
        return controlGtfSet;
    }
}
