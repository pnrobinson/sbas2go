package org.jax.sbas2go.parsers;

import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Parse the results of sbas analysis of differential gene expression
 * For each tissue, there are three files:
 *
 * {tissue}_DGE.csv: topTable results for the edgeR/Limma differential analysis
 * {tissue}_DGE_ensg_map.csv: a convenience mapping of the ENSG to the geneSymbol
 * {tissue}_DGE_refined.csv: a convenience mapping of the topTable results satisfying the 1.5 fold change and adjusted P-Value < 0.05.
 */
public class SbasDgeParser {

    private final Map<String,String> ensemblIdToSymbolMap;
    /** Set of all genes in the DGE data. */
    private final Set<String> ensemblPopulation;
    /** Key -- tissue, value, set of significant genes. */
    private final Map<String, Set<Dge>> tissueSpecificDiffernetial;

    private final static double P_THRESHOLD = 0.05;


    public SbasDgeParser(File dgeDir) {
        String [] fnames = dgeDir.list();
        List<String> mapFiles = Arrays.stream(fnames).filter(s -> s.contains("ensg_map")).collect(Collectors.toList());
        List<String> topTableFiles = Arrays.stream(fnames)
                .filter(not(value -> value.contains("ensg_map")))
                .filter(not(value -> value.contains("refined")))
                .collect(Collectors.toList());
        ensemblIdToSymbolMap = createEnsemblIdToSymbolMap(dgeDir, mapFiles);
        System.out.printf("[INFO] Extracted %d ensembl id to gene symbol mappings.\n", ensemblIdToSymbolMap.size());
        this.ensemblPopulation = new HashSet<>();
        this.tissueSpecificDiffernetial = new HashMap<>();
        getDifferentialGenes(dgeDir, topTableFiles);
    }

    /**
     * Format
     * [0] ensg with version, e.g., ENSG00000225470.7
     * [1] logFC
     * [2] AveExpr
     * [3] t
     * [4] P.Value
     * [5] adj.P.Val
     * [6] B
     * @param dgeDir
     * @param fnames
     */
    private void  getDifferentialGenes(File dgeDir, List<String> fnames) {
        for (String name : fnames) {
            int i = name.indexOf("_DGE.csv");
            if (i < 0) {
                throw new SbasRuntimeException("Bad DGE file name " + name);
            }
            String tissue = name.substring(0,i);
            Path p = Paths.get(dgeDir.getAbsolutePath(), name);
            File f = p.toFile();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                br.readLine(); // discard header
                String line;
                while ((line = br.readLine()) != null) {
                   String [] fields = line.split(",");
                   if (fields.length != 7) {
                       throw new SbasRuntimeException("Malformed dge line " + line);
                   }
                   String ensg = fields[0];
                   double fc = Double.parseDouble(fields[1]);
                   double aveExp = Double.parseDouble(fields[2]);
                   double t = Double.parseDouble(fields[3]);
                   double pval = Double.parseDouble(fields[4]);
                   double adjPval = Double.parseDouble(fields[5]);
                   double B = Double.parseDouble(fields[6]);
                   this.ensemblPopulation.add(ensg);
                   if (adjPval > P_THRESHOLD) {
                       continue;
                   }
                   Dge dge = new Dge(ensg, fc, aveExp, t, pval, adjPval, B);
                   tissueSpecificDiffernetial.putIfAbsent(tissue, new HashSet<>());
                   tissueSpecificDiffernetial.get(tissue).add(dge);

                }
            } catch (IOException e) {
                throw new SbasRuntimeException(e.getLocalizedMessage());
            }
        }
    }

    Map<String,String> createEnsemblIdToSymbolMap(File dgeDir, List<String> fnames) {
        Map<String,String> id2symbol = new HashMap<>();
        for (String name : fnames) {
            Path p = Paths.get(dgeDir.getAbsolutePath(), name);
            File f = p.toFile();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                br.readLine(); // discard header
                String line;
                while ((line = br.readLine()) != null) {
                    String [] fields = line.split(",");
                    if (fields.length != 2) {
                        throw new SbasRuntimeException("Malformed map file line " + line);
                    }
                    String ensg = fields[0];
                    String symbol = fields[1];
                    id2symbol.put(ensg, symbol);
                }
            } catch (IOException e) {
                throw new SbasRuntimeException(e.getLocalizedMessage());
            }
        }
        return id2symbol;
    }

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    public Map<String, String> getEnsemblIdToSymbolMap() {
        return ensemblIdToSymbolMap;
    }

    public Set<String> getEnsemblPopulation() {
        return ensemblPopulation;
    }

    public Map<String, Set<Dge>> getTissueSpecificDiffernetial() {
        return tissueSpecificDiffernetial;
    }
}
