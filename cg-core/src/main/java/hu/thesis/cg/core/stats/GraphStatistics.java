package hu.thesis.cg.core.stats;

import hu.thesis.cg.core.model.CallEdge;
import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.CallKind;
import hu.thesis.cg.core.model.MethodFlag;
import hu.thesis.cg.core.model.MethodNode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Egy gráf leíró statisztikái.
 */
public record GraphStatistics(
        int nodes,
        int edges,
        int applicationNodes,
        int libraryNodes,
        int isolatedNodes,
        int distinctCallPairs,
        int selfLoops,
        int mutualCallPairs,
        int maxOutDegree,
        int maxInDegree,
        double avgOutDegreeApplication,
        Map<MethodFlag, Integer> flagCounts,
        Map<CallKind, Integer> edgeKindCounts) {

    /**
     * A statisztikák kiszámítása. Futásideje lineáris az élek és csúcsok számában.
     */
    public static GraphStatistics of(CallGraph g) {
        Map<String, Integer> out = new HashMap<>();
        Map<String, Integer> in = new HashMap<>();
        Set<String> pairKeys = new HashSet<>();
        Map<CallKind, Integer> kinds = new EnumMap<>(CallKind.class);
        int selfLoops = 0;

        // FIXME:
        for (CallEdge e : g.edges()) {
            kinds.merge(e.kind(), 1, Integer::sum);
            if (pairKeys.add(e.sourceId() + "\u0000" + e.targetId())) {
                out.merge(e.sourceId(), 1, Integer::sum);
                in.merge(e.targetId(), 1, Integer::sum);
                if (e.isSelfLoop()) {
                    selfLoops++;
                }
            }
        }
        int mutual = 0;
        for (String key : pairKeys) {
            int sep = key.indexOf('\u0000');
            String a = key.substring(0, sep);
            String b = key.substring(sep + 1);
            if (a.compareTo(b) < 0 && pairKeys.contains(b + "\u0000" + a)) {
                mutual++;
            }
        }

        Map<MethodFlag, Integer> flags = new EnumMap<>(MethodFlag.class);
        int app = 0;
        int isolated = 0;
        long appOut = 0;
        for (MethodNode n : g.nodes()) {
            n.flags().forEach(f -> flags.merge(f, 1, Integer::sum));
            if (n.isApplication()) {
                app++;
                appOut += out.getOrDefault(n.id(), 0);
            }
            if (!out.containsKey(n.id()) && !in.containsKey(n.id())) {
                isolated++;
            }
        }

        return new GraphStatistics(
                g.nodeCount(), g.edgeCount(), app, g.nodeCount() - app, isolated, pairKeys.size(),
                selfLoops, mutual,
                out.values().stream().mapToInt(Integer::intValue).max().orElse(0),
                in.values().stream().mapToInt(Integer::intValue).max().orElse(0),
                app == 0 ? 0 : (double) appOut / app,
                flags, kinds);
    }
}
