package hu.levente.prog3.factory;

import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.DataType;
import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Table;

import java.awt.*;
import java.util.List;

public class RelationalEntityFactory implements SchemaEntityFactory {
    @Override
    public Column createColumn(String name, DataType type, boolean isPk, boolean isNullable) {
        return new Column(name, type, isPk, isNullable);
    }

    @Override
    public Table createTable(String name, List<Column> columns, Point pos) {
        Point point = (pos != null) ? new Point(pos) : new Point(0, 0);
        return new Table(name, columns, point);
    }

    @Override
    public Relationship createRelationship(String sourceTable, String sourceCol, String targetTable, String targetCol) {
        return new Relationship(sourceTable.trim(), sourceCol.trim(), targetTable.trim(), targetCol.trim());
    }
}
