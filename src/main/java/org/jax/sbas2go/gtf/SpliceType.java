package org.jax.sbas2go.gtf;

import org.jax.sbas2go.except.SbasRuntimeException;

public enum SpliceType {
    SE, MXE, RI, A3SS, A5SS;

    public static SpliceType fromGtfFileName(String fname) {
        if (fname.contains("A3SS.txt")) return A3SS;
        else if (fname.contains("A5SS.txt")) return A5SS;
        else if (fname.contains("RI.txt")) return RI;
        else if (fname.contains("MXE.txt")) return MXE;
        else if (fname.contains("SE.txt")) return SE;
        else
            throw new SbasRuntimeException("Could not identify SpliceType from " + fname);
    }
}
