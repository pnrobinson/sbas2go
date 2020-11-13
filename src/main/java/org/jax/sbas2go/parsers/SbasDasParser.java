package org.jax.sbas2go.parsers;

import org.jax.sbas2go.except.SbasRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SbasDasParser {

    public SbasDasParser(File dgeDir) {
        String []files = dgeDir.list();
        List<String> originalFiles = Arrays.stream(files)
                .filter(not(value -> value.contains("refined")))
                .collect(Collectors.toList());
        List<String> a3ss = originalFiles.stream().filter(value -> value.startsWith("a3ss")).collect(Collectors.toList());
        List<String> a5ss =originalFiles.stream().filter(value -> value.startsWith("a5ss")).collect(Collectors.toList());
        List<String> ri = originalFiles.stream().filter(value -> value.startsWith("ri")).collect(Collectors.toList());
        List<String> se = originalFiles.stream().filter(value -> value.startsWith("se")).collect(Collectors.toList());
        List<String> mxe = originalFiles.stream().filter(value -> value.startsWith("mxe")).collect(Collectors.toList());

    }


    private void parseAs(File dgeDir, String name) {
        Path p = Paths.get(dgeDir.getAbsolutePath(), name);
        File f = p.toFile();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new SbasRuntimeException(e.getLocalizedMessage());
        }
    }

    private static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }
}
