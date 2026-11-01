package hu.thesis.cg.core.model;

import java.util.Objects;

/**
 * Irányított hívási él a hívótól a hívottig.
 */
public record CallEdge(String sourceId, String targetId, CallKind kind, int callSiteLine) {

    public CallEdge {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(kind, "kind");
    }

    /**
     * Hurokél-e, azaz közvetlen rekurzió (a metódus önmagát hívja).
     */
    public boolean isSelfLoop() {
        return sourceId.equals(targetId);
    }
}
