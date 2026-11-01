package hu.thesis.cg.builder.osa;

import hu.thesis.cg.core.api.BuildRequest;
import hu.thesis.cg.core.api.Scope;
import hu.thesis.cg.core.api.Strategy;
import hu.thesis.cg.core.api.ToolSettings;
import hu.thesis.cg.core.model.CallGraph;
import hu.thesis.cg.core.model.MethodFlag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Az OSA kimenetének feldolgozása egy kézzel írt mintán
 * A minta oszlopai az OSA Method.csv-jét követik; ha a valódi
 * kimenet eltér, ezt a tesztet kell először hozzáigazítani.
 */
class OsaCallGraphBuilderTest {

    /**
     * Ellenőrzi: a hibás LongName-ű sor kimarad; a könyvtári hívott (println) LIBRARY jelzős
     * csúcs lesz; a belső osztály INNER_CLASS jelzőt kap; a fájlnév az útvonalból kerül ki;
     * a hurokél (Worker.run → önmaga) is bekerül.
     */
    @Test
    void readsMethodsAndCalls(@TempDir Path tmp) throws Exception {
        Path src = Files.createDirectories(tmp.resolve("src"));
        Path work = Files.createDirectories(tmp.resolve("work/demo/java/run"));
        Files.writeString(work.resolve("demo-Method.csv"), """
                "ID","Name","LongName","Parent","Component","Path","Line","Column"
                "L1","main","p.App.main([Ljava/lang/String;)V","L0","L9","/x/p/App.java","5","3"
                "L2","run","p.App$Worker.run()V","L0","L9","/x/p/App.java","12","5"
                "L3","weird","not a long name","L0","L9","","",""
                """);
        Files.writeString(work.resolve("demo-Calls.csv"), """
                caller,callee,line
                p.App.main([Ljava/lang/String;)V,p.App$Worker.run()V,7
                p.App.main([Ljava/lang/String;)V,java.io.PrintStream.println(Ljava/lang/String;)V,8
                p.App$Worker.run()V,p.App$Worker.run()V,13
                """);

        ToolSettings settings = new ToolSettings(Map.of(
                "osa.reuseResults", "true", "osa.calls.source", "csv", "osa.calls.file", "*-Calls.csv"));
        BuildRequest req = BuildRequest.builder().source(src).strategy(Strategy.DECLARED_TYPE)
                .scope(Scope.WITH_LIBRARY).project("demo", "1").workDir(tmp.resolve("work"))
                .settings(settings).classpath(List.of()).build();

        CallGraph g = new OsaCallGraphBuilder().build(req);

        assertEquals(3, g.nodeCount());   // 2 alkalmazás + 1 könyvtári, a hibás sor kihagyva
        assertEquals(3, g.edgeCount());
        assertTrue(g.node("java.io.PrintStream.println(Ljava/lang/String;)V").orElseThrow().has(MethodFlag.LIBRARY));
        assertTrue(g.node("p.App$Worker.run()V").orElseThrow().has(MethodFlag.INNER_CLASS));
        assertEquals("App.java", g.node("p.App.main([Ljava/lang/String;)V").orElseThrow().sourceFile());
    }
}
