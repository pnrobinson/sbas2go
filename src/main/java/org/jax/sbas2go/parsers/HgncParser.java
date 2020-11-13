package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.File;

public class HgncParser {
    public HgncParser() {
        String path = "data/non_alt_loci_set.txt";
        File f = new File(path);
        if (! f.exists()) {
            throw new SbasRuntimeException("Could not find HGNC file");
        }
    }
}
