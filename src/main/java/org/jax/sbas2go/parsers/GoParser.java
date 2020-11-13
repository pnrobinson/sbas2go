package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GoParser {

    private final Ontology ontology;

    private final List<GoGaf21Annotation> goAnnotations;

    private final AssociationContainer associationContainer;

    /**
     *
     * @param dir directory with location of the go.obo and go_human.gaf files
     */
    public GoParser(String dir) {
        File directory = new File(dir);
        if (! directory.exists() && directory.isDirectory()) {
            throw new SbasRuntimeException("Invalid GO data directory");
        }
        Path oboPath = Paths.get(directory.getAbsolutePath(), "go.obo");
        File oboFile = oboPath.toFile();
        if (! oboFile.exists()) {
            throw new SbasRuntimeException("Could not find go.obo");
        }
        this.ontology = OntologyLoader.loadOntology(oboFile);
        System.out.printf("[INFO] Parsed go.obo and extracted %d GO terms.\n", this.ontology.countNonObsoleteTerms());
        Path gafPath = Paths.get(directory.getAbsolutePath(), "goa_human.gaf");
        File gafFile = gafPath.toFile();
        if (! gafFile.exists()) {
            throw new SbasRuntimeException("Could not find goa_human.gaf");
        }
        this.goAnnotations = GoGeneAnnotationParser.loadAnnotations(gafFile);
        System.out.printf("[INFO] Parsed goa_human.gaf and extracted %d annotations.\n", this.goAnnotations.size());
        associationContainer = AssociationContainer.loadGoGafAssociationContainer(gafFile);
    }

    public Ontology getOntology() {
        return ontology;
    }

    public List<GoGaf21Annotation> getGoAnnotations() {
        return goAnnotations;
    }

    public AssociationContainer getAssociationContainer() {
        return associationContainer;
    }
}
