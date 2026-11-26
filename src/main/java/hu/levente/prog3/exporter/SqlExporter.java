package hu.levente.prog3.exporter;

import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Schema;
import hu.levente.prog3.model.Table;

import java.time.Instant;
import java.util.stream.Collectors;

public class SqlExporter implements Exporter<Schema, String> {
    private final SqlDialect sqlDialect;

    public SqlExporter(SqlDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    /**
     * Legenerálja a teljes SQL scriptet a megadott adatbázis schemanak megfelelően.
     *
     * A kimenet tartalmazza a generálás metadatait megjegyzésként, az összes tábla utasítását, valamint
     * a referenciákhoz tartozó parancsokat.
     *
     * @param schema az exportálandó adatbázis schema
     * @return a végrehajtható SQL script karakterláncban
     * */
    @Override
    public String export(Schema schema) {
        var builder = new StringBuilder();
        builder.append("-- Dialektus: ").append(sqlDialect.getDisplayName()).append("\n");
        builder.append("-- Generálva: ").append(Instant.now()).append("\n");

        for (Table table : schema.tables()) {
            String tableName = (sqlDialect == SqlDialect.MYSQL) ? "`" + table.name() + "`" : table.name();
            builder.append("CREATE TABLE ").append(tableName).append(" \n");

            var colDefs = table.columns().stream()
                    .map(c -> "      " + sqlDialect.mapColumn(c))
                    .collect(Collectors.joining(",\n"));

            builder.append(colDefs);
            builder.append(sqlDialect == SqlDialect.MYSQL ? "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n" : "\n);\n\n");
        }

        for (Relationship relationship : schema.relationships()) {
            if (sqlDialect == SqlDialect.MYSQL) {
                builder.append(String.format(
                        "ALTER TABLE `%s` ADD CONSTRAINT `FK_%s_%s` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ON DELETE CASCADE;\n",
                        relationship.sourceTable(), relationship.sourceTable(), relationship.sourceColumn(),
                        relationship.sourceColumn(), relationship.targetTable(), relationship.targetColumn()
                ));
            } else {
                builder.append(String.format(
                        "ALTER TABLE %s ADD CONSTRAINT FK_%s_%s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE;\n",
                        relationship.sourceTable(), relationship.sourceTable(), relationship.sourceColumn(),
                        relationship.sourceColumn(), relationship.targetTable(), relationship.targetColumn()
                ));
            }
        }
        return builder.toString();
    }
}
