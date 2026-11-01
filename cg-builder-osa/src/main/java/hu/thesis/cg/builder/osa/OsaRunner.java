package hu.thesis.cg.builder.osa;

import hu.thesis.cg.core.api.BuildException;
import hu.thesis.cg.core.api.BuildRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Az OSA Java-elemzőjének futtatása külső folyamatként.
 */
final class OsaRunner {

    static final String LOG_FILE = "osa.log";

    private final List<String> commandTemplate;
    private final Map<String, String> variables;
    private final long timeoutMinutes;

    /**
     * Futtató létrehozása közvetlenül (tesztekhez)
     */
    OsaRunner(String commandTemplate, Map<String, String> variables, long timeoutMinutes) {
        this.commandTemplate = List.of(commandTemplate.trim().split("\\s+"));
        this.variables = Map.copyOf(variables);
        this.timeoutMinutes = timeoutMinutes;
    }

    /**
     * Futtató létrehozása a kérés és a beállítások alapján.
     */
    static OsaRunner from(BuildRequest request) throws BuildException {
        var s = request.settings();
        return new OsaRunner(
                s.require("osa.command"),
                Map.of("osa.home", s.require("osa.home"),
                        "project", request.project(),
                        "input", request.input().toAbsolutePath().normalize().toString(),
                        "workDir", request.workDir().toAbsolutePath().normalize().toString()),
                Long.parseLong(s.get("osa.timeoutMinutes").orElse("120")));
    }

    /**
     * A végleges parancs a változók behelyettesítése után.
     */
    List<String> command() {
        List<String> cmd = new ArrayList<>();
        for (String token : commandTemplate) {
            String t = token;
            for (Map.Entry<String, String> v : variables.entrySet()) {
                t = t.replace("${" + v.getKey() + "}", v.getValue());
            }
            cmd.add(t);
        }
        return cmd;
    }

    /**
     * Az OSA futtatása és megvárása.
     */
    void run(Path workDir) throws BuildException {
        List<String> cmd = command();
        Path log = workDir.resolve(LOG_FILE);
        try {
            Process p = new ProcessBuilder(cmd)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(log.toFile())
                    .start();
            if (!p.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                throw new BuildException("Az OSA nem végzett " + timeoutMinutes + " perc alatt. Napló: " + log);
            }
            if (p.exitValue() != 0) {
                throw new BuildException("Az OSA " + p.exitValue() + " kóddal állt le. Napló: " + log);
            }
        } catch (IOException e) {
            throw new BuildException("Az OSA nem indítható: " + String.join(" ", cmd), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BuildException("Az OSA futása megszakadt", e);
        }
    }
}
