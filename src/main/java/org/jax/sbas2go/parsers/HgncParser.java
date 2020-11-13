package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * [0] hgnc_id
 * [1] symbol
 * [2] name
 * [3]locus_group
 * [4]locus_type
 * [5]status
 * [6]location
 * [7]location_sortable
 * [8]alias_symbol
 * [9]alias_name
 * [10]prev_symbol
 * [11]prev_name
 * [12]gene_family
 * [13]gene_family_id
 * [14]date_approved_reserved
 * [15]date_symbol_changed
 * [16]date_name_changed
 * [17]date_modified
 * [18]entrez_id
 * [19]ensembl_gene_idvega_id
 * ucsc_id	ena	refseq_accession	ccds_id	uniprot_ids	pubmed_id	mgd_id	rgd_id	lsdb	cosmic	omim_id	mirbase	homeodb	snornabase	bioparadigms_slc	orphanet	pseudogene.org	horde_id	merops	imgt	iuphar	kznf_gene_catalog	mamit-trnadb	cd	lncrnadb	enzyme_id	intermediate_filament_db	rna_central_ids	lncipedia	gtrnadbagr
 */
public class HgncParser {
    /** key, an ensembl ID (without version number), value, a gene symbol. */
    private final Map<String, String> ensembl2symbolMap;
    private final Map<String, TermId> ensembl2termIdMap;
    private final Map<String, TermId> symbol2termIdMap;

    public HgncParser() {
        String path = "data/non_alt_loci_set.txt";
        File f = new File(path);
        if (! f.exists()) {
            throw new SbasRuntimeException("Could not find HGNC file");
        }
        ensembl2symbolMap = new HashMap<>();
        ensembl2termIdMap = new HashMap<>();
        symbol2termIdMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // discard header
            while ((line = br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length < 20) {
                    //System.err.printf("[WARNING] Short HGNC line(%s)\n.", line);
                    // these are mainly things like fragile sites or unknown etc
                    continue;
                }
                String symbol = fields[1];
                String ensemblId = fields[19];
                String geneId = fields[18];

                //System.out.println(symbol + ": " + ensemblId);
                ensembl2symbolMap.put(ensemblId, symbol);
                if (geneId != null && geneId.length()>0) {
                    TermId tid = TermId.of("NCBIGene", fields[18]);
                    ensembl2termIdMap.put(ensemblId, tid);
                    symbol2termIdMap.put(symbol, tid);
                }
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
        System.out.printf("[INFO] Extracted %d ensembl mappings.\n", ensembl2symbolMap.size());
    }


    public Map<String, String> getEnsembl2symbolMap() {
        return ensembl2symbolMap;
    }

    public Map<String, TermId> getEnsembl2termIdMap() {
        return ensembl2termIdMap;
    }

    public Map<String, TermId> getSymbol2termIdMap() {
        return symbol2termIdMap;
    }
}
