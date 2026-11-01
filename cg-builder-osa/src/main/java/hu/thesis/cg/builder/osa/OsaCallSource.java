package hu.thesis.cg.builder.osa;

import hu.thesis.cg.core.api.ToolSettings;
import hu.thesis.cg.core.io.Csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Az OSA hívási éleinek forrása (cserélhető stratégia).
 */
interface OsaCallSource {

    /**
     * Egy hívás az OSA LongName alakú azonosítóival, még a gráfcsúcsokhoz rendelés előtt.
     */
    record RawCall(String callerLongName, String calleeLongName, int line) {
    }

    /**
     * Az élek beolvasása.
     */
    List<RawCall> read(Path osaResultsDir) throws IOException;

    /**
     * Olvasásra szánt leírás a naplóhoz és a hibaüzenetekhez.
     */
    String describe();

    /**
     * A beállításoknak megfelelő élforrás.
     */
    static OsaCallSource from(ToolSettings settings) {
        String kind = settings.get("osa.calls.source").orElse("none");
        return switch (kind) {
            case "csv" -> new CsvCallSource(settings.get("osa.calls.file").orElse("*-Calls.csv"));
            case "none" -> new NoCallSource();
            default -> throw new IllegalArgumentException("Ismeretlen osa.calls.source: " + kind + " (csv | none)");
        };
    }

    /**
     * Nincs élforrás beállítva: a gráf csak csúcsokat tartalmaz.
     */
    final class NoCallSource implements OsaCallSource {

        /** Állapot nélküli. */
        NoCallSource() {
        }

        @Override
        public List<RawCall> read(Path osaResultsDir) {
            return List.of();
        }

        @Override
        public String describe() {
            return "nincs élforrás beállítva (osa.calls.source=none), a gráf csak csúcsokat tartalmaz";
        }
    }

    /**
     * Élek egy CSV-fájlból.
     */
    final class CsvCallSource implements OsaCallSource {

        /** A fájlnév glob-mintája, például
         * *-Calls.csv.
         */
        private final String glob;

        /**
         * Élforrás egy glob-mintával megadott fájlhoz.
         */
        CsvCallSource(String glob) {
            this.glob = glob;
        }

        @Override
        public List<RawCall> read(Path osaResultsDir) throws IOException {
            var matcher = osaResultsDir.getFileSystem().getPathMatcher("glob:" + glob);
            Path file;
            try (var s = Files.walk(osaResultsDir)) {
                file = s.filter(p -> matcher.matches(p.getFileName())).findFirst()
                        .orElseThrow(() -> new IOException("Nincs '" + glob + "' fájl itt: " + osaResultsDir));
            }
            List<RawCall> calls = new ArrayList<>();
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<String> cols = Csv.parse(r.readLine());
                int caller = cols.indexOf("caller");
                int callee = cols.indexOf("callee");
                int line = cols.indexOf("line");
                if (caller < 0 || callee < 0) {
                    throw new IOException(file + ": 'caller' és 'callee' oszlop kell. Fejléc: " + cols);
                }
                String row;
                while ((row = r.readLine()) != null) {
                    if (!row.isBlank()) {
                        List<String> f = Csv.parse(row);
                        int ln = line >= 0 && line < f.size() && !f.get(line).isBlank()
                                ? Integer.parseInt(f.get(line).trim()) : -1;
                        calls.add(new RawCall(f.get(caller), f.get(callee), ln));
                    }
                }
            }
            return calls;
        }

        @Override
        public String describe() {
            return "CSV élforrás (" + glob + ")";
        }
    }
}
