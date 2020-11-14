package org.jax.sbas2go.go;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GoResultPrinter {

    private double ALPHA = 0.05;
    private final List<GoResultSet> resultList;

    private final Ontology ontology;

    public GoResultPrinter(List<GoResultSet> results, Ontology ontology) {
        this.resultList = results;
        this.ontology = ontology;
    }

    private String getLine(String tissue, int study, int pop, GoTerm2PValAndCounts gt) {
        int annotatedStudy = gt.getAnnotatedStudyGenes();
        int annotatedPop = gt.getAnnotatedPopulationGenes();
        TermId tid = gt.getItem();
        Term term = ontology.getTermMap().get(tid);
        String label = "n/a";
        if (term != null) {
            label = term.getName();
        }
        double adjP = gt.getAdjustedPValue();
        double P = gt.getRawPValue();
        return String.format("%s\t%s\t%s\t%d/%d(%.1f%%)\t%d/%d(%.1f%%)\t%f\t%f", tissue, tid.getValue(), label,
                annotatedStudy, study, 100.0*annotatedStudy/study,
                annotatedPop, pop, 100.0*annotatedPop/pop,
                P, adjP);
    }

    public void output(String fname) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))) {
            for (var result : resultList) {
                String tissue = result.getName();
                int studySize = result.getStudysize();
                int popSize = result.getPopsize();
                List<GoTerm2PValAndCounts> pvals = result.getPvals();
                for (var gt : pvals) {
                    if (gt.passesThreshold(ALPHA)) {
                        String line = getLine(tissue, studySize, popSize, gt);
                        bw.write(line + "\n");
                    }
                }

            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }



}
