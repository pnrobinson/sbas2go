package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FromGtfParser {
    /**
     * Key-one of the five RMATS splice types
     * Value-a map whose key is the event number and the value is a {@link FromGtf} object.
     */
    private final Map<SpliceType, Map<Integer, FromGtf>> gtfMap;

    /**
     *
     * @param gtfDir directory with individual GTF files
     */
    public FromGtfParser(File gtfDir) {
        gtfMap = new HashMap<>();
        for (String gtfFile : gtfDir.list()) {
            if (gtfFile.contains("novelEvents")) {
                continue; // these files are empty
            }
            Path gtfpath = Paths.get(gtfDir.getAbsolutePath(), gtfFile);
            SpliceType stype = SpliceType.fromGtfFileName(gtfpath.toString());
            Map<Integer, FromGtf> fromGtfMap = parseGtfFile(gtfpath.toFile(), stype);
            gtfMap.put(stype, fromGtfMap);
        }
    }

    private Map<Integer, String> columnNames(String line) {
        String [] columnNames = line.split("\t");
        Map<Integer, String> names = new HashMap<>();
        for (int i=0; i < columnNames.length; i++) {
            names.put(i, columnNames[i]);
        }
        return Map.copyOf(names);
    }

    public Map<Integer, FromGtf> parseGtfFile(File gtfFile, SpliceType stype) {
        Map<Integer, FromGtf> fromGtfMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gtfFile))){
            String line = br.readLine(); // header
            Map<Integer, String> columns = columnNames(line);
            while((line = br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split("\t");
                if (fields.length != columns.size()) {
                    // should never happen
                    throw new SbasRuntimeException("Mismatched length header/columns");
                }
                int id = Integer.parseInt(fields[0]);
                String geneID = fields[1].replaceAll("\"", "");
                String symbol = fields[2].replaceAll("\"", "");
                String chr = fields[3];
                String strand = fields[4];
                int exonStart = Integer.parseInt(fields[5]);
                int exonEnd = Integer.parseInt(fields[6]);
                // from this point on, the format depends on the splice type.
                /// therefore, we use the map to get the remaining items.
                Map<String, Integer> positions = new HashMap<>();
                for (int i=7; i < fields.length; i++) {
                    String fieldname = columns.get(i);
                    Integer value = Integer.parseInt(fields[i]);
                    positions.put(fieldname, value);
                }
                FromGtf fromGtf = new FromGtf(stype, id, geneID, symbol, chr, strand, exonStart, exonEnd, positions);
                fromGtfMap.put(id, fromGtf);
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
        return fromGtfMap;
    }

    public Map<SpliceType, Map<Integer, FromGtf>> getGtfMap() {
        return gtfMap;
    }
}
