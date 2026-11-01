package hu.thesis.cg.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Eszközfüggetlen hívási gráf: csúcsok, élek és az előállítás metaadatai.
 */
public final class CallGraph {
    private final GraphMetadata metadata;
    /** Csúcsok azonosító szerint, beszúrási sorrendben. */
    private final Map<String, MethodNode> nodes = new LinkedHashMap<>();
    /** Élek beszúrási sorrendben, ismétlődés nélkül. */
    private final Set<CallEdge> edges = new LinkedHashSet<>();

    /**
     * Üres gráf létrehozása.
     */
    public CallGraph(GraphMetadata metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
    }

    /**
     * Az előállítás körülményei.
     */
    public GraphMetadata metadata() {
        return metadata;
    }

    /**
     * Csúcs felvétele.
     */
    public MethodNode addNode(MethodNode node) {
        return nodes.putIfAbsent(node.id(), node) == null ? node : nodes.get(node.id());
    }

    /**
     * Él felvétele.
     */
    public boolean addEdge(CallEdge edge) {
        if (!nodes.containsKey(edge.sourceId()) || !nodes.containsKey(edge.targetId())) {
            throw new IllegalStateException("Az él végpontja nem csúcs: " + edge);
        }
        return edges.add(edge);
    }

    /**
     * Csúcs keresése azonosító szerint.
     */
    public Optional<MethodNode> node(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    /**
     * Az összes csúcs beszúrási sorrendben.
     */
    public Collection<MethodNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Az összes él beszúrási sorrendben.
     */
    public Set<CallEdge> edges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * A csúcsok száma.
     */
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Az élek (hívási helyek) száma; az azonos csúcspár közötti többszörös élek külön számítanak.
     */
    public int edgeCount() {
        return edges.size();
    }

    /**
     * Másolat új metaadatokkal; a csúcsok és élek ugyanazok (a record-ok immutable-ek,
     * ezért elég sekély másolat).
     */
    public CallGraph withMetadata(GraphMetadata newMetadata) {
        CallGraph copy = new CallGraph(newMetadata);
        copy.nodes.putAll(nodes);
        copy.edges.addAll(edges);
        return copy;
    }
}


