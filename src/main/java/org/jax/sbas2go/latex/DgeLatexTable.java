package org.jax.sbas2go.latex;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermId;

import javax.sql.RowSetReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DgeLatexTable {

    private final String prefix;
    private final List<ResultRow> results;
    private final List<String> tissues;
    /** List of terms seem at least 3 times. */
    private final List<String> terms;

    private final Map<String, String> tissuemap;

    private final static int THRESHOLD = 8;


    public DgeLatexTable(String prefix, List<ResultRow> resultRowList) {
        this.prefix = prefix;
        tissuemap = createTissueMap();
        results = resultRowList.stream()
                .filter(ResultRow::isDge)
                .collect(Collectors.toList());
        this.tissues = new ArrayList<>(results.stream()
                .map(ResultRow::getTissue)
                .collect(Collectors.toSet()));

        Map<String, Integer> countMap = new HashMap<>();
        for (var r : results) {
            String label = r.getLabel();
            countMap.putIfAbsent(label, 0);
            countMap.merge(label, 1, Integer::sum);
        }
        terms = new ArrayList<>();
        for (var e : countMap.entrySet()) {
            if (e.getValue() > THRESHOLD) {
                terms.add(e.getKey());
            }
        }
        System.out.printf("[INFO] Got %d terms with more than %d significant tissues", terms.size(), THRESHOLD);
    }


    private Map<String, String> createTissueMap() {
        Map<String,String> tissuemap = new HashMap<>();
        tissuemap.put("nerve_tibial", "tibial nerve");
        tissuemap.put("cells_cultured_fibroblasts", "fibroblasts");
        tissuemap.put("brain_spinal_cord_cervical_c_1", "spinal cord");
        tissuemap.put("adipose_subcutaneous","Adipose (sc)");
        tissuemap.put("adipose_visceral_omentum","Adipose (v)");
        tissuemap.put("AdrenalGland","Adrenal gland");
        tissuemap.put("artery_aorta","Aorta");
        tissuemap.put("Artery-Coronary","Coronary artery");
        tissuemap.put("artery_tibial","Tibial artery");
        tissuemap.put("Brain-Caudate(basalganglia)","Caudate");
        tissuemap.put("brain_cerebellar_hemisphere","Cerebellar hemisphere");
        tissuemap.put("brain_cerebellum","Cerebellum");
        tissuemap.put("Brain-Cortex","Cortex");
        tissuemap.put("brain_frontal_cortex_ba_9","Frontal cortex");
        tissuemap.put("brain_hippocampus","Hippocampus");
        tissuemap.put("brain_hypothalamus","Hypothalamus");
        tissuemap.put("brain_nucleus_accumbens_basal_ganglia","Nucleus accumbens");
        tissuemap.put("Brain-Putamen(basalganglia)","Putamen");
        tissuemap.put("Brain-Spinalcord(cervicalc-1)","Spinal cord");
        tissuemap.put("breast_mammary_tissue","Breast");
        tissuemap.put("Cells-Culturedfibroblasts","Fibroblasts");
        tissuemap.put("cells_ebv_transformed_lymphocytes","EBV-lymphocytes");
        tissuemap.put("Colon-Sigmoid","Sigmoid colon");
        tissuemap.put("colon_transverse","Transverse colon");
        tissuemap.put("esophagus_gastroesophageal_junction","Esophagus (gej)");
        tissuemap.put("esophagus_mucosa","Esophagus (m)");
        tissuemap.put("esophagus_muscularis","Esophagus (mu)");
        tissuemap.put("heart_atrial_appendage","Atrial appendage");
        tissuemap.put("heart_left_ventricle","Left ventricle");
        tissuemap.put("liver","Liver");
        tissuemap.put("lung","Lung");
        tissuemap.put("muscle_skeletal","Skeletal muscle");
        tissuemap.put("Nerve-Tibial","Tibial nerve");
        tissuemap.put("pancreas","Pancreas");
        tissuemap.put("pituitary","Pituitary");
        tissuemap.put("skin_not_sun_exposed_suprapubic","Skin (not exposed)");
        tissuemap.put("skin_sun_exposed_lower_leg","Skin (exposed)");
        tissuemap.put("small_intestine_terminal_ileum","Small intestine");
        tissuemap.put("spleen","Spleen");
        tissuemap.put("stomach","Stomach");
        tissuemap.put("thyroid","Thyroid");
        tissuemap.put("whole_blood","Whole blood");
       return tissuemap;
    }



    private void outputHeader(Writer writer) throws IOException {
        writer.write("\\documentclass[11pt]{article} \n");
        writer.write("\\usepackage[table]{xcolor} \n");
        writer.write("\\usepackage{booktabs}\n");
        writer.write("\\usepackage{pifont} \n");
        writer.write("\\usepackage{adjustbox}\n" +
                "\\usepackage{array}\n" +
                "\\usepackage{booktabs}\n" +
                "\\usepackage{multirow}\n");
        writer.write("\\newcolumntype{R}[2]{%\n" +
                "    >{\\adjustbox{angle=#1,lap=\\width-(#2)}\\bgroup}%\n" +
                "    l%\n" +
                "    <{\\egroup}%\n" +
                "}\n" +
                "\\newcommand*\\rot{\\multicolumn{1}{R{90}{1em}}}% \n");
        writer.write("\\begin{document}\n");
        writer.write("\\begin{table*}  \n");
        writer.write("\\centering \n");
        String repeated = "|l".repeat(terms.size());
        writer.write("\\begin{scriptsize}  \n");
        writer.write(String.format("\\begin{tabular}{p{5cm}%s|}\n", repeated));
        //writer.write("\\toprule \n");
    }

    private void outputTermline(Writer writer) throws IOException {
        writer.write("\\multicolumn{1}{c}{Tissue}");
        for (var label : terms) {
            writer.write("& \\rot{ " + label + "}\n");
        }
        writer.write("\\\\ \n");
        writer.write("\\hrule \n");
    }


    private void outputRow(Writer writer, String tissue) throws IOException {
        String tis;
        if (tissuemap.containsKey(tissue)) {
            tis = tissuemap.get(tissue);
        } else {
            throw new SbasRuntimeException("Bad tissue " + tissue);
        }

        writer.write(tis);
        List<ResultRow> tissueResults = this.results.stream()
                .filter(result -> result.getTissue().equals(tissue))
                .collect(Collectors.toList());
        for (var term : terms) {
            if (tissueResults.stream().map(ResultRow::getLabel).filter(value -> value.equals(term)).findAny().isPresent()) {
                writer.write("& \\cellcolor{blue!25} \\ding{51} ");
            } else {
                writer.write("& ");
            }
        }
        writer.write("\\\\ \n");
        writer.write("\\hline \n");

    }

    private void outputFooter(Writer writer) throws IOException {
        //writer.write("\\bottomrule \n");
        writer.write("\\end{tabular} \n");
        writer.write("\\end{scriptsize}  \n");
        writer.write("\\end{table*} \n");
        writer.write("\\end{document} \n");
    }

    public void outputTable() {
        String fname = String.format("%s-dge.tex", this.prefix);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            outputHeader(bw);
            outputTermline(bw);
            for (var t : tissues) {
                outputRow(bw, t);
            }
            outputFooter(bw);
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }

}
