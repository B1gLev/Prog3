package hu.levente.prog3.exporter;

import java.util.Optional;

/**
 * Egy külső forrásformátumból történő adatbeolvasásért felelős interface.
 *
 * @param <R> a beolvasandó nyers forrás típusa
 * @param <T> a visszaállított célobjektum típusa
 * */
@FunctionalInterface
public interface Importer<R ,T> {
    /**
     * Beolvassa és feldolgozza a megadott nyers forrást, majd visszaállítja belőle a célobjektumot.
     *
     * @param source a feldlgozandó nyers bemeneti forrás
     * @return a sikeresen visszaállított objektum
     * */
    Optional<T> importFrom(R source);
}
