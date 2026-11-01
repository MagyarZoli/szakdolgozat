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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Az egységes kimeneti formátum: egy könyvtár három UTF-8 fájllal.
 */
public final class CsvGraphFormat implements GraphWriter, GraphReader {
    public static final String NODES_FILE = "nodes.csv";
    public static final String EDGES_FILE = "edges.csv";
    public static final String META_FILE = "meta.json";
    static final String NODES_HEADER = "id,class,method,params,return,file,line,flags";
    static final String EDGES_HEADER = "source,target,kind,line";

    @Override
    public void write(CallGraph graph, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        try (BufferedWriter w = Files.newBufferedWriter(outputDir.resolve(NODES_FILE), StandardCharsets.UTF_8)) {
            w.write(NODES_HEADER);
            w.newLine();
            for (MethodNode n : graph.nodes()) {
                MethodSignature s = n.signature();
                w.write(Csv.row(n.id(), s.className(), s.methodName(), String.join(";", s.parameterTypes()),
                        s.returnType(), n.sourceFile(), n.line(),
                        n.flags().stream().map(Enum::name).collect(Collectors.joining("|"))));
                w.newLine();
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(outputDir.resolve(EDGES_FILE), StandardCharsets.UTF_8)) {
            w.write(EDGES_HEADER);
            w.newLine();
            for (CallEdge e : graph.edges()) {
                w.write(Csv.row(e.sourceId(), e.targetId(), e.kind(), e.callSiteLine()));
                w.newLine();
            }
        }

        Files.writeString(outputDir.resolve(META_FILE), FlatJson.write(metaToMap(graph)), StandardCharsets.UTF_8);
    }

    /**
     * A metaadatok összeállítása a meta.json -hoz.
     */
    private static Map<String, Object> metaToMap(CallGraph graph) {
        GraphMetadata m = graph.metadata();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", 1);
        map.put("tool", m.tool());
        map.put("toolVersion", m.toolVersion());
        map.put("strategy", m.strategy().name());
        map.put("inputKind", m.inputKind().name());
        map.put("input", m.input());
        map.put("scope", m.scope().name());
        map.put("signatureStyle", m.signatureStyle().name());
        map.put("project", m.project());
        map.put("projectVersion", m.projectVersion());
        map.put("createdAt", m.createdAt().toString());
        map.put("buildMillis", m.buildMillis());
        map.put("nodeCount", graph.nodeCount());
        map.put("edgeCount", graph.edgeCount());
        return map;
    }

    @Override
    public CallGraph read(Path graphDir) throws IOException {
        Map<String, String> meta = FlatJson.read(Files.readString(graphDir.resolve(META_FILE), StandardCharsets.UTF_8));
        CallGraph graph = new CallGraph(new GraphMetadata(
                meta.get("tool"),
                meta.get("toolVersion"),
                Strategy.valueOf(meta.get("strategy")),
                InputKind.valueOf(meta.get("inputKind")),
                meta.get("input"),
                Scope.valueOf(meta.get("scope")),
                SignatureStyle.valueOf(meta.get("signatureStyle")),
                meta.get("project"),
                meta.get("projectVersion"),
                Instant.parse(meta.get("createdAt")),
                Long.parseLong(meta.get("buildMillis"))));

        try (BufferedReader r = Files.newBufferedReader(graphDir.resolve(NODES_FILE), StandardCharsets.UTF_8)) {
            requireHeader(r.readLine(), NODES_HEADER, NODES_FILE);
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> f = Csv.parse(line);
                List<String> params = f.get(3).isEmpty() ? List.of() : Arrays.asList(f.get(3).split(";"));
                MethodSignature sig = new MethodSignature(f.get(1), f.get(2), params, f.get(4));
                graph.addNode(new MethodNode(f.get(0), sig, f.get(5).isEmpty() ? null : f.get(5),
                        Integer.parseInt(f.get(6)), parseFlags(f.get(7))));
            }
        }

        try (BufferedReader r = Files.newBufferedReader(graphDir.resolve(EDGES_FILE), StandardCharsets.UTF_8)) {
            requireHeader(r.readLine(), EDGES_HEADER, EDGES_FILE);
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> f = Csv.parse(line);
                graph.addEdge(new CallEdge(f.get(0), f.get(1), CallKind.valueOf(f.get(2)), Integer.parseInt(f.get(3))));
            }
        }
        return graph;
    }

    /**
     * A flags oszlop feldolgozása.
     */
    private static Set<MethodFlag> parseFlags(String text) {
        Set<MethodFlag> flags = EnumSet.noneOf(MethodFlag.class);
        if (!text.isBlank()) {
            for (String s : text.split("\\|")) {
                flags.add(MethodFlag.valueOf(s.trim()));
            }
        }
        return flags;
    }

    /**
     * A fejléc ellenőrzése, hogy egy régi vagy idegen formátumú fájl ne olvasódjon be hibásan.
     */
    private static void requireHeader(String actual, String expected, String file) throws IOException {
        if (!expected.equals(actual)) {
            throw new IOException(file + ": ismeretlen fejléc. Várt: '" + expected + "', kapott: '" + actual + "'");
        }
    }
}
