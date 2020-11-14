package org.jax.sbas2go.latex;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ingest the files das2go.tsv and dge2go.tsv that are created by the {@code go} command
 */
public class SbasLaTeXParser {


    List<ResultRow> resultRowList;

    public SbasLaTeXParser(){
        File dge = new File("dge2go.tsv");
        File das = new File("das2go.tsv");
        if (! dge.exists()) {
            throw new SbasRuntimeException("Could not find dge2go file - did you run the go command?");
        }
        if (! das.exists()){
            throw new SbasRuntimeException("Could not find das2go file - did you run the go command?");
        }
        String line;
        resultRowList = new ArrayList<>();
        // nerve_tibial	GO:0098742	cell-cell adhesion via plasma-membrane adhesion molecules	22/656(3.4%)	129/11270(1.1%)	0.000005	0.020397
        try(BufferedReader br = new BufferedReader(new FileReader(dge))) {
            while ((line = br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length != 7) {
                    throw new SbasRuntimeException("Malformed dge2go line: " + line);
                }
                ResultRow row = new ResultRow(fields, "DGE");
                resultRowList.add(row);
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }

        try(BufferedReader br = new BufferedReader(new FileReader(das))) {
            while ((line = br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length != 7) {
                    throw new SbasRuntimeException("Malformed das2go line: " + line);
                }
                ResultRow row = new ResultRow(fields, "DAS");
                resultRowList.add(row);
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
        System.out.printf("[INFO] Extracted %d result rows.\n", resultRowList.size());
    }


    public List<ResultRow> getResultRowList() {
        return resultRowList;
    }
}
