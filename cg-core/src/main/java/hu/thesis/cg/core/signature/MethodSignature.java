package hu.thesis.cg.core.signature;

import java.util.List;
import java.util.Objects;

/**
 * Egy metódus eszközfüggetlen, szerkezetes leírása.
 */
public record MethodSignature(String className, String methodName, List<String> parameterTypes, String returnType) {

    /** A konstruktorok bájtkód szerinti neve. */
    public static final String CONSTRUCTOR_NAME = "<init>";

    /** A statikus inicializáló blokk, bájtkód szerinti neve. */
    public static final String STATIC_INIT_NAME = "<clinit>";

    /**
     * Kompakt konstruktor: ellenőrzi a kötelező mezőket, és a paraméterlistát módosíthatatlan
     * másolatra cseréli.
     */
    public MethodSignature {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(returnType, "returnType");
        parameterTypes = List.copyOf(parameterTypes);
    }

    /**
     * A csomag neve.
     */
    public String packageName() {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? "" : className.substring(0, dot);
    }

    /**
     * Az osztály neve csomag nélkül.
     */
    public String simpleClassName() {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Konstruktor-e a metódus.
     */
    public boolean isConstructor() {
        return CONSTRUCTOR_NAME.equals(methodName);
    }

    /**
     * Statikus inicializáló-e a metódus.
     */
    public boolean isStaticInitializer() {
        return STATIC_INIT_NAME.equals(methodName);
    }

    /**
     * Szöveges alak naplózáshoz és hibakereséshez, a kanonikus stílusban.
     */
    @Override
    public String toString() {
        return SignatureStyle.DEFAULT.format(this);
    }
}
