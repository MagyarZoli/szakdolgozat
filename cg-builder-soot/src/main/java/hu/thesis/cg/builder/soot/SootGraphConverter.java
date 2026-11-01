package hu.thesis.cg.builder.soot;

import hu.thesis.cg.core.api.Scope;
import hu.thesis.cg.core.model.CallEdge;
import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.CallKind;
import hu.thesis.cg.core.model.MethodFlag;
import hu.thesis.cg.core.model.MethodNode;
import hu.thesis.cg.core.model.NodeFactory;
import hu.thesis.cg.core.signature.MethodSignature;
import soot.Kind;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Soot saját hívásigráf-modelljének átalakítása az egységes CallGraph-ba.
 */
final class SootGraphConverter {

    private final NodeFactory factory;
    private final Scope scope;
    private final Map<SootMethod, MethodNode> cache = new HashMap<>();

    /**
     * Átalakító létrehozása.
     */
    SootGraphConverter(NodeFactory factory, Scope scope) {
        this.factory = factory;
        this.scope = scope;
    }

    /**
     * Az átalakítás: először az alkalmazás összes metódusa csúcsként, majd az élek.
     */
    void convert(soot.jimple.toolkits.callgraph.CallGraph sootGraph, CallGraph graph) {
        // Az alkalmazás minden metódusa csúcs, akkor is, ha nincs éle (izolált csúcsok is számítanak).
        // Másolaton iterálunk, mert a Soot lustán bővítheti a listákat.
        for (SootClass cls : new ArrayList<>(Scene.v().getApplicationClasses())) {
            for (SootMethod m : new ArrayList<>(cls.getMethods())) {
                graph.addNode(toNode(m));
            }
        }

        // Élek: csak az alkalmazásból induló hívások; a könyvtáron belüli hívásokat nem követjük
        for (Edge e : sootGraph) {
            SootMethod src = e.src();
            SootMethod tgt = e.tgt();
            if (src == null || tgt == null || !isApplication(src)) {
                continue;
            }
            if (scope == Scope.APPLICATION_ONLY && !isApplication(tgt)) {
                continue;
            }
            MethodNode s = graph.addNode(toNode(src));
            MethodNode t = graph.addNode(toNode(tgt));
            graph.addEdge(new CallEdge(s.id(), t.id(), mapKind(e.kind()), lineOf(e.srcUnit())));
        }
    }

    /**
     * Csúcs egy Soot-metódushoz, gyorsítótárral.
     */
    private MethodNode toNode(SootMethod m) {
        return cache.computeIfAbsent(m, this::createNode);
    }

    /**
     * Új csúcs létrehozása egy Soot-metódusból: leírás, eszköztől kapott jelzők, forrásfájl, sor.
     */
    private MethodNode createNode(SootMethod m) {
        SootClass cls = m.getDeclaringClass();
        MethodSignature sig = new MethodSignature(cls.getName(), m.getName(),
                m.getParameterTypes().stream().map(Type::toString).toList(), m.getReturnType().toString());

        Set<MethodFlag> flags = EnumSet.noneOf(MethodFlag.class);
        if (m.isStatic()) {
            flags.add(MethodFlag.STATIC);
        }
        if (m.isAbstract()) {
            flags.add(MethodFlag.ABSTRACT);
        }
        if ((m.getModifiers() & Modifier.SYNTHETIC) != 0) {
            flags.add(MethodFlag.SYNTHETIC);
        }
        if (!isApplication(m)) {
            flags.add(MethodFlag.LIBRARY);
        }
        if (m.isPhantom() || cls.isPhantom()) {
            flags.add(MethodFlag.PHANTOM);
        }
        return factory.create(sig, sourceFileOf(cls), firstLineOf(m), flags);
    }

    /**
     * Az alkalmazás része-e a metódus.
     */
    private static boolean isApplication(SootMethod m) {
        return m.getDeclaringClass().isApplicationClass();
    }

    /**
     * A forrásfájl neve a class-fájl SourceFile attribútumából.
     */
    private static String sourceFileOf(SootClass cls) {
        Tag tag = cls.getTag(SourceFileTag.NAME);
        return tag instanceof SourceFileTag sft ? sft.getSourceFile() : null;
    }

    /**
     * A metódus első sora: a törzs utasításainak legkisebb sorszáma.
     */
    private static int firstLineOf(SootMethod m) {
        if (!m.hasActiveBody()) {
            return -1;
        }
        int min = Integer.MAX_VALUE;
        for (Unit u : m.getActiveBody().getUnits()) {
            int line = u.getJavaSourceStartLineNumber();
            if (line > 0 && line < min) {
                min = line;
            }
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

    /**
     * A hívási utasítás sora.
     */
    private static int lineOf(Unit u) {
        return u == null ? -1 : u.getJavaSourceStartLineNumber();
    }

    /**
     * A Soot élfajtájának leképezése az egységes CallKind-ra.
     */
    private static CallKind mapKind(Kind k) {
        if (k == Kind.STATIC) {
            return CallKind.STATIC;
        } else if (k == Kind.VIRTUAL) {
            return CallKind.VIRTUAL;
        } else if (k == Kind.INTERFACE) {
            return CallKind.INTERFACE;
        } else if (k == Kind.SPECIAL) {
            return CallKind.SPECIAL;
        } else if (k == Kind.CLINIT) {
            return CallKind.STATIC_INIT;
        }
        return CallKind.OTHER; // szál, finalize, reflexio, privileged stb. implicit elek
    }
}
