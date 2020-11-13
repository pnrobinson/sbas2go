package org.jax.sbas2go.analysis;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.PopulationSet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;

public class GeneSetExtractor {

    private final Map<String, String> ensembl2symbolMap;

    private final Map<String, TermId> symbol2termIdMap;

    private final Map<String, TermId> ensembl2termId;

    private final AssociationContainer associationContainer;

    private final Ontology ontology;

    public GeneSetExtractor(Map<String, String> ensembl2symbol,
                            Map<String, TermId> ensembl2termId,Map<String,
                            TermId> symbol2termId,
                            AssociationContainer associationContainer,
                            Ontology ontology) {
        this.ensembl2symbolMap = ensembl2symbol;
        this.ensembl2termId = ensembl2termId;
        this.symbol2termIdMap = symbol2termId;
        this.associationContainer = associationContainer;
        this.ontology = ontology;
    }

    /**
     * Get all genes used for either DGE or DAS analysis.
     * @param dgePopulation List of ensembl ids for DGEs
     * @param asGeneSymbols List of symbols for DASs
     * @return
     */
    public PopulationSet getPopulationSet(Set<String> dgePopulation, Set<String> asGeneSymbols) {
        Set<TermId> tidSet = new HashSet<>();
        for (var ens : dgePopulation) {
            Optional<TermId> t = Optional.ofNullable(ensembl2termId.get(ens));
            if (t.isPresent()) {
                tidSet.add(t.get());
            } else {
                System.err.println("Could not find ENS-" + ens);
            }
        }
        for (var symbol : asGeneSymbols) {
            Optional<TermId> t = Optional.ofNullable(symbol2termIdMap.get(symbol));
            if (t.isPresent()) {
                tidSet.add(t.get());
            } else {
                System.err.println("Could not find symbol-" + symbol);
            }
        }
        Map<TermId, DirectAndIndirectTermAnnotations> populationAssociations = associationContainer.getAssociationMap(tidSet, ontology);
        return new PopulationSet(tidSet, populationAssociations);
    }


}
