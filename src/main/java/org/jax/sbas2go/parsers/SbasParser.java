package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SbasParser {

    //private final Map<SpliceType, Map<Integer, FromGtf>> gtfMap;

    public SbasParser(String dir) {
        File directory = new File(dir);
        if (! directory.exists() && directory.isDirectory()) {
            throw new SbasRuntimeException("Invalid sbas data directory");
        }
       /* Path fromGtfPath = Paths.get(directory.getAbsolutePath(), "fromGTF");
        File fromGtfFile = fromGtfPath.toFile();
        if (! fromGtfFile.exists() && fromGtfFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find fromGTF directory");
        }
        FromGtfParser fromGtfParser = new FromGtfParser(fromGtfFile);
        this.gtfMap = fromGtfParser.getGtfMap();
        */
        Path dgePath = Paths.get(directory.getAbsolutePath(), "dge");
        File dgeFile = dgePath.toFile();
        if (! dgeFile.exists() && dgeFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDgeParser dgeParser = new SbasDgeParser(dgeFile);
        Path dasPath = Paths.get(directory.getAbsolutePath(), "as");
        File dasFile = dasPath.toFile();
        if (! dasFile.exists() && dasFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDasParser dasParser = new SbasDasParser(dasFile);
    }
}
