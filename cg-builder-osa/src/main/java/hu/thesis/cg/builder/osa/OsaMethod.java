package hu.thesis.cg.builder.osa;

/**
 * Egy sor az OSA metódus szintű CSV-jéből; csak a gráfhoz szükséges oszlopok.
 */
record OsaMethod(String longName, String path, int line) {
}
