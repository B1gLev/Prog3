package hu.levente.prog3.exporter;

/**
 * Lehetővé teszi különböző exportálási megoldások egységes kezelését és lambada kifejezésekkel történő megvalósítását.
 *
 * @param <T> az exportálandó forrásadat típusa
 * @param <R> a generált kimenet típusa
 * */
@FunctionalInterface
public interface Exporter<T ,R> {
    /**
     * Átalakítja a megadott bementi objektumot a célformátumnak megfelelő kimenetté.
     *
     * @param t az exportálandó forrásadat
     * @return a generált célformátumú kimenet
     * @throws Exception ha az exportálási folyamat során bármilyen hiba lép fel
     * */
    R export(T t) throws Exception;
}
