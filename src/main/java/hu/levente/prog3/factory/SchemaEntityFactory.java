package hu.levente.prog3.factory;

import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.DataType;
import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Table;

import java.awt.*;
import java.util.List;

public interface SchemaEntityFactory {
    /**
     * Létrehoz egy új oszlopot reprezentáló entitást.
     *
     * @param name az oszlop neve, nem lehet {@code NULL}
     * @param type az oszlop adattípusa
     * @param isPk igaz, ha az oszlop elsődleges kulcs
     * @param isNullable igaz, ha az oszlop felvehet {@code NULL} értéket
     * @return az újonnan létrehozott példány
     * */
    Column createColumn(String name, DataType type, boolean isPk, boolean isNullable);

    /**
     * Létrehoz egy új adatbázistáblát reprezentáló entistást a megadott oszlopokkal.
     *
     * @param name a tábla neve, nem lehet {@code NULL}
     * @param columns a táblához tartozó oszlopok listája
     * @param pos a tábla koordinátája
     * @return az újonnan létrehozott példány
     * */
    Table createTable(String name, List<Column> columns, Point pos);

    /**
     * Létrehoz egy új idegenkulcs kapcsolatot két tábla között.
     *
     * @param sourceTable a hivatkozó tábla neve
     * @param sourceCol a hivatkozó tábla idegenkulcs oszlopának neve
     * @param targetTable a hivatkozott tábla célosulopának neve
     * @param targetCol a hivatkozott tábla céloszlopának neve
     * @return az újonnan létrehozott példány
     * */
    Relationship createRelationship(String sourceTable, String sourceCol, String targetTable, String targetCol);
}
