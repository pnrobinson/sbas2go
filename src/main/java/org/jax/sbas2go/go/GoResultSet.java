package org.jax.sbas2go.go;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.List;

/**
 * This class is designed to organize the results from analysis of studysets
 */
public class GoResultSet {

    private final String name;
    private final int studysize;
    private final int popsize;
    private final List<GoTerm2PValAndCounts> pvals;
    private final double ALPHA;

    public GoResultSet(String name, int studysize, int popsize, List<GoTerm2PValAndCounts> pvals, double alpha) {
        this.name = name;
        this.studysize = studysize;
        this.popsize = popsize;
        this.pvals = pvals;
        this.ALPHA = alpha;
    }



    public void dump(Ontology ontology) {
        int n_sig = 0;
        System.out.printf("[INFO] Study set: %d genes. Population set: %d genes%n",
                studysize, popsize);
        for (GoTerm2PValAndCounts item : pvals) {
            double pval = item.getRawPValue();
            double pval_adj = item.getAdjustedPValue();
            TermId tid = item.getItem();
            Term term = ontology.getTermMap().get(tid);
            if (term == null) {
                System.err.println("[ERROR] Could not retrieve term for " + tid.getValue());
                continue;
            }
            String label = term.getName();
            if (pval_adj > ALPHA) {
                continue;
            }
            n_sig++;
            double studypercentage = 100.0 * (double) item.getAnnotatedStudyGenes() / studysize;
            double poppercentage = 100.0 * (double) item.getAnnotatedPopulationGenes() / popsize;
            System.out.printf("%s [%s]: %.2e (adjusted %.2e). Study: n=%d (%.1f%%); population: N=%d (%.1f%%)%n",
                    label, tid.getValue(), pval, pval_adj, item.getAnnotatedStudyGenes(), studypercentage,
                    item.getAnnotatedPopulationGenes(), poppercentage);
            System.out.printf("%d of %d terms were significant at alpha %.7f%n", n_sig, pvals.size(), ALPHA);
        }
    }

    public String getName() {
        return name;
    }

    public int getStudysize() {
        return studysize;
    }

    public int getPopsize() {
        return popsize;
    }

    public List<GoTerm2PValAndCounts> getPvals() {
        return pvals;
    }

    public double getALPHA() {
        return ALPHA;
    }
}
