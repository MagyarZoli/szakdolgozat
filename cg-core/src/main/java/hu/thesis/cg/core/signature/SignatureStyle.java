package hu.thesis.cg.core.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A metódusazonosító szöveges stílusa: hogyan lesz egy MethodSignature-ből azonosító, és vissza.
 */
public enum SignatureStyle {

    /**
     * Az OSA LongName stílusa: osztály.név + JVM-descriptor.
     */
    JVM {
        @Override
        public String format(MethodSignature s) {
            return s.className() + "." + s.methodName()
                    + TypeNames.toMethodDescriptor(s.parameterTypes(), s.returnType());
        }

        @Override
        public MethodSignature parse(String text) {
            int paren = text.indexOf('(');
            if (paren < 0) {
                throw new IllegalArgumentException("Hiányzó '(' az azonosítóban: " + text);
            }
            String owner = text.substring(0, paren);
            int dot = lastMemberDot(owner);
            List<String> types = TypeNames.parseMethodDescriptor(text.substring(paren));
            return new MethodSignature(owner.substring(0, dot), owner.substring(dot + 1),
                    types.subList(1, types.size()), types.get(0));
        }
    },

    /**
     * Ember számára olvasható Java-forrás stílus, a visszatérési típus kettőspont után.
     */
    JAVA {
        @Override
        public String format(MethodSignature s) {
            return s.className() + "." + s.methodName()
                    + "(" + String.join(",", s.parameterTypes()) + "):" + s.returnType();
        }

        @Override
        public MethodSignature parse(String text) {
            int open = text.indexOf('(');
            int close = text.lastIndexOf(')');
            if (open < 0 || close < open) {
                throw new IllegalArgumentException("Hibás azonosító: " + text);
            }
            String owner = text.substring(0, open);
            int dot = lastMemberDot(owner);
            String inside = text.substring(open + 1, close).trim();
            List<String> params = inside.isEmpty()
                    ? List.of()
                    : new ArrayList<>(Arrays.stream(inside.split(",")).map(String::trim).toList());
            String ret = close + 1 < text.length() && text.charAt(close + 1) == ':'
                    ? text.substring(close + 2) : "void";
            return new MethodSignature(owner.substring(0, dot), owner.substring(dot + 1), params, ret);
        }
    };

    public static final SignatureStyle DEFAULT = JVM;

    /**
     * Azonosító készítése a metódusleírásból.
     */
    public abstract String format(MethodSignature signature);

    /**
     * Azonosító visszaalakítása metódusleírássá (a fordítottja).
     */
    public abstract MethodSignature parse(String text);

    /**
     * Az osztálynév és a metódusnév közötti pont megkeresése.
     */
    private static int lastMemberDot(String owner) {
        int dot = owner.lastIndexOf('.');
        if (dot <= 0 || dot == owner.length() - 1) {
            throw new IllegalArgumentException("Nem található osztály.metódus alak: " + owner);
        }
        return dot;
    }
}
