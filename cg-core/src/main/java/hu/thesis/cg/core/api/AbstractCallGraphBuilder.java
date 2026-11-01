package hu.thesis.cg.core.api;

import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.GraphMetadata;
import hu.thesis.cg.core.model.NodeFactory;

import java.nio.file.Files;
import java.time.Instant;

/**
 * Közös alaposztály a builderekhez, hogy az azonosítók és a jelzők minden eszköznél egységesek legyenek.</p>
 */
public abstract class AbstractCallGraphBuilder implements CallGraphBuilder {
    protected AbstractCallGraphBuilder() {}

    @Override
    public final CallGraph build(BuildRequest request) throws BuildException {
        validate(request);
        GraphMetadata meta = new GraphMetadata(id(), toolVersion(), request.strategy(), request.inputKind(),
                request.input().toAbsolutePath().normalize().toString(), request.scope(),
                request.signatureStyle(), request.project(), request.projectVersion(), Instant.now(), 0);
        CallGraph graph = new CallGraph(meta);

        long start = System.nanoTime();
        try {
            Files.createDirectories(request.workDir());
            populate(request, graph);
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(id() + ": a gráfépítés nem sikerült: " + e.getMessage(), e);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        return graph.withMetadata(meta.withBuildMillis(millis));
    }

    /**
     * Az eszközspecifikus rész: a kérés alapján feltölti a gráfot csúcsokkal és élekkel.
     */
    protected abstract void populate(BuildRequest request, CallGraph graph) throws Exception;

    /**
     * A kérés általános ellenőrzése a gráfépítés előtt.
     */
    protected void validate(BuildRequest request) throws BuildException {
        if (!supportedStrategies().contains(request.strategy())) {
            throw new BuildException(id() + " nem támogatja a(z) " + request.strategy()
                    + " stratégiát. Támogatott: " + supportedStrategies());
        }
        if (!supportedInputs().contains(request.inputKind())) {
            throw new BuildException(id() + " nem fogad " + request.inputKind() + " bemenetet. Támogatott: "
                    + supportedInputs() + " (használd a "
                    + (request.inputKind() == InputKind.SOURCE ? "--bytecode" : "--source") + " kapcsolót)");
        }
        if (!Files.exists(request.input())) {
            throw new BuildException("A bemenet nem létezik: " + request.input());
        }
    }

    /**
     * A kérés azonosítóstílusához tartozó csúcsgyár.
     */
    protected static NodeFactory nodeFactory(BuildRequest request) {
        return new NodeFactory(request.signatureStyle());
    }
}























































