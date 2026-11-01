package hu.thesis.cg.core.io;

import hu.thesis.cg.core.model.CallGraph;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Gráf kiírása egy kimeneti könyvtárba.
 */
public interface GraphWriter {

    /**
     * A gráf kiírása.
     */
    void write(CallGraph graph, Path outputDir) throws IOException;
}
