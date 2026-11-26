package hu.levente.prog3.model;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Egy relációs adatbázistáblát és annak a diagrammon elfoglalt pozicióját reprezentáló record.
 *
 * @param name a tábla neve, nem lehet {@code NULL} vagy üres
 * @param columns a táblához tartozó osulopok listája
 * @param pos a tábla koordinátája, {@code NULL} esetén 0.0 koordinátát kapja meg alapértelmezetten.
 * */
public record Table(String name, List<Column> columns, Point pos) {

    public Table {
        Objects.requireNonNull(name, "A táblanéve nem lehet null");
        Objects.requireNonNull(columns, "Az oszlopok nem lehetnek null");
        if (name.isBlank()) throw new IllegalArgumentException("A táblanév nem lehet üres");
        columns = Collections.unmodifiableList(columns);
        pos = (pos != null) ? new Point(pos) : new Point(0, 0);
    }
}
