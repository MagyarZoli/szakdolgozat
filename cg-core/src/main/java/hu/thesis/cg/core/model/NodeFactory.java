package hu.thesis.cg.core.model;

import hu.thesis.cg.core.signature.MethodSignature;
import hu.thesis.cg.core.signature.SignatureStyle;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Csúcsok egységes létrehozása (gyár minta).
 */
public final class NodeFactory {
    private final SignatureStyle style;

    /**
     * Gyár létrehozása a megadott azonosítóstílussal.
     */
    public NodeFactory(SignatureStyle style) {
        this.style = Objects.requireNonNull(style, "style");
    }

    /**
     * Az azonosítók stílusa.
     */
    public SignatureStyle style() {
        return style;
    }

    /**
     * Új csúcs létrehozása.
     */
    public MethodNode create(MethodSignature signature, String sourceFile, int line, Set<MethodFlag> extraFlags) {
        Set<MethodFlag> flags = EnumSet.noneOf(MethodFlag.class);
        flags.addAll(MethodFlag.fromNames(signature));
        flags.addAll(extraFlags);
        return new MethodNode(style.format(signature), signature, sourceFile, line, flags);
    }
}
