package hu.thesis.cg.core.model;

import hu.thesis.cg.core.api.InputKind;
import hu.thesis.cg.core.api.Scope;
import hu.thesis.cg.core.api.Strategy;
import hu.thesis.cg.core.signature.SignatureStyle;

import java.time.Instant;

/**
 * A gráf előállításának körülményei.
 */
public record GraphMetadata(
        String tool,
        String toolVersion,
        Strategy strategy,
        InputKind inputKind,
        String input,
        Scope scope,
        SignatureStyle signatureStyle,
        String project,
        String projectVersion,
        Instant createdAt,
        long buildMillis) {

    /**
     * Másolat a megadott futásidővel; minden más mező változatlan.
     * A builder alaposztálya hívja a mérés végén.
     */
    public GraphMetadata withBuildMillis(long millis) {
        return new GraphMetadata(tool, toolVersion, strategy, inputKind, input, scope, signatureStyle,
                project, projectVersion, createdAt, millis);
    }
}



