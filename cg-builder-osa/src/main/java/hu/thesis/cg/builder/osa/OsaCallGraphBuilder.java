package hu.thesis.cg.builder.osa;

import hu.thesis.cg.core.api.AbstractCallGraphBuilder;
import hu.thesis.cg.core.api.BuildException;
import hu.thesis.cg.core.api.BuildRequest;
import hu.thesis.cg.core.api.InputKind;
import hu.thesis.cg.core.api.Scope;
import hu.thesis.cg.core.api.Strategy;
import hu.thesis.cg.core.model.CallEdge;
import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.CallKind;
import hu.thesis.cg.core.model.MethodFlag;
import hu.thesis.cg.core.model.MethodNode;
import hu.thesis.cg.core.model.NodeFactory;
import hu.thesis.cg.core.signature.MethodSignature;
import hu.thesis.cg.core.signature.SignatureStyle;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * forráskódból, deklarált típus szerinti (névalapú) feloldással.
 */
public final class OsaCallGraphBuilder extends AbstractCallGraphBuilder {

    /**
     * Napló a figyelmeztetésekhez (újrahasznosított eredmény, értelmezhetetlen sorok, 0 él).
     */
    private static final Logger LOG = Logger.getLogger(OsaCallGraphBuilder.class.getName());

    public OsaCallGraphBuilder() {
    }

    @Override
    public String id() {
        return "osa";
    }

    @Override
    public String description() {
        return "OpenStaticAnalyzer, forráskód alapú elemzés (deklarált típus szerinti feloldás)";
    }

    @Override
    public String toolVersion() {
        return "external";
    }

    @Override
    public Set<Strategy> supportedStrategies() {
        return EnumSet.of(Strategy.DECLARED_TYPE);
    }

    @Override
    public Set<InputKind> supportedInputs() {
        return EnumSet.of(InputKind.SOURCE);
    }

    /**
     * Az értelmezhetetlen LongName metódusokat kihagyja és megszámolja (az első ötöt
     * a naplóba is kiírja), mert ezek a novemberi párosítási elemzésnél külön kategóriát jelentenek.
     * A könyvtári hívottak esetén jelzős
     * csúcsként kerülnek be, fájl- és sorinformáció nélkül.
     */
    @Override
    protected void populate(BuildRequest request, CallGraph graph) throws Exception {
        Path workDir = request.workDir();
        boolean reuse = Boolean.parseBoolean(request.settings().get("osa.reuseResults").orElse("false"));

        Path methodCsv = reuse ? OsaMethodCsvReader.findMethodCsv(workDir).orElse(null) : null;
        if (methodCsv == null) {
            OsaRunner.from(request).run(workDir);
            methodCsv = OsaMethodCsvReader.findMethodCsv(workDir).orElseThrow(() -> new BuildException(
                    "Az OSA lefutott, de nem található *-Method.csv itt: " + workDir));
        } else {
            LOG.info("Korábbi OSA-eredmény újrahasznosítva: " + methodCsv);
        }

        NodeFactory factory = nodeFactory(request);
        // LongName -> csúcs; az OSA LongName a JVM-stílusú azonosító
        Map<String, MethodNode> byLongName = new HashMap<>();
        int unparsable = 0;
        for (OsaMethod m : new OsaMethodCsvReader().read(methodCsv)) {
            MethodSignature sig;
            try {
                sig = SignatureStyle.JVM.parse(m.longName());
            } catch (IllegalArgumentException e) {
                // Nem várt LongName-alak: kihagyjuk, de számoljuk
                if (unparsable++ < 5) {
                    LOG.warning("Nem értelmezhető LongName: " + m.longName() + " (" + e.getMessage() + ")");
                }
                continue;
            }
            byLongName.put(m.longName(), graph.addNode(factory.create(sig, fileName(m.path()), m.line(),
                    EnumSet.noneOf(MethodFlag.class))));
        }
        if (unparsable > 0) {
            LOG.warning("OSA: " + unparsable + " metódus LongName-je nem értelmezhető, kihagyva");
        }

        OsaCallSource calls = OsaCallSource.from(request.settings());
        var raw = calls.read(workDir);
        if (raw.isEmpty()) {
            LOG.warning("OSA: 0 él. Élforrás: " + calls.describe());
        }
        for (OsaCallSource.RawCall c : raw) {
            MethodNode src = byLongName.get(c.callerLongName());
            if (src == null) {
                continue;
            }
            MethodNode tgt = byLongName.get(c.calleeLongName());
            if (tgt == null) {
                if (request.scope() == Scope.APPLICATION_ONLY) {
                    continue;
                }
                MethodSignature sig = SignatureStyle.JVM.parse(c.calleeLongName());
                tgt = graph.addNode(factory.create(sig, null, -1, EnumSet.of(MethodFlag.LIBRARY)));
                byLongName.put(c.calleeLongName(), tgt);
            }
            graph.addEdge(new CallEdge(src.id(), tgt.id(), CallKind.UNKNOWN, c.line()));
        }
    }

    private static String fileName(String path) {
        if (path == null) {
            return null;
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return path.substring(slash + 1);
    }
}
