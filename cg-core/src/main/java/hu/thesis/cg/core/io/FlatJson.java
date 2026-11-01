package hu.thesis.cg.core.io;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Egymásba nem ágyazott JSON-objektum írása és olvasása.
 */
public final class FlatJson {

    /**
     * Objektum kiírása, kulcsonként külön sorba, kétszóközös behúzással.
     */
    public static String write(Map<String, Object> values) {
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            sb.append("  ").append(quote(e.getKey())).append(": ");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append(quote(v.toString()));
            }
            sb.append(++i < values.size() ? ",\n" : "\n");
        }
        return sb.append("}\n").toString();
    }

    /**
     * Lapos objektum beolvasása.
     */
    public static Map<String, String> read(String json) {
        Map<String, String> out = new LinkedHashMap<>();
        int i = json.indexOf('{') + 1;
        while (true) {
            i = skipWs(json, i);
            if (i >= json.length() || json.charAt(i) == '}') {
                return out;
            }
            if (json.charAt(i) == ',') {
                i++;
                continue;
            }
            StringBuilder key = new StringBuilder();
            i = readString(json, i, key);
            i = skipWs(json, i);
            if (json.charAt(i) != ':') {
                throw new IllegalArgumentException("Hiányzó ':' a JSON-ban, pozíció: " + i);
            }
            i = skipWs(json, i + 1);
            if (json.charAt(i) == '"') {
                StringBuilder val = new StringBuilder();
                i = readString(json, i, val);
                out.put(key.toString(), val.toString());
            } else {
                int end = i;
                while (end < json.length() && ",}\n\r ".indexOf(json.charAt(end)) < 0) {
                    end++;
                }
                String raw = json.substring(i, end);
                out.put(key.toString(), "null".equals(raw) ? null : raw);
                i = end;
            }
        }
    }

    /**
     * Szóközök átugrása.
     */
    private static int skipWs(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Idézőjeles JSON-szöveg beolvasása a feloldójelek (\n, \", Unicode-escape stb.)
     * kezelésével.
     */
    private static int readString(String s, int i, StringBuilder out) {
        if (s.charAt(i) != '"') {
            throw new IllegalArgumentException("Szöveget vártam, pozíció: " + i);
        }
        i++;
        while (s.charAt(i) != '"') {
            char c = s.charAt(i);
            if (c == '\\') {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n' -> out.append('\n');
                    case 't' -> out.append('\t');
                    case 'r' -> out.append('\r');
                    case 'u' -> {
                        out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> out.append(n);
                }
            } else {
                out.append(c);
            }
            i++;
        }
        return i + 1;
    }

    /**
     * Szöveg JSON-szövegliterállá alakítása.
     */
    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
