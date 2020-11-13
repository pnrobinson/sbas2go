package org.jax.sbas2go.command;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.analysis.GeneSetExtractor;
import org.jax.sbas2go.parsers.GoParser;
import org.jax.sbas2go.parsers.HgncParser;
import org.jax.sbas2go.parsers.SbasParser;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.PopulationSet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "go",
        mixinStandardHelpOptions = true,
        description = "GO")
public class SbasCommand implements Callable<Integer> {
    @CommandLine.Option(names={"-s","--sbas"}, description ="directory to sbas data" )
    private String sbasdir;

    @Override
    public Integer call() throws Exception {
        GoParser goParser = new GoParser("data");
        AssociationContainer associationContainer = goParser.getAssociationContainer();
        Ontology ontology = goParser.getOntology();
        SbasParser sbasParser = new SbasParser(sbasdir);
        // the following has ENSEMBL ids
        Set<String> dgePopulation = sbasParser.getEnsembleDgePopulation();
        Set<String> asGeneSymbols = sbasParser.getAllGeneSymbols();
        Map<String, List<Das>> asMap = sbasParser.getTissue2asMap();
        Map<String, Set<Dge>> dgeMap = sbasParser.getTissueSpecificDgeMap();
        HgncParser hgncParser = new HgncParser();
        Map<String, String> ensembl2symbolMap = hgncParser.getEnsembl2symbolMap();
        Map<String, TermId> ensembl2termIdMap = hgncParser.getEnsembl2termIdMap();
        Map<String, TermId> symbol2termIdMap = hgncParser.getSymbol2termIdMap();
        GeneSetExtractor geneSetExtractor = new GeneSetExtractor(ensembl2symbolMap,
                ensembl2termIdMap,
                symbol2termIdMap,
                associationContainer,
                ontology);
        PopulationSet populationSet = geneSetExtractor.getPopulationSet(dgePopulation, asGeneSymbols);
        System.out.printf("[INFO] Got populaton set with %d  annotated genes.\n", populationSet.getAnnotatedItemCount());


        return 0;
    }
}
