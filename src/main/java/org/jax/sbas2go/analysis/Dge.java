package org.jax.sbas2go.analysis;

public class Dge {

    private final String ensg;
    private final int version;
    private final double fc;
    private final double aveExp;
    private final double t;
    private final double pval;
    private final double adjPval;
    private final double B;

    public Dge(String ensg, double fc, double aveExp, double t, double pval, double adjPval, double B) {
        int i = ensg.indexOf(".");
        if (i < 0) {
            this.ensg = ensg;
            this.version = 0;
        } else {
            this.ensg = ensg.substring(0,i);
            this.version = Integer.parseInt(ensg.substring(i+1));
        }
        this.fc = fc;
        this.aveExp = aveExp;
        this.t = t;
        this.pval = pval;
        this.adjPval = adjPval;
        this.B = B;
    }

    public String getEnsg() {
        return ensg;
    }

    public int getVersion() {
        return version;
    }

    public double getFc() {
        return fc;
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
}
