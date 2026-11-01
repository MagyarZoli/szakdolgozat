package hu.thesis.cg.core.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * A classpath-on elérhető builderek nyilvántartása, azonosító szerint.
 */
public final class BuilderRegistry {
    private final Map<String, CallGraphBuilder> builders = new TreeMap<>();

    public BuilderRegistry() {}

    /**
     * A classpath-on található összes builder felderítése és regisztrálása.
     */
    public static BuilderRegistry discover() {
        BuilderRegistry registry = new BuilderRegistry();
        ServiceLoader.load(CallGraphBuilder.class).forEach(registry::register);
        return registry;
    }

    /**
     * Builder kézi regisztrálása (például tesztben).
     */
    public void register(CallGraphBuilder builder) {
        CallGraphBuilder previous = builders.putIfAbsent(builder.id(), builder);
        if (previous != null && previous != builder) {
            throw new IllegalStateException("Két builder azonos azonosítóval: " + builder.id());
        }
    }

    /**
     * Builder keresése azonosító szerint.
     */
    public Optional<CallGraphBuilder> find(String id) {
        return Optional.ofNullable(builders.get(id));
    }

    /**
     * Builder keresése azonosító szerint, kötelezően.
     */
    public CallGraphBuilder require(String id) throws BuildException {
        return find(id).orElseThrow(() -> new BuildException(
                "Ismeretlen eszköz: '" + id + "'. Elérhető: " + builders.keySet()));
    }

    /**
     * Az összes regisztrált builder azonosító szerinti sorrendben.
     */
    public Collection<CallGraphBuilder> all() {
        return Collections.unmodifiableCollection(builders.values());
    }
}


























