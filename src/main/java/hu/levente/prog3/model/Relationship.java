package hu.levente.prog3.model;

import java.util.Objects;

/**
 * Két relációs adatbázistábla közötti idegenkulcs kapcsolatoto reprezentáló record.
 *
 * @param sourceTable a hivatkozó tábla neve, nem lehet {@code NULL}
 * @param sourceColumn a hivatkozó tábla idegenkulcs oszlopának neve, nem lehet {@code NULL}
 * @param targetTable a hivatkozott tábla neve, nem lehet {@code NULL}
 * @param targetColumn a hivatkozott tábla céloszlopának neve, nem lehet {@code NULL}
 * */
public record Relationship(String sourceTable, String sourceColumn, String targetTable, String targetColumn) {

    public Relationship {
        Objects.requireNonNull(sourceTable, "A forrsátábla nem lehet null");
        Objects.requireNonNull(sourceColumn, "A forrásoszlopok nem lehetnek null");
        Objects.requireNonNull(targetTable, "A céltáblac nem lehet null");
        Objects.requireNonNull(targetColumn, "A céloszlop nem lehet null");
    }
}
