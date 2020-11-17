package org.jax.sbas2go.gtf;

import java.util.Map;

public class FromGtf {
    private final SpliceType spliceType;
    private final int id;
    private final String geneID;
    private final String symbol;
    private final String chr;
    private final String strand;
    private final int exonStart;
    private final int exonEnd ;
    private final Map<String, Integer> positions;

    public FromGtf(SpliceType spliceType,
                   int id,
                   String geneID,
                   String symbol,
                   String chr,
                   String strand,
                   int exonStart,
                   int exonEnd,
                   Map<String, Integer> positions) {
        this.spliceType = spliceType;
        this.id = id;
        this.geneID = geneID;
        this.symbol = symbol;
        this.chr = chr;
        this.strand = strand;
        this.exonStart = exonStart;
        this.exonEnd = exonEnd;
        this.positions = Map.copyOf(positions);
    }

    public SpliceType getSpliceType() {
        return spliceType;
    }

    public int getId() {
        return id;
    }

    public String getGeneID() {
        return geneID;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getChr() {
        return chr;
    }

    public String getStrand() {
        return strand;
    }

    public int getExonStart() {
        return exonStart;
    }

    public int getExonEnd() {
        return exonEnd;
    }

    public Map<String, Integer> getPositions() {
        return positions;
    }
}
