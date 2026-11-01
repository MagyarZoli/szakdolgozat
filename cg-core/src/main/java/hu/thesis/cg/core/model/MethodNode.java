package hu.thesis.cg.core.model;

import hu.thesis.cg.core.signature.MethodSignature;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A hívási gráf egy csúcsa, azaz egy metódus.
 */
public record MethodNode(String id, MethodSignature signature, String sourceFile, int line, Set<MethodFlag> flags) {

    /**
     * Kompakt konstruktor: ellenőrzi a kötelező mezőket, és a jelzőket saját {@link EnumSet}-be másolja.
     */
    public MethodNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(signature, "signature");
        flags = flags.isEmpty() ? EnumSet.noneOf(MethodFlag.class) : EnumSet.copyOf(flags);
    }

    /**
     * Rendelkezik-e a csúcs a megadott jelzővel.
     */
    public boolean has(MethodFlag flag) {
        return flags.contains(flag);
    }


    /**
     * Az elemzett alkalmazás része-e a metódus.
     */
    public boolean isApplication() {
        return !flags.contains(MethodFlag.LIBRARY);
    }
}


