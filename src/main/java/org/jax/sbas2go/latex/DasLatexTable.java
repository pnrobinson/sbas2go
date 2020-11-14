package org.jax.sbas2go.latex;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DasLatexTable {
    private final String prefix;
    private final List<ResultRow> results;
    private final Map<String, String> tissuemap;

    public DasLatexTable(String prefix, List<ResultRow> resultRowList) {
        this.prefix = prefix;
        tissuemap = DgeLatexTable.createTissueMap();
        results = resultRowList.stream()
                .filter(ResultRow::isDas)
                .collect(Collectors.toList());
    }


    private final String [] header = {"GO term", "tissue", "study", "population", "p-value", "adj. p-value"};

    private String getHeaderRow() {
        List<String> items = Arrays.stream(header)
                .map(value -> String.format("\\textbf{%s}", value))
                .collect(Collectors.toList());
        return String.join(" & ", items);

    }
    private void outputRow(Writer writer, ResultRow result) throws IOException {
        List<String> fields = new ArrayList<>();
        fields.add(result.getLabel() + "\\\\ " + result.getTid().getValue());
        fields.add(tissuemap.get(result.getTissue()));
        fields.add(result.getStudyResults().replace("%", "\\%"));
        fields.add(result.getPopResults().replace("%", "\\%"));
        fields.add(String.valueOf(result.getPval()));
        fields.add(String.valueOf(result.getAdjP()));
        writer.write(String.join(" & ", fields) + "\\\\ \n");
    }

    private void outputFooter(Writer writer) throws IOException {
        //writer.write("\\bottomrule \n");
        writer.write("\\end{longtable} \n");
        writer.write("\\end{scriptsize} \n");
        writer.write("\\end{document} \n");
    }

    private void outputHeader(Writer writer) throws IOException {
        writer.write("\\documentclass[11pt]{article} \n");
        writer.write("\\usepackage[table]{xcolor} \n");
        writer.write("\\usepackage{booktabs}\n");
        String repeated = "p{4.5cm}" + "l".repeat(header.length - 1);
        writer.write("\\usepackage{longtable}\n");
        writer.write("\\begin{document}\n");
        writer.write("\\begin{scriptsize}\n");
        writer.write(String.format("\\begin{longtable} {%s} \n", repeated));
        writer.write("\\caption{GO Analysis of differentially spliced genes.}\\\\\n" +
                "\\hline\n" +
                getHeaderRow() + " \\\\\n" +
                "\\hline\n" +
                "\\endfirsthead\n");
        writer.write(String.format("\\multicolumn{%d}{c}%%\n", header.length));
        writer.write(
                "{\\tablename\\ \\thetable\\ -- \\textit{Continued from previous page}} \\\\\n" +
                "\\hline\n" +
                        getHeaderRow() + " \\\\\n" +
                "\\hline\n" +
                "\\endhead\n");
        writer.write(String.format("\\hline \\multicolumn{%d}{r}%%\n", header.length));
        writer.write("{\\textit{Continued on next page}} \\\\\n" +
                "\\endfoot\n" +
                "\\hline\n" +
                "\\endlastfoot \n");
    }


    public void outputTable() {
        String fname = String.format("%s-das.tex", this.prefix);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
          outputHeader(bw);
          for (ResultRow resultRow : results) {
              outputRow(bw, resultRow);
          }
          outputFooter(bw);
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }
}
