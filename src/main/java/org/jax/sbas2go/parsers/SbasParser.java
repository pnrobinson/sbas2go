package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class SbasParser {

    private final Map<SpliceType, Map<Integer, FromGtf>> gtfMap;

    public SbasParser(String dir) {
        File directory = new File(dir);
        if (! directory.exists() && directory.isDirectory()) {
            throw new SbasRuntimeException("Invalid sbas data directory");
        }
        Path fromGtfPath = Paths.get(directory.getAbsolutePath(), "fromGTF");
        File fromGtfFile = fromGtfPath.toFile();
        if (! fromGtfFile.exists() && fromGtfFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find fromGTF directory");
        }
        FromGtfParser fromGtfParser = new FromGtfParser(fromGtfFile);
        this.gtfMap = fromGtfParser.getGtfMap();
    }
}
