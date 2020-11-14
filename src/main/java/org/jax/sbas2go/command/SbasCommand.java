package org.jax.sbas2go.command;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.analysis.GeneSetExtractor;
import org.jax.sbas2go.go.GoResultPrinter;
import org.jax.sbas2go.go.GoResultSet;
import org.jax.sbas2go.parsers.GoParser;
import org.jax.sbas2go.parsers.HgncParser;
import org.jax.sbas2go.parsers.SbasParser;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.PopulationSet;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;
import picocli.CommandLine;

import java.util.ArrayList;
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


    private final double ALPHA = 0.05;

    private List<GoResultSet> dgeResults;

    private List<GoResultSet> dasResults;

    @Override
    public Integer call() throws Exception {
        SbasParser sbasParser = new SbasParser(sbasdir);
        GoParser goParser = new GoParser("data");
        AssociationContainer associationContainer = goParser.getAssociationContainer();
        Ontology ontology = goParser.getOntology();
        List<GoGaf21Annotation> annots =  goParser.getGoAnnotations();
       // SbasParser sbasParser = new SbasParser(sbasdir);
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
                annots,
                ontology);
        PopulationSet populationSet = geneSetExtractor.getPopulationSet(dgePopulation, asGeneSymbols);
        System.out.printf("[INFO] Got populaton set with %d  annotated genes.\n", populationSet.getAnnotatedItemCount());
        this.dgeResults = new ArrayList<>();
        this.dasResults = new ArrayList<>();
        // do DGE analysis
        for (var entry : dgeMap.entrySet()) {
            String tissue = entry.getKey();
            Set<Dge> dgeSet = entry.getValue();
            StudySet study = geneSetExtractor.getStudySet(dgeSet, tissue);
            if (study.getAnnotatedItemCount() < 2) {
                System.out.printf("[INFO] Skipping analysis for %s (DGE) because it contains only %d gene(s)",
                        tissue, study.getAnnotatedItemCount());
                continue;
            }
            try {
                GoResultSet result = doDgeAnalysis(tissue, study, ontology, associationContainer, populationSet);
                dgeResults.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // do DAS analysis
        for (var entry : asMap.entrySet()) {
            String tissue = entry.getKey();
            List<Das> dasSet = entry.getValue();
            StudySet study = geneSetExtractor.getStudySet(dasSet, tissue);
            if (study.getAnnotatedItemCount() < 2) {
                System.out.printf("[INFO] Skipping analysis for %s (DAS) because it contains only %d gene(s)",
                        tissue, study.getAnnotatedItemCount());
                continue;
            }
            try {
                GoResultSet result = doDgeAnalysis(tissue, study, ontology, associationContainer, populationSet);
                dasResults.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GoResultPrinter printer = new GoResultPrinter(dgeResults, ontology);
        printer.output("dge2go.tsv");
        printer = new GoResultPrinter(dasResults, ontology);
        printer.output("das2go.tsv");

        return 0;
    }


    private GoResultSet doDgeAnalysis(String tissue,
                               StudySet study,
                               Ontology ontology,
                               AssociationContainer associationContainer,
                               PopulationSet populationSet) {
        System.out.println();
        System.out.printf("[INFO] DGE: Term-for-term analysis for %s\n", tissue);
        System.out.println();
        System.out.printf("[INFO] study set: %d genes; population set: %d genes\n", study.getAnnotatedItemCount(), populationSet.getAnnotatedItemCount());

        MultipleTestingCorrection bonf = new Bonferroni();
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(ontology,
                associationContainer,
                populationSet,
                study,
                bonf);
        List<GoTerm2PValAndCounts> pvals = tftpvalcal.calculatePVals();
        System.out.println("[INFO] Total number of retrieved p values: " + pvals.size());
        int n_sig = 0;
        int studysize = study.getAnnotatedItemCount();
        int popsize = populationSet.getAnnotatedItemCount();
        GoResultSet resultSet = new GoResultSet(tissue, studysize, popsize, pvals, ALPHA);
        return resultSet;
    }






}
