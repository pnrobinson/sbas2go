package org.jax.sbas2go.latex;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResultWriter {
    private final String prefix;
    private final List<ResultRow> resultRowList;

    public ResultWriter(String prefix, List<ResultRow> results) {
        this.prefix = prefix;
        this.resultRowList = results;
    }


    private Map<String, Integer> countTerms(List<ResultRow> results) {
        Map<String, Integer> countMap = new HashMap<>();
        for (var r : results) {
            String label = r.getLabel();
            countMap.putIfAbsent(label, 0);
            countMap.merge(label, 1, Integer::sum);
        }
        return countMap;
    }

    public void outputLatexTable() {
        DgeLatexTable table = new DgeLatexTable(this.prefix, resultRowList);
        table.outputTable();
    }

    public void outputSupplement() {
        SupplementalTable table = new SupplementalTable(this.prefix, resultRowList);
        table.outputTable();
    }

    public void outputDasTable() {
        DasLatexTable dasLatexTable = new DasLatexTable(this.prefix, resultRowList);
        dasLatexTable.outputTable();
    }


    public void printSummary() {
        String fname = String.format("%s-summary.txt", this.prefix);
        int dasResults = (int)resultRowList.stream().filter(ResultRow::isDas).count();
        int dgeResults = (int)resultRowList.stream().filter(ResultRow::isDge).count();
        Set<String> dgeTissues = resultRowList.stream()
                .filter(ResultRow::isDge)
                .map(ResultRow::getTissue)
                .collect(Collectors.toSet());
        Set<String> dasTissues = resultRowList.stream()
                .filter(ResultRow::isDas)
                .map(ResultRow::getTissue)
                .collect(Collectors.toSet());
        Set<TermId> dgeTermids = resultRowList.stream()
                .filter(ResultRow::isDge)
                .map(ResultRow::getTid)
                .collect(Collectors.toSet());
        Set<TermId> dasTermids = resultRowList.stream()
                .filter(ResultRow::isDas)
                .map(ResultRow::getTid)
                .collect(Collectors.toSet());
        Map<String, Integer> dgeCounts = countTerms(resultRowList.stream().filter(ResultRow::isDge).collect(Collectors.toList()));
        Map<String, Integer> dasCounts = countTerms(resultRowList.stream().filter(ResultRow::isDas).collect(Collectors.toList()));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(String.format("DAS.results: %d\n", dasResults));
            bw.write(String.format("DGE.results: %d\n", dgeResults));
            bw.write(String.format("DAS.n.tissues: %d\n", dasTissues.size()));
            bw.write(String.format("DGE.n.tissues: %d\n", dgeTissues.size()));
            bw.write(String.format("DAS.n.terms: %d\n", dasTermids.size()));
            bw.write(String.format("DGE.n.terms: %d\n", dgeTermids.size()));
            for (var e : dgeCounts.entrySet()) {
                if (e.getValue() < 2) continue;
                bw.write(String.format("DGE.count(%s): %d\n", e.getKey(), e.getValue()));
            }
            for (var e : dasCounts.entrySet()) {
                if (e.getValue() < 2) continue;
                bw.write(String.format("DAS.count(%s): %d\n", e.getKey(), e.getValue()));
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }

}
