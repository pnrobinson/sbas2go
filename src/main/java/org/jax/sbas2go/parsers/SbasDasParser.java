package org.jax.sbas2go.parsers;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SbasDasParser {

    private final Map<String, List<Das>> tissue2asMap;
    private final Set<String> allGeneSymbols;
    private int totalAlternativeSplicintEventCount = 0;
    private int significantEventCount = 0;

    public SbasDasParser(File dgeDir) {
        String []files = dgeDir.list();
        Objects.requireNonNull(files);
        List<String> originalFiles = Arrays.stream(files)
                .filter(not(value -> value.contains("refined")))
                .collect(Collectors.toList());
        List<String> a3ss = originalFiles.stream().filter(value -> value.startsWith("a3ss")).collect(Collectors.toList());
        List<String> a5ss =originalFiles.stream().filter(value -> value.startsWith("a5ss")).collect(Collectors.toList());
        List<String> ri = originalFiles.stream().filter(value -> value.startsWith("ri")).collect(Collectors.toList());
        List<String> se = originalFiles.stream().filter(value -> value.startsWith("se")).collect(Collectors.toList());
        List<String> mxe = originalFiles.stream().filter(value -> value.startsWith("mxe")).collect(Collectors.toList());
        tissue2asMap = new HashMap<>();
        allGeneSymbols = new HashSet<>();
        parseFiles(dgeDir, a3ss);
        parseFiles(dgeDir, a5ss);
        parseFiles(dgeDir, ri);
        parseFiles(dgeDir, se);
        parseFiles(dgeDir, mxe);
        System.out.printf("[INFO] Parsed %d tissues for the AS data.\n", tissue2asMap.size());
        System.out.printf("[INFO] Parsed %d gene symbols in the AS data.\n", allGeneSymbols.size());
        System.out.printf("[INFO] Parsed %d events, %d of which were significant.\n", totalAlternativeSplicintEventCount, significantEventCount);
    }

    /**
     *
     * @param fname something like a3ss_adipose_subcutaneous_AS_model_B_sex_as_events.csv,
     * @return something like adipose_subcutaneous
     */
    String getTissueName(String fname) {
        int i = fname.indexOf("_");
        if (i<0) throw new SbasRuntimeException("Malformed AS filename " + fname);
        String tissue = fname.substring(i+1);
        i = tissue.indexOf("_AS_model_B_sex_as_events.csv");
        if (i<0) throw new SbasRuntimeException("Malformed AS filename " + fname);
        return tissue.substring(0,i);
    }

    private void parseFiles(File dgeDir, List<String> files) {
        for (String file : files) {
            parseAs(dgeDir, file);
        }
    }

    /**
     * [0] genesymbol-id, e.g., SDF4-1937
     * [1] logFC
     * [2] AveExpr
     * [3] t
     * [4] P.Value
     * [5] adj.P.Val
     * [6] B
     * The filename is like a3ss_adipose_subcutaneous_AS_model_B_sex_as_events.csv,
     * so we get the kind of
     * @param dasDir directory with AS data
     * @param name filename
     */
    private void parseAs(File dasDir, String name) {
        Path p = Paths.get(dasDir.getAbsolutePath(), name);
        File f = p.toFile();
        SpliceType stype = SpliceType.fromDasFileName(name);
        String tissue = getTissueName(name);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            br.readLine(); // discard header
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split(",");
                if (fields.length != 7) {
                    for (int i=0; i < fields.length; i++) {
                        System.out.printf("%d) %s\n", i, fields[i]);
                    }
                    throw new SbasRuntimeException("Wrong number of fields in DAS file");
                }
                String symbolId = fields[0];
                int j = symbolId.lastIndexOf("-");
                if (j < 0) {
                    throw new SbasRuntimeException("Wrong number of fields in symbolID");
                }
                String symbol = symbolId.substring(0,j);
                int id = Integer.parseInt(symbolId.substring(j+1));
                double logFC = Double.parseDouble(fields[1]);
                double aveExp = Double.parseDouble(fields[2]);
                double t = Double.parseDouble(fields[3]);
                double pval = Double.parseDouble(fields[4]);
                double adjPval = Double.parseDouble(fields[5]);
                double B = Double.parseDouble(fields[6]);
                allGeneSymbols.add(symbol);
                totalAlternativeSplicintEventCount++;
                if (adjPval>0.05) {
                    continue;
                }
                significantEventCount++;
                Das das = new Das(symbol, id, logFC, aveExp, t, pval, adjPval, B,stype, tissue);
                tissue2asMap.putIfAbsent(tissue, new ArrayList<>());
                tissue2asMap.get(tissue).add(das);
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }

    private static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    public Map<String, List<Das>> getTissue2asMap() {
        return tissue2asMap;
    }

    public Set<String> getAllGeneSymbols() {
        return allGeneSymbols;
    }

    public int getTotalAlternativeSplicintEventCount() {
        return totalAlternativeSplicintEventCount;
    }

    public int getSignificantEventCount() {
        return significantEventCount;
    }
}
