package hu.levente.prog3.model;

import java.util.Objects;

/**
 * Egy relációs adatbázistábla egyetlen oszlopát reprezentáló record.
 *
 * @param name az oszlop neve, nem lehet {@code NULL} vagy üres
 * @param type az oszlop adattípusa, nem lehet {@code NULL}
 * @param isPrimaryKey igaz, ha az oszlop elsődleges kulcs
 * @param isNullable igaz, ha az oszlop felvehet {@code NULL} értéket
 * */
public record Column(String name, DataType type, boolean isPrimaryKey, boolean isNullable) {
    public Column{
        Objects.requireNonNull(name, "A mező neve nem lehet üres");
        Objects.requireNonNull(type, "A típus nem lehet üres");
        if (name.isBlank()) throw new IllegalArgumentException("A mező neve nem lehet üres");
    }
}
