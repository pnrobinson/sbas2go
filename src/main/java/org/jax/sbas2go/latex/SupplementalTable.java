package org.jax.sbas2go.latex;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SupplementalTable {

    private final String prefix;
    private final List<ResultRow> resultRows;
    //esophagus_muscularis	GO:0010608	posttranscriptional regulation of gene expression	18/138(13.0%)	433/11270(3.8%)	0.000005	0.009013
    private final String [] header = {"GO term", "GO id", "tissue", "category","study set", "population", "p.value", "adj.p.value"};

    public SupplementalTable(String prefix, List<ResultRow> resultRowList) {
        this.prefix = prefix;
        this.resultRows = resultRowList;
    }

    private String getRow(ResultRow result) {
        List<String> fields = new ArrayList<>();
        fields.add(result.getTid().getValue());
        fields.add(result.getLabel());
        fields.add(result.getTissue());
        if (result.isDge()) {
            fields.add("DGE");
        } else {
            fields.add("DAS");
        }
        fields.add(result.getStudyResults());
        fields.add(result.getPopResults());
        fields.add(String.valueOf(result.getPval()));
        fields.add(String.valueOf(result.getAdjP()));
        return String.join(",", fields);
    }

    public void outputTable() {
        String fname = String.format("%s-go-analysis.csv", this.prefix);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            bw.write(String.join(",", header) + "\n");
            for (ResultRow result : resultRows) {
                bw.write(getRow(result) + "\n");
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }
}
