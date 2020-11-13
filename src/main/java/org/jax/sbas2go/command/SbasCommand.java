package org.jax.sbas2go.command;

import org.jax.sbas2go.analysis.Das;
import org.jax.sbas2go.analysis.Dge;
import org.jax.sbas2go.analysis.GeneSetExtractor;
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

    @Override
    public Integer call() throws Exception {
        GoParser goParser = new GoParser("data");
        AssociationContainer associationContainer = goParser.getAssociationContainer();
        Ontology ontology = goParser.getOntology();
        List<GoGaf21Annotation> annots =  goParser.getGoAnnotations();
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
                annots,
                ontology);
        PopulationSet populationSet = geneSetExtractor.getPopulationSet(dgePopulation, asGeneSymbols);
        System.out.printf("[INFO] Got populaton set with %d  annotated genes.\n", populationSet.getAnnotatedItemCount());
        // do DGE analysis
        for (var entry : dgeMap.entrySet()) {
            String tissue = entry.getKey();
            Set<Dge> dgeSet = entry.getValue();
            StudySet study = geneSetExtractor.getStudySet(dgeSet, tissue);
            try {
                doDgeAnalysis(tissue, study, ontology, associationContainer, populationSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //associationContainer.

        return 0;
    }


    private void doDgeAnalysis(String tissue,
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
        System.out.println(String.format("[INFO] Study set: %d genes. Population set: %d genes",
                studysize, popsize));
        for (GoTerm2PValAndCounts item : pvals) {
            double pval = item.getRawPValue();
            double pval_adj = item.getAdjustedPValue();
            TermId tid = item.getItem();
            Term term = ontology.getTermMap().get(tid);
            if (term == null) {
                System.err.println("[ERROR] Could not retrieve term for " + tid.getValue());
                continue;
            }
            String label = term.getName();
            if (pval_adj > ALPHA) {
                continue;
            }
            n_sig++;
            double studypercentage = 100.0 * (double) item.getAnnotatedStudyGenes() / studysize;
            double poppercentage = 100.0 * (double) item.getAnnotatedPopulationGenes() / popsize;
            System.out.println(String.format("%s [%s]: %.2e (adjusted %.2e). Study: n=%d (%.1f%%); population: N=%d (%.1f%%)",
                    label, tid.getValue(), pval, pval_adj, item.getAnnotatedStudyGenes(), studypercentage,
                    item.getAnnotatedPopulationGenes(), poppercentage));
        }
        System.out.println(String.format("%d of %d terms were significant at alpha %.7f", n_sig, pvals.size(), ALPHA));
    }






}
