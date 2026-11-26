package hu.levente.prog3.parser;

import java.util.function.Predicate;

/**
 * Cursor interfac a szekvenciális adatokhoz
 * @param <T> a cursor által bejárt elemek típusa
 * */
public interface Cursor<T> {
    /**
     * Visszadja az akutális pozícón álló elemet anélkül, hogy előrébb léptetné akurzort.
     * @return az aktuális elem
     * */
    T peek();

    /**
     * Visszadja az aktuális elemet és egy pozicióval előrébb lépteti a kurzort.
     * @return az előrelépés előtti aktuális elem
     * */
    T advance();

    /**
     * Visszadja a kurzor aktuális pozíciójnak megelőző elemét.
     * @return az előzőleg feldolgozott elem
     * */
    T previous();

    /**
     * Ellenőrzi, hogy a kurzor elérte-e az adatfolyam végét.
     * @return igaz, ha nincs több feldolgozandó elem, minden más esetben hamis
     * */
    boolean isAtEnd();

    /**
     * Ellenőrzi, hogy az aktuális elem megfelel-e a megadott feltételnek anélkül, hogy a kurzor poziciója változna.
     * @param predicate a vizsgálandó feltétel
     * @return igaz, ha az illeszkedés sikeres volt és a kurzor előrelépett, különben hamis
     * */
    default boolean check(Predicate<T> predicate) {
        if(isAtEnd()) return false;
        return predicate.test(peek());
    }
}
