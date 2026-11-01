package hu.thesis.cg.core.api;

import hu.thesis.cg.core.model.CallGraph;

import java.util.Set;

/**
 * Egy hívásigráf-előállító eszköz egységes interfésze.
 */
public interface CallGraphBuilder {

    /**
     * Rövid, egyedi azonosító; kapcsolója ezzel választ.
     */
    String id();

    /**
     * Egysoros leírás a listához.
     */
    String description();

    /**
     * A használt eszköz verziója; a metaadatokba kerül.
     */
    String toolVersion();

    /**
     * A támogatott gráfépítési stratégiák.
     */
    Set<Strategy> supportedStrategies();

    /**
     * A támogatott bemenetfajták.
     */
    Set<InputKind> supportedInputs();

    /**
     * A hívási gráf előállítása.
     */
    CallGraph build(BuildRequest request) throws BuildException;
}
