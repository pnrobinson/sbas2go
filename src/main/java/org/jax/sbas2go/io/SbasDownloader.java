package org.jax.sbas2go.io;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * Command to download the {@code hp.obo} and {@code phenotype.hpoa} files that
 * we will need to run the LIRICAL approach.
 * @author Peter N Robinson
 */
public class SbasDownloader {
    //private static final Logger logger = LoggerFactory.getLogger(PrositometryDownloader.class);
    /** Directory to which we will download the files. */
    private final String downloadDirectory;
    /** If true, download new version whether or not the file is already present. */
    private final boolean overwrite;

    private final static String HGNC_File = "non_alt_loci_set.txt";

    private final static String HGNC_URL ="ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc/tsv/non_alt_loci_set.txt";




    private final static String GO_OBO = "go.obo";
    private final static String GO_OBO_URL = "http://purl.obolibrary.org/obo/go.obo";
    private final static String GO_ANNOT = "goa_human.gaf";
    private final static String GO_ANNOT_GZ = "goa_human.gaf.gz";
    private final static String GO_ANNOT_URL = "http://geneontology.org/gene-associations/goa_human.gaf.gz";



    public SbasDownloader(String path){
        this(path,false);
    }

    public SbasDownloader(String path, boolean overwrite){
        this.downloadDirectory=path;
        this.overwrite=overwrite;
    }

    /**
     * Download the files unless they are already present.
     */
    public void download() {
        downloadFileIfNeeded(HGNC_File,HGNC_URL);
        downloadGzipFileIfNeeded(GO_ANNOT,GO_ANNOT_GZ, GO_ANNOT_URL);
        downloadFileIfNeeded(GO_OBO,GO_OBO_URL);

    }


    private void downloadGzipFileIfNeeded(String filename, String gzFilename, String webAddress) {
        File file = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        File gzfile = new File(String.format("%s%s%s",downloadDirectory,File.separator,gzFilename));
        if (! ( file.exists() || gzfile.exists()) && ! overwrite ) {
            downloadFileIfNeeded(gzFilename, webAddress);
        }
        if (! file.exists()) {
            Path source = Paths.get(gzfile.getAbsolutePath());
            Path target = Paths.get(file.getAbsolutePath());
            try (GZIPInputStream gis = new GZIPInputStream(
                    new FileInputStream(source.toFile()));
                 FileOutputStream fos = new FileOutputStream(target.toFile())) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new SbasRuntimeException("Could not un-gzip GAF file: " + e.getMessage());
            }
        }
    }




    private void downloadFileIfNeeded(String filename, String webAddress) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            System.out.printf("Cowardly refusing to download %s since we found it at %s.\n",
                    filename,
                    f.getAbsolutePath());
            return;
        }
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(webAddress);
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (MalformedURLException e) {
            System.err.printf("Malformed URL for %s [%s]: %s",filename, webAddress,e.getMessage());
        } catch (FileDownloadException e) {
            System.err.printf("Error downloading %s from %s: %s\"" ,filename, webAddress,e.getMessage());
        }
        System.out.println("[INFO] Downloaded " + filename);
    }

}
