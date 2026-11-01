package hu.thesis.cg.core.api;

import hu.thesis.cg.core.signature.SignatureStyle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Egy gráfépítés összes paramétere, egyetlen immutable objektumban.
 */
public record BuildRequest(
        InputKind inputKind,
        Path input,
        List<Path> classpath,
        Strategy strategy,
        Scope scope,
        SignatureStyle signatureStyle,
        String project,
        String projectVersion,
        Path workDir,
        ToolSettings settings) {

    /**
     * konstruktor: ellenőrzi a kötelező mezőket, és kitölti az alapértékeket.
     */
    public BuildRequest {
        Objects.requireNonNull(inputKind, "inputKind");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(workDir, "workDir");
        classpath = List.copyOf(classpath);
        scope = scope == null ? Scope.WITH_LIBRARY : scope;
        signatureStyle = signatureStyle == null ? SignatureStyle.DEFAULT : signatureStyle;
        settings = settings == null ? ToolSettings.empty() : settings;
        project = project == null || project.isBlank() ? defaultProjectName(input) : project;
        projectVersion = projectVersion == null || projectVersion.isBlank() ? "unknown" : projectVersion;
    }

    /**
     * Alapértelmezett projektnév a bemenet nevéből.
     */
    private static String defaultProjectName(Path input) {
        Path name = input.toAbsolutePath().normalize().getFileName();
        String s = name == null ? "project" : name.toString();
        return s.endsWith(".jar") ? s.substring(0, s.length() - 4) : s;
    }

    /**
     * Új, üres építő.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Épító a BuildRequest-hez. Minden beállító metódus az építőt adja vissza,
     * így a hívások láncolhatók; a build() hozza létre a kérést.
     */
    public static final class Builder {
        // A mezők jelentése megegyezik a BuildRequest azonos nevű komponensével;
        // a még be nem állított mező null (illetve üres lista), az alapértéket a build() tölti ki.
        private InputKind inputKind;
        private Path input;
        private final List<Path> classpath = new ArrayList<>();
        private Strategy strategy;
        private Scope scope;
        private SignatureStyle signatureStyle;
        private String project;
        private String projectVersion;
        private Path workDir;
        private ToolSettings settings;

        private Builder() {}

        /**
         * Bájtkód bemenet beállítása.
         */
        public Builder bytecode(Path path) {
            this.inputKind = InputKind.BYTECODE;
            this.input = path;
            return this;
        }

        /**
         * Forráskód bemenet beállítása.
         */
        public Builder source(Path path) {
            this.inputKind = InputKind.SOURCE;
            this.input = path;
            return this;
        }

        /**
         * Függőségek hozzáadása; többször hívva bővíti a listát.
         */
        public Builder classpath(List<Path> entries) {
            this.classpath.addAll(entries);
            return this;
        }

        /**
         * A gráfépítési stratégia beállítása (kötelező).
         */
        public Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * A gráf hatókörének beállítása.
         */
        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * A csúcsazonosítók stílusának beállítása.
         */
        public Builder signatureStyle(SignatureStyle style) {
            this.signatureStyle = style;
            return this;
        }

        /**
         * A projekt nevének és verziójának beállítása (a kimenet útvonalában és a metaadatokban jelenik meg).
         */
        public Builder project(String project, String version) {
            this.project = project;
            this.projectVersion = version;
            return this;
        }

        /**
         * A munkakönyvtár beállítása (kötelező).
         */
        public Builder workDir(Path workDir) {
            this.workDir = workDir;
            return this;
        }

        /**
         * Az eszközbeállítások megadása.
         */
        public Builder settings(ToolSettings settings) {
            this.settings = settings;
            return this;
        }

        /**
         * A kérés létrehozása. Az építő ezután is használható (például módosított másolathoz).
         */
        public BuildRequest build() {
            return new BuildRequest(inputKind, input, classpath, strategy, scope, signatureStyle, project, projectVersion, workDir, settings);
        }
    }
}















