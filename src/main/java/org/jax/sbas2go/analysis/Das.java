package org.jax.sbas2go.analysis;

import org.jax.sbas2go.gtf.SpliceType;

/**
 * Differential splicing
 */
public class Das {
    private final String symbol;
    private final int id;
    private final double logFC;
    private final double aveExp;
    private final double t;
    private final double pval;
    private final double adjPval;
    private final double B;
    private final SpliceType stype;
    private final String tissue;

    public Das(String symbol,
               int id,
               double logFC,
               double aveExp,
               double t,
               double pval,
               double adjPval,
               double B,
               SpliceType stype,
               String tissue) {
        this.symbol = symbol;
        this.id = id;
        this.logFC = logFC;
        this.aveExp = aveExp;
        this.t = t;
        this.pval = pval;
        this.adjPval = adjPval;
        this.B = B;
        this.stype = stype;
        this.tissue = tissue;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getId() {
        return id;
    }

    public double getLogFC() {
        return logFC;
    }

    public double getAveExp() {
        return aveExp;
    }

    public double getT() {
        return t;
    }

    public double getPval() {
        return pval;
    }

    public double getAdjPval() {
        return adjPval;
    }

    public double getB() {
        return B;
    }

    public SpliceType getStype() {
        return stype;
    }

    public String getTissue() {
        return tissue;
    }
}
