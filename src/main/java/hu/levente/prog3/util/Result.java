package hu.levente.prog3.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Egy művelet kimenetelét reprezentáló típusbiztos, lezárt konténer, amely vagy sikeres eredményt ({@link Success}),
 * vagy hibát ({@link Failure}) tartalmaz.
 * Célja a kivételkezelés kiváltása olyan esetekben, ahol a hiba a normál működés része.
 *
 * @param <T> a sikeres művelet visszatérési típusa
 * @param <E> a hibaállapotot leíró objektum típusa
 * */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    /**
     * A művelet sikeres lefutását és annak eredményét reprezentáló record.
     * @param value a sikeres eredmény értéke
     * @param <E> a hiba típusa
     * @param <T> az eredmény típusa
     * */
    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            Objects.requireNonNull(value, "Az érték nem lehet null");
        }
    }

    /**
     * A művelet sikertelen lefutását és a keletkezett hibát reprezentáló record.
     * @param error a hibát leíró objektum
     * @param <E> a hiba típusa
     * @param <T> az eredmény típusa
     * */
    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            Objects.requireNonNull(error, "Nem lehet null");
        }
    }

    /**
     * Létrehoz egy sikeres eredményt tartalmazó {@link Result} példányt
     * @param value az eredmény értéke
     * @param <T> az eredmény típusa
     * @param <E> a hiba típusa
     * @return új {@link Success} példány
     * */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Létrehoz egy hibaállapotot tartalmazó {@link Result} példányt
     * @param error a hibát leiró objektum
     * @param <T> az eredmény típusa
     * @param <E> a hiba típusa
     * @return új {@link Failure} példány
     * */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Végrehajtja a megadott műveletet a tárolt értéken, ha az eredmény {@link Success}
     * Hiba esetén nem történik semmi sem.
     * @param consumer a sikeres értéket feldolgozó művelet
     * */
    default void ifSuccess(Consumer<? super T> consumer) {
        if (this instanceof Success<T, E>(T value))
            consumer.accept(value);
    }

    /**
     * Végrehajtja a megadott műveletet a hibaobjektumon, ha az eredmény {@link Failure}
     * Sikeres esetén nem történik semmi sem.
     * @param consumer a hibát feldolgozó művelet
     * */
    default void ifFailure(Consumer<? super E> consumer) {
        if (this instanceof Failure<T, E>(E error))
            consumer.accept(error);
    }
}
