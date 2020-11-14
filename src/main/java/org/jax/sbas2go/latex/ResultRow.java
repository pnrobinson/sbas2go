package org.jax.sbas2go.latex;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Store results for output
 *  // nerve_tibial	GO:0098742	cell-cell adhesion via plasma-membrane adhesion molecules	22/656(3.4%)	129/11270(1.1%)	0.000005	0.020397
 */
public class ResultRow {
    private final String tissue;
    private final String type;
    private final TermId tid;
    private final String label;
    private final String studyResults;
    private final String popResults;
    private final double pval;
    private final double adjP;
    public ResultRow(String [] fields, String category) {
        this.tissue = fields[0];
        this.tid = TermId.of(fields[1]);
        this.label = fields[2];
        this.studyResults = fields[3];
        this.popResults = fields[4];
        this.pval = Double.parseDouble(fields[5]);
        this.adjP = Double.parseDouble(fields[6]);
        this.type = category;
    }

    public String getTissue() {
        return tissue;
    }

    public boolean isDas() {
        return type.equals("DAS");
    }

    public boolean isDge() {
        return type.equals("DGE");
    }

    public TermId getTid() {
        return tid;
    }

    public String getLabel() {
        return label;
    }

    public String getStudyResults() {
        return studyResults;
    }

    public String getPopResults() {
        return popResults;
    }

    public double getPval() {
        return pval;
    }

    public double getAdjP() {
        return adjP;
    }
}
