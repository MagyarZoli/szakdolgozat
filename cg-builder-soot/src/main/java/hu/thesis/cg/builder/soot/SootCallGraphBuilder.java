package hu.thesis.cg.builder.soot;

import hu.thesis.cg.core.api.AbstractCallGraphBuilder;
import hu.thesis.cg.core.api.BuildRequest;
import hu.thesis.cg.core.api.InputKind;
import hu.thesis.cg.core.api.Strategy;
import hu.thesis.cg.core.model.CallGraph;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Soot alapú builder: bájtkódból (jar vagy class-könyvtár) épít CHA szerinti hívási gráfot.
 */
public final class SootCallGraphBuilder extends AbstractCallGraphBuilder {

    /** Paraméter nélküli konstruktor a {@link java.util.ServiceLoader} számára. */
    public SootCallGraphBuilder() {
    }

    @Override
    public String id() {
        return "soot";
    }

    @Override
    public String description() {
        return "Soot, bájtkód alapú elemzés (Class Hierarchy Analysis)";
    }

    @Override
    public String toolVersion() {
        try (InputStream in = G.class.getResourceAsStream("/META-INF/maven/org.soot-oss/soot/pom.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                return p.getProperty("version", "unknown");
            }
        } catch (IOException ignored) {
            // nem kritikus, csak a metaadatba kerül
        }
        return "unknown";
    }

    @Override
    public Set<Strategy> supportedStrategies() {
        return EnumSet.of(Strategy.CHA);
    }

    @Override
    public Set<InputKind> supportedInputs() {
        return EnumSet.of(InputKind.BYTECODE);
    }

    /**
     * A Soot beállítása, az osztályok betöltése, a hívási gráf felépítése és átalakítása.
     */
    @Override
    protected synchronized void populate(BuildRequest request, CallGraph graph) {
        configureSoot(request);
        Scene.v().loadNecessaryClasses();
        // A "cg" csomag a beállított algoritmussal (cg.cha) felépíti a Scene hívási gráfját
        PackManager.v().getPack("cg").apply();
        new SootGraphConverter(nodeFactory(request), request.scope()).convert(Scene.v().getCallGraph(), graph);
        G.reset(); // memória felszabadítása a következő futás előtt
    }

    /**
     * A Soot globális beállításainak elvégzése a kérés alapján.
     */
    private static void configureSoot(BuildRequest request) {
        G.reset();
        Options o = Options.v();
        o.set_whole_program(true);
        o.set_allow_phantom_refs(true);
        o.set_keep_line_number(true);
        o.set_prepend_classpath(true);
        o.set_no_bodies_for_excluded(true);
        o.set_src_prec(Options.src_prec_only_class);
        o.set_output_format(Options.output_format_none);
        o.set_process_dir(List.of(request.input().toAbsolutePath().toString()));
        if (!request.classpath().isEmpty()) {
            o.set_soot_classpath(request.classpath().stream()
                    .map(Path::toAbsolutePath).map(Path::toString)
                    .collect(Collectors.joining(File.pathSeparator)));
        }

        // Hívási gráf: CHA, belépési pont az alkalmazás minden metódusa
        // (könyvtárszerű kódnál nincs egyetlen main, így minden metódus elérhetőnek számít)
        o.setPhaseOption("cg", "all-reachable:true");
        o.setPhaseOption("cg.cha", "enabled:true");
        o.setPhaseOption("cg.spark", "enabled:false");
    }
}
