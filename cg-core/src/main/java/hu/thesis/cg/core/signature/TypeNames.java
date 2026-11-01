package hu.thesis.cg.core.signature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Típusnevek átalakítása a Java-forrás alak és a JVM-descriptor alak között.
 */
public final class TypeNames {

    /** Primitív típusnév → egybetűs descriptor kód. */
    private static final Map<String, Character> PRIMITIVE_TO_DESC = Map.of(
            "void", 'V',
            "boolean", 'Z',
            "byte", 'B',
            "char", 'C',
            "short", 'S',
            "int", 'I',
            "long", 'J',
            "float", 'F',
            "double", 'D'
    );

    /** Egybetűs descriptor kód → primitív típusnév (az előző fordítottja). */
    private static final Map<Character, String> DESC_TO_PRIMITIVE = Map.of(
            'V', "void",
            'Z', "boolean",
            'B', "byte",
            'C', "char",
            'S', "short",
            'I', "int",
            'J', "long",
            'F', "float",
            'D', "double"
    );

    /**
     * Egy Java-forrás alakú típusnév átalakítása descriptorrá.
     */
    public static String toDescriptor(String javaType) {
        String t = javaType.trim();
        StringBuilder sb = new StringBuilder();
        while (t.endsWith("[]")) {
            sb.append('[');
            t = t.substring(0, t.length() - 2);
        }
        if (t.endsWith("...")) {
            sb.append('[');
            t = t.substring(0, t.length() - 3);
        }
        Character prim = PRIMITIVE_TO_DESC.get(t);
        if (prim != null) {
            sb.append(prim);
        } else {
            sb.append('L').append(t.replace('.', '/')).append(';');
        }
        return sb.toString();
    }

    /**
     * Teljes metódusdescriptor összeállítása.
     */
    public static String toMethodDescriptor(List<String> params, String returnType) {
        StringBuilder sb = new StringBuilder("(");
        for (String p : params) {
            sb.append(toDescriptor(p));
        }
        return sb.append(')').append(toDescriptor(returnType)).toString();
    }

    /**
     * Egyetlen típusdescriptor visszaalakítása Java-forrás alakra.
     */
    public static String fromDescriptor(String descriptor) {
        List<String> out = new ArrayList<>();
        int end = parseOne(descriptor, 0, out);
        if (end != descriptor.length()) {
            throw new IllegalArgumentException("Nem egyetlen típus: " + descriptor);
        }
        return out.get(0);
    }

    /**
     * Metódusdescriptor szétbontása visszatérési típusra és paraméterekre.
     */
    public static List<String> parseMethodDescriptor(String descriptor) {
        if (!descriptor.startsWith("(")) {
            throw new IllegalArgumentException("Hibás metódusdescriptor: " + descriptor);
        }
        List<String> params = new ArrayList<>();
        int i = 1;
        while (i < descriptor.length() && descriptor.charAt(i) != ')') {
            i = parseOne(descriptor, i, params);
        }
        if (i >= descriptor.length()) {
            throw new IllegalArgumentException("Hiányzó ')' a descriptorban: " + descriptor);
        }
        List<String> ret = new ArrayList<>();
        int end = parseOne(descriptor, i + 1, ret);
        if (end != descriptor.length()) {
            throw new IllegalArgumentException("Hibás visszatérési típus: " + descriptor);
        }
        List<String> result = new ArrayList<>();
        result.add(ret.get(0));
        result.addAll(params);
        return result;
    }

    /**
     * Egy típus beolvasása a descriptor adott pozíciójától.
     */
    private static int parseOne(String d, int pos, List<String> out) {
        int dims = 0;
        while (pos < d.length() && d.charAt(pos) == '[') {
            dims++;
            pos++;
        }
        if (pos >= d.length()) {
            throw new IllegalArgumentException("Váratlan vége a descriptornak: " + d);
        }
        char c = d.charAt(pos);
        String base;
        if (c == 'L') {
            int semi = d.indexOf(';', pos);
            if (semi < 0) {
                throw new IllegalArgumentException("Hiányzó ';' a descriptorban: " + d);
            }
            base = d.substring(pos + 1, semi).replace('/', '.');
            pos = semi + 1;
        } else {
            base = DESC_TO_PRIMITIVE.get(c);
            if (base == null) {
                throw new IllegalArgumentException("Ismeretlen típuskód '" + c + "' itt: " + d);
            }
            pos++;
        }
        out.add(base + "[]".repeat(dims));
        return pos;
    }
}
