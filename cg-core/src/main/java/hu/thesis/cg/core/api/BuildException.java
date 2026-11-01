package hu.thesis.cg.core.api;

public class BuildException extends Exception {

    /**
     * Kivétel csak üzenettel.
     */
    public BuildException(String message) {
        super(message);
    }

    /**
     * Kivétel üzenettel és az eredeti hiba.
     */
    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
