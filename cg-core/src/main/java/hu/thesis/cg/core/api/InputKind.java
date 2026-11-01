package hu.thesis.cg.core.api;

/**
 * A bemenet fajtája.
 */
public enum InputKind {
    /** Lefordított bemenet: .jar fájl vagy .class fájlokat tartalmazó könyvtár (például Soot). */
    BYTECODE,
    /** Forráskód: .java fájlokat tartalmazó projektkönyvtár (például OSA). */
    SOURCE
}
