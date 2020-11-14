package org.jax.sbas2go.parsers;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.except.SbasRuntimeException;
import org.jax.sbas2go.gtf.FromGtf;
import org.jax.sbas2go.gtf.SpliceType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SbasParser {

    private final Map<SpliceType, Map<Integer, FromGtf>> gtfMap;

    private final Map<String, String> ensemblGeneIdToSymbolMap;

    private final Set<String> ensembleDgePopulation;

    private final Map<String, Set<Dge>> tissueSpecificDgeMap;

    private final Map<String, List<Das>> tissue2asMap;
    private final Set<String> allGeneSymbols;
    /** We do not include all of the tissues in our analysis, however, files were made for
     * all files. We exclude tissues from our GO analysis if they were not included in the
     * main analysis.
     */
    private final Map<String, Boolean> includedTissues;


    public SbasParser(String dir) {
        File directory = new File(dir);
        this.includedTissues = createIncludedMap();
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

        Path dgePath = Paths.get(directory.getAbsolutePath(), "dge");
        File dgeFile = dgePath.toFile();
        if (! dgeFile.exists() && dgeFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDgeParser dgeParser = new SbasDgeParser(dgeFile);
        this.ensemblGeneIdToSymbolMap = dgeParser.getEnsemblIdToSymbolMap();
        this.ensembleDgePopulation = dgeParser.getEnsemblPopulation();
        this.tissueSpecificDgeMap = dgeParser.getTissueSpecificDiffernetial();
        Path dasPath = Paths.get(directory.getAbsolutePath(), "as");
        File dasFile = dasPath.toFile();
        if (! dasFile.exists() && dasFile.isDirectory()) {
            throw new SbasRuntimeException("Could not find dge directory");
        }
        SbasDasParser dasParser = new SbasDasParser(dasFile);
        this.tissue2asMap = dasParser.getTissue2asMap();
        this.allGeneSymbols = dasParser.getAllGeneSymbols();
    }



    private Map<String, Boolean>  createIncludedMap() {
        Map<String, Boolean> included = new HashMap<>();
        included.put("colon_sigmoid", true);
        included.put("colon_transverse", true);
        included.put("brain_cortex", true);
        included.put("brain_putamen_basal_ganglia", true);
        included.put("pituitary", true);
        included.put("muscle_skeletal", true);
        included.put("brain_hypothalamus", true);
        included.put("spleen", true);
        included.put("brain_cerebellum", true);
        included.put("adipose_visceral_omentum", true);
        included.put("nerve_tibial", true);
        included.put("adrenal_gland", true);
        included.put("brain_spinal_cord_cervical_c_1", true);
        included.put("brain_cerebellar_hemisphere", true);
        included.put("stomach", true);
        included.put("brain_caudate_basal_ganglia", true);
        included.put("brain_hippocampus", true);
        included.put("artery_aorta", true);
        included.put("liver", true);
        included.put("pancreas", true);
        included.put("esophagus_gastroesophageal_junction", true);
        included.put("cells_cultured_fibroblasts", true);
        included.put("whole_blood", true);
        included.put("thyroid", true);
        included.put("skin_sun_exposed_lower_leg", true);
        included.put("small_intestine_terminal_ileum", true);
        included.put("lung", true);
        included.put("adipose_subcutaneous", true);
        included.put("artery_coronary", true);
        included.put("skin_not_sun_exposed_suprapubic", true);
        included.put("cells_ebv_transformed_lymphocytes", true);
        included.put("breast_mammary_tissue", true);
        included.put("brain_frontal_cortex_ba_9", true);
        included.put("esophagus_mucosa", true);
        included.put("esophagus_muscularis", true);
        included.put("brain_nucleus_accumbens_basal_ganglia", true);
        included.put("artery_tibial", true);
        included.put("heart_left_ventricle", true);
        included.put("heart_atrial_appendage", true);
        return included;
    }

    public Map<SpliceType, Map<Integer, FromGtf>> getGtfMap() {
        return gtfMap;
    }

    public Map<String, String> getEnsemblGeneIdToSymbolMap() {
        return ensemblGeneIdToSymbolMap;
    }

    public Set<String> getEnsembleDgePopulation() {
        return ensembleDgePopulation;
    }

    public Map<String, Set<Dge>> getTissueSpecificDgeMap() {
        return tissueSpecificDgeMap;
    }

    public Map<String, List<Das>> getTissue2asMap() {
        return tissue2asMap;
    }

    public Set<String> getAllGeneSymbols() {
        return allGeneSymbols;
    }
}
