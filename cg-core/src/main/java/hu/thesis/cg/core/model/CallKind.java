package hu.thesis.cg.core.model;

/**
 * A hívás fajtája, a bájtkód hívóutasításaihoz igazítva.
 */
public enum CallKind {
    /** statikus metódus, a cél egyértelmű. */
    STATIC,
    /** példánymetódus osztálytípuson, polimorf lehet. */
    VIRTUAL,
    /** példánymetódus interfésztípuson, polimorf lehet. */
    INTERFACE,
    /** konstruktor, privát metódus vagy super.m(); a cél egyértelmű. */
    SPECIAL,
    /** lambda, metódusreferencia, string-összefűzés (Java 9+). */
    DYNAMIC,
    /** Implicit statikus inicializáló hívás (az osztály első használatakor). */
    STATIC_INIT,
    /** Egyéb, az eszköz által modellezett implicit hívás (szál indítása, finalize, reflexió stb.). */
    OTHER,
    /** Az eszköz nem adja meg a hívás fajtáját (például az OSA élei). */
    UNKNOWN
}
