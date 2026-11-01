package hu.thesis.cg.builder.osa;

import hu.thesis.cg.core.io.Csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Az OSA kimenetének olvasása.
 */
final class OsaMethodCsvReader {

    /** Az OSA metódus szintű CSV-jének névvége. */
    static final String METHOD_CSV_SUFFIX = "-Method.csv";

    /** Állapot nélküli olvasó. */
    OsaMethodCsvReader() {
    }

    /**
     * A legfrissebb megkeresése a könyvtárfában.
     */
    static Optional<Path> findMethodCsv(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return Optional.empty();
        }
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(p -> p.getFileName().toString().endsWith(METHOD_CSV_SUFFIX))
                    .max(Comparator.comparingLong(OsaMethodCsvReader::lastModified));
        }
    }

    /**
     * Utolsó módosítás ideje, hiba esetén 0 (így az olvashatatlan fájl sosem lesz a „legfrissebb”).
     */
    private static long lastModified(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * A fájl beolvasása.
     */
    List<OsaMethod> read(Path csv) throws IOException {
        List<OsaMethod> out = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String header = r.readLine();
            if (header == null) {
                return out;
            }
            List<String> cols = Csv.parse(stripBom(header));
            int longName = indexOf(cols, "LongName", csv);
            int path = cols.indexOf("Path");
            int line = cols.indexOf("Line");

            String row;
            while ((row = r.readLine()) != null) {
                if (row.isBlank()) {
                    continue;
                }
                List<String> f = Csv.parse(row);
                out.add(new OsaMethod(f.get(longName),
                        path >= 0 && path < f.size() && !f.get(path).isBlank() ? f.get(path) : null,
                        line >= 0 && line < f.size() ? parseIntOr(f.get(line), -1) : -1));
            }
        }
        return out;
    }

    /**
     * Kötelező oszlop indexe.
     */
    private static int indexOf(List<String> cols, String name, Path csv) throws IOException {
        int i = cols.indexOf(name);
        if (i < 0) {
            throw new IOException(csv + ": nincs '" + name + "' oszlop. Fejléc: " + cols);
        }
        return i;
    }

    /**
     * A UTF-8 bájtsorrendjel levágása
     */
    private static String stripBom(String s) {
        return s.startsWith("﻿") ? s.substring(1) : s;
    }

    /**
     * Egész szám értelmezése hibatűréssel.
     */
    private static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
