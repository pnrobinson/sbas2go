package org.jax.sbas2go.parsers;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SbasParser {

    private final Map<SpliceType, Map<Integer, FromGtf>> gtfMap;

    private final Map<String, String> ensemblGeneIdToSymbolMap;

    private final Set<String> ensembleDgePopulation;

    private final Map<String, Set<Dge>> tissueSpecificDgeMap;

    private final Map<String, List<Das>> tissue2asMap;
    private final Set<String> allGeneSymbols;


    public SbasParser(String dir) {
        File directory = new File(dir);
        if (! directory.exists() && directory.isDirectory()) {
            throw new SbasRuntimeException("Invalid sbas data directory");
        }
        Path fromGtfPath = Paths.get(directory.getAbsolutePath(), "fromGTF");
        File fromGtfFile = fromGtfPath.toFile();
        if (! fromGtfFile.exists() && fromGtfFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find fromGTF directory");
        }
        FromGtfParser fromGtfParser = new FromGtfParser(fromGtfFile);
        this.gtfMap = fromGtfParser.getGtfMap();

        Path dgePath = Paths.get(directory.getAbsolutePath(), "dge");
        File dgeFile = dgePath.toFile();
        if (! dgeFile.exists() && dgeFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDgeParser dgeParser = new SbasDgeParser(dgeFile);
        this.ensemblGeneIdToSymbolMap = dgeParser.getEnsemblIdToSymbolMap();
        this.ensembleDgePopulation = dgeParser.getEnsemblPopulation();
        this.tissueSpecificDgeMap = dgeParser.getTissueSpecificDiffernetial();
        Path dasPath = Paths.get(directory.getAbsolutePath(), "as");
        File dasFile = dasPath.toFile();
        if (! dasFile.exists() && dasFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDasParser dasParser = new SbasDasParser(dasFile);
        this.tissue2asMap = dasParser.getTissue2asMap();
        this.allGeneSymbols = dasParser.getAllGeneSymbols();
    }

    public Map<SpliceType, Map<Integer, FromGtf>> getGtfMap() {
        return gtfMap;
    }

    public Map<String, String> getEnsemblGeneIdToSymbolMap() {
        return ensemblGeneIdToSymbolMap;
    }

    public Set<String> getEnsembleDgePopulation() {
        return ensembleDgePopulation;
    }

    public Map<String, Set<Dge>> getTissueSpecificDgeMap() {
        return tissueSpecificDgeMap;
    }

    public Map<String, List<Das>> getTissue2asMap() {
        return tissue2asMap;
    }

    public Set<String> getAllGeneSymbols() {
        return allGeneSymbols;
    }
}
