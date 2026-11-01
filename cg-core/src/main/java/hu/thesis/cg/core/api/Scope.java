package hu.thesis.cg.core.api;

/**
 * A gráf hatóköre: milyen metódusok kerüljenek csúcsként a gráfba.
 */
public enum Scope {
    /** Csak az elemzett alkalmazás metódusai; a könyvtárba mutató élek kimaradnak. */
    APPLICATION_ONLY,
    /** Az alkalmazásból közvetlenül hívott könyvtári metódusok is csúcsok lesznek. */
    WITH_LIBRARY
}
