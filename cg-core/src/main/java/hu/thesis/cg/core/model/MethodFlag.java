package hu.thesis.cg.core.model;

import hu.thesis.cg.core.signature.MethodSignature;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A metódus nyelvi jellegét leíró jelzők.
 */
public enum MethodFlag {
    /** Konstruktor. Névből. */
    CONSTRUCTOR,
    /** Statikus inicializáló. Névből. */
    STATIC_INIT,
    /** {@code static} módosítójú metódus. Eszköztől. */
    STATIC,
    /** Absztrakt vagy interfészmetódus törzs nélkül. Eszköztől. */
    ABSTRACT,
    /** A fordító által generált metódus (például bridge, enum. Eszköztől. */
    SYNTHETIC,
    /** Lambdatörzs vagy a lambdát megvalósító generált osztály metódusa. Névből. */
    LAMBDA,
    /** Anonim osztály metódusa (osztálynév végén). Névből. */
    ANONYMOUS_CLASS,
    /** Beágyazott (belső, statikus beágyazott, anonim vagy lokális) osztály metódusa. Névből. */
    INNER_CLASS,
    /** Nem az elemzett alkalmazás része (JDK vagy külső könyvtár). Eszköztől. */
    LIBRARY,
    /** Az eszköz nem találta meg a definícióját (hiányzó classpath-elem). Eszköztől. */
    PHANTOM;

    /** Anonim osztály: a név utolsó $ utáni része csak számjegy. */
    private static final Pattern ANONYMOUS = Pattern.compile(".*\\$\\d+$");

    /**
     * A csak a metódus nevéből és osztályából eldönthető jelzők.
     */
    public static Set<MethodFlag> fromNames(MethodSignature s) {
        Set<MethodFlag> flags = EnumSet.noneOf(MethodFlag.class);
        if (s.isConstructor()) {
            flags.add(CONSTRUCTOR);
        }
        if (s.isStaticInitializer()) {
            flags.add(STATIC_INIT);
        }
        String cls = s.className();
        if (s.methodName().startsWith("lambda$") || cls.contains("$$Lambda") || cls.contains("$lambda_")) {
            flags.add(LAMBDA);
        }
        if (cls.contains("$")) {
            flags.add(INNER_CLASS);
            if (ANONYMOUS.matcher(cls).matches()) {
                flags.add(ANONYMOUS_CLASS);
            }
        }
        return flags;
    }
}
