package hu.thesis.cg.core.signature;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A metódusazonosító-stílusok tesztjei. A párosítás (november) erre épül, ezért a
 * szélsőséges eseteket (belső osztály, többdimenziós tömb, konstruktor, alapértelmezett csomag)
 * külön ellenőrizzük.
 */
class SignatureStyleTest {

    /** Közös minta: belső osztály, primitív, tömb és kétdimenziós tömb paraméter, referencia visszatérés. */
    private final MethodSignature sig = new MethodSignature(
            "pkg.Outer$Inner", "run", List.of("int", "java.lang.String[]", "long[][]"), "java.util.List");

    /**
     * A JVM-stílus pontosan az OSA LongName alakját adja.
     */
    @Test
    void jvmFormat() {
        assertEquals("pkg.Outer$Inner.run(I[Ljava/lang/String;[[J)Ljava/util/List;", SignatureStyle.JVM.format(sig));
    }

    /**
     * A Java-stílus olvasható alakja.
     */
    @Test
    void javaFormat() {
        assertEquals("pkg.Outer$Inner.run(int,java.lang.String[],long[][]):java.util.List",
                SignatureStyle.JAVA.format(sig));
    }

    /**
     * Minden stílusra: parse(format(s)) == s (visszaalakíthatóság).
     */
    @Test
    void roundTripBothStyles() {
        for (SignatureStyle style : SignatureStyle.values()) {
            assertEquals(sig, style.parse(style.format(sig)), style.name());
        }
    }

    /** Konstruktor paraméter nélkül, mindkét irányban. */
    @Test
    void constructorAndNoParams() {
        MethodSignature ctor = new MethodSignature("a.B", "<init>", List.of(), "void");
        assertEquals("a.B.<init>()V", SignatureStyle.JVM.format(ctor));
        assertEquals(ctor, SignatureStyle.JVM.parse("a.B.<init>()V"));
    }

    /**
     * Alapértelmezett csomag: az osztálynévben nincs pont, a csomagnév üres. Ha az azonosítóban
     * egyáltalán nincs osztály.metódus pont, az hiba.
     */
    @Test
    void defaultPackage() {
        assertThrows(IllegalArgumentException.class, () -> SignatureStyle.JVM.parse("noDot()V"));
        MethodSignature s = SignatureStyle.JVM.parse("Main.main([Ljava/lang/String;)V");
        assertEquals("Main", s.className());
        assertEquals("", s.packageName());
    }
}
