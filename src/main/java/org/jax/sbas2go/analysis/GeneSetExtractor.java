package org.jax.sbas2go.analysis;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.PopulationSet;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;

public class GeneSetExtractor {

    private final Map<String, String> ensembl2symbolMap;

    private final Map<String, TermId> symbol2termIdMap;

    private final Map<String, TermId> ensembl2termId;

    private final AssociationContainer associationContainer;

    private final Ontology ontology;



    private final Map<String, TermId> symbol2uniprotMap;

    public GeneSetExtractor(Map<String, String> ensembl2symbol,
                            Map<String, TermId> ensembl2termId,Map<String,
                            TermId> symbol2termId,
                            AssociationContainer associationContainer,
                            List<GoGaf21Annotation> annots,
                            Ontology ontology) {
        this.ensembl2symbolMap = ensembl2symbol;
        this.ensembl2termId = ensembl2termId;
        this.symbol2termIdMap = symbol2termId;
        this.associationContainer = associationContainer;
        this.ontology = ontology;
        symbol2uniprotMap = new HashMap<>();
        for (var termAnnot : annots) {
            TermId uniprotId = termAnnot.getDbObjectTermId();
            String symbol = termAnnot.getDbObjectSymbol();
            if (! uniprotId.getValue().startsWith("UniProtKB")) {
                throw new SbasRuntimeException("Bad Uniprot id in mapping: " + uniprotId.getValue());
            }
            if (symbol == null || symbol.length()<1) {
                throw new SbasRuntimeException("Bad symbol id \"" + symbol +"\"");
            }
           symbol2uniprotMap.putIfAbsent(symbol, uniprotId);
        }
        System.out.printf("Extracted %d symbol to Uniprot TermId mappings.\n", symbol2termId.size());
    }

    /**
     * Get all genes used for either DGE or DAS analysis.
     * We need to map to Uniprot IDs
     * @param dgePopulation List of ensembl ids for DGEs
     * @param asGeneSymbols List of symbols for DASs
     * @return Population set for GO analysis
     */
    public PopulationSet getPopulationSet(Set<String> dgePopulation, Set<String> asGeneSymbols) {
        Set<TermId> tidSet = new HashSet<>();
        Set<String> allSymbols = new HashSet<>(asGeneSymbols);
        for (var ens : dgePopulation) {
            Optional<String> symbolOpt = Optional.ofNullable(ensembl2symbolMap.get(ens));
            if (symbolOpt.isPresent()) {
                allSymbols.add(symbolOpt.get());
            } else {
                System.err.println("Could not find ENS-" + ens);
            }
        }
        for (var symbol : allSymbols) {
            Optional<TermId> t = Optional.ofNullable(symbol2uniprotMap.get(symbol));
            if (t.isPresent()) {
                tidSet.add(t.get());
            } else {
                System.err.println("Could not find symbol-" + symbol);
            }
        }
        Map<TermId, DirectAndIndirectTermAnnotations> populationAssociations = associationContainer.getAssociationMap(tidSet, ontology);
        return new PopulationSet(tidSet, populationAssociations);
    }

    public StudySet getStudySet(List<Das> dasSet, String name) {
        Set<TermId> tidSet = new HashSet<>();
        int notFound = 0;
        for (var das : dasSet) {
            String symbol = das.getSymbol();
            Optional<TermId> tidOpt = Optional.ofNullable(symbol2uniprotMap.get(symbol));
            if (tidOpt.isPresent()) {
                TermId uniprot = tidOpt.get();
                if (! uniprot.getValue().startsWith("UniProtKB")) {
                    throw new SbasRuntimeException("Got bad uniprot id for study set: \"" + uniprot.getValue() + "\"");
                }
                tidSet.add(tidOpt.get());
            } else {
                notFound++;
            }
        }
        System.out.printf("[INFO] Found %d TermIds, could not find %d\n", tidSet.size(), notFound);
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(tidSet, ontology);

        return new StudySet(tidSet, name, studyAssociations);
    }


    public StudySet getStudySet(Set<Dge> dgeSet, String name) {
        Set<TermId> tidSet = new HashSet<>();
        int notFound = 0;
        for (var dge : dgeSet) {
            String ens = dge.getEnsg();
            Optional<String> symbolOpt = Optional.ofNullable(ensembl2symbolMap.get(ens));
            if (symbolOpt.isPresent()) {
                Optional<TermId> tidOpt = Optional.ofNullable(symbol2uniprotMap.get(symbolOpt.get()));
                if (tidOpt.isPresent()) {
                    TermId uniprot = tidOpt.get();
                    if (! uniprot.getValue().startsWith("UniProtKB")) {
                        throw new SbasRuntimeException("Got bad uniprot id for study set: \"" + uniprot.getValue() + "\"");
                    }
                    tidSet.add(tidOpt.get());
                } else {
                    notFound++;
                }
            } else {
                notFound++;
            }
        }
        System.out.printf("[INFO] Found %d TermIds, could not find %d\n", tidSet.size(), notFound);
        if (tidSet.size() < 2) {
            throw new SbasRuntimeException("Study set too small size =" + tidSet.size());
        }
        Map<TermId, DirectAndIndirectTermAnnotations> studyAssociations = associationContainer.getAssociationMap(tidSet, ontology);

        return new StudySet(tidSet, name, studyAssociations);
    }


}
