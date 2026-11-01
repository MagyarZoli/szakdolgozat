package hu.thesis.cg.core.api;

/**
 * Gráfépítési stratégia: hogyan oldjuk fel a virtuális (polimorf) hívások célpontját.
 */
public enum Strategy {
    /** Csak a deklarált (statikus) típus metódusára. */
    DECLARED_TYPE("Deklarált típus szerinti feloldás"),
    /** Class Hierarchy Analysis: a deklarált típus minden altípusára. */
    CHA("Class Hierarchy Analysis");

    /** Emberi olvasásra szánt leírás. */
    private final String description;

    /**
     * Konstans létrehozása leírással.
     */
    Strategy(String description) {
        this.description = description;
    }

    /**
     * Emberi olvasásra szánt leírás (például naplózáshoz).
     */
    public String description() {
        return description;
    }
}
