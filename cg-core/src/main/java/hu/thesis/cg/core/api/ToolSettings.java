package hu.thesis.cg.core.api;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Gépenként eltérő eszközbeállítások (például az OSA telepítési útvonala), kulcs–érték párokként.
 */
public final class ToolSettings {
    private final Map<String, String> values;

    /**
     * Beállítások közvetlenül egy táblából.
     */
    public ToolSettings(Map<String, String> values) {
        this.values = new TreeMap<>(values);
    }

    /**
     * Üres beállításkészlet
     * (tesztekhez, illetve ha nincs konfigurációs könyvtár).
     */
    public static ToolSettings empty() {
        return new ToolSettings(Map.of());
    }

    /**
     * Beállítások betöltése a megadott konfigurációs könyvtárból, a fenti sorrendben.
     */
    public static ToolSettings load(Path configDir) throws IOException {
        Map<String, String> map = new TreeMap<>();
        for (String name : new String[]{"tools.properties", "tools.local.properties"}) {
            Path file = configDir.resolve(name);
            if (Files.isRegularFile(file)) {
                Properties p = new Properties();
                try (Reader r = Files.newBufferedReader(file)) {
                    p.load(r);
                }
                p.stringPropertyNames().forEach(k -> map.put(k, p.getProperty(k).trim()));
            }
        }
        // Környezeti változók felülírják a fájlt
        for (String key : Map.copyOf(map).keySet()) {
            String env = System.getenv("CG_" + key.toUpperCase().replace('.', '_'));
            if (env != null && !env.isBlank()) {
                map.put(key, env.trim());
            }
        }
        return new ToolSettings(map);
    }

    /**
     * Egy beállítás értéke.
     */
    public Optional<String> get(String key) {
        String v = values.get(key);
        return v == null || v.isBlank() ? Optional.empty() : Optional.of(v);
    }

    /**
     * Kötelező beállítás értéke.
     */
    public String require(String key) throws BuildException {
        return get(key).orElseThrow(() -> new BuildException(
                "Hiányzó beállítás: '" + key + "' (config/tools.local.properties vagy CG_"
                        + key.toUpperCase().replace('.', '_') + " környezeti változó)"));
    }

    /**
     * Az összes beállítás.
     */
    public Map<String, String> asMap() {
        return Map.copyOf(values);
    }
}
