package hu.thesis.cg.core.io;

import hu.thesis.cg.core.api.InputKind;
import hu.thesis.cg.core.api.Scope;
import hu.thesis.cg.core.api.Strategy;
import hu.thesis.cg.core.model.CallEdge;
import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.CallKind;
import hu.thesis.cg.core.model.GraphMetadata;
import hu.thesis.cg.core.model.MethodFlag;
import hu.thesis.cg.core.model.MethodNode;
import hu.thesis.cg.core.signature.MethodSignature;
import hu.thesis.cg.core.signature.SignatureStyle;
import hu.thesis.cg.core.stats.GraphStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Az egységes kimeneti formátum és a statisztikák tesztjei egy kis, kézzel összerakott gráfon.
 */
class CsvGraphFormatTest {

    /**
     * Mintagráf: 2 alkalmazáscsúcs (egy statikus és egy anonim osztálybeli) és 1 könyvtári
     * konstruktor; élek: kölcsönös hívás A ↔ A$1, hurokél A → A, és A$1 → Object.&lt;init&gt;.
     */
    static CallGraph sampleGraph(SignatureStyle style) {
        CallGraph g = new CallGraph(new GraphMetadata("test", "1", Strategy.CHA, InputKind.BYTECODE, "/x.jar",
                Scope.WITH_LIBRARY, style, "demo", "1.0", Instant.parse("2026-10-31T12:00:00Z"), 42));
        MethodNode a = node(style, "p.A", "a", List.of("int", "java.lang.String"), Set.of(MethodFlag.STATIC));
        MethodNode b = node(style, "p.A$1", "b", List.of(), Set.of(MethodFlag.ANONYMOUS_CLASS, MethodFlag.INNER_CLASS));
        MethodNode lib = node(style, "java.lang.Object", "<init>", List.of(),
                Set.of(MethodFlag.LIBRARY, MethodFlag.CONSTRUCTOR));
        g.addNode(a);
        g.addNode(b);
        g.addNode(lib);
        g.addEdge(new CallEdge(a.id(), b.id(), CallKind.VIRTUAL, 10));
        g.addEdge(new CallEdge(b.id(), a.id(), CallKind.STATIC, 20));
        g.addEdge(new CallEdge(a.id(), a.id(), CallKind.STATIC, 11));
        g.addEdge(new CallEdge(b.id(), lib.id(), CallKind.SPECIAL, -1));
        return g;
    }

    /**
     * Segéd egy csúcs létrehozásához void visszatérési típussal.
     */
    private static MethodNode node(SignatureStyle style, String cls, String name, List<String> params,
                                   Set<MethodFlag> flags) {
        MethodSignature s = new MethodSignature(cls, name, params, "void");
        return new MethodNode(style.format(s), s, "A.java", 5, EnumSet.copyOf(flags));
    }

    /**
     * Kiírás, majd visszaolvasás után a gráf (csúcsok sorrenddel, élek, metaadatok) változatlan.
     * A JAVA stílus azonosítója vesszőt tartalmaz, így ez a CSV-idézést is teszteli.
     */
    @Test
    void writeThenReadGivesSameGraph(@TempDir Path dir) throws Exception {
        for (SignatureStyle style : SignatureStyle.values()) {
            CallGraph g = sampleGraph(style);
            Path out = dir.resolve(style.name());
            CsvGraphFormat format = new CsvGraphFormat();
            format.write(g, out);
            CallGraph back = format.read(out);

            assertEquals(List.copyOf(g.nodes()), List.copyOf(back.nodes()));
            assertEquals(g.edges(), back.edges());
            assertEquals(g.metadata(), back.metadata());
        }
    }

    /** A statisztikák a mintagráfon: 1 hurokél, 1 kölcsönös pár, nincs izolált csúcs. */
    @Test
    void statistics() {
        GraphStatistics s = GraphStatistics.of(sampleGraph(SignatureStyle.JVM));
        assertEquals(3, s.nodes());
        assertEquals(4, s.edges());
        assertEquals(2, s.applicationNodes());
        assertEquals(1, s.selfLoops());
        assertEquals(1, s.mutualCallPairs());
        assertEquals(0, s.isolatedNodes());
    }
}
