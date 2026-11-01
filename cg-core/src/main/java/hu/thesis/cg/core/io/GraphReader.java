package hu.thesis.cg.core.io;

import hu.thesis.cg.core.model.CallGraph;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Korábban kiírt gráf visszaolvasása, például a későbbi graflet-számlálás
 * számára (így a drága gráfépítést nem kell megismételni).
 */
public interface GraphReader {

    /**
     * A gráf beolvasása.
     */
    CallGraph read(Path graphDir) throws IOException;
}
