package hu.levente.prog3.exporter;

import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.DataType;

public enum SqlDialect {
    POSTGRESQL("PostgreSQL") {
        @Override
        public String mapColumn(Column column) {
            if (column.isPrimaryKey()) {
                if (column.type() == DataType.INT) return formatIdentifier(column.name()) + " SERIAL PRIMARY KEY";
                if (column.type() == DataType.BIGINT) return formatIdentifier(column.name()) + " BIGSERIAL PRIMARY KEY";
            }

            return super.mapColumn(column);
        }

        @Override
        protected String mapSpecificType(DataType dataType) {
            return switch (dataType) {
                case INT -> "INTEGER";
                case DECIMAL -> "NUMERIC(12,2)";
                case FLOAT -> "DOUBLE PRECISION";
                case BOOLEAN -> "BOOLEAN";
                case TIMESTAMP -> "TIMESTAMPTZ";
                default -> super.mapSpecificType(dataType);
            };
        }
    },
    MYSQL("MySQL") {
        @Override
        public String formatIdentifier(String identifier) {
            return "`" + identifier + "`";
        }

        @Override
        protected String primaryKeyConstraint() {
            return "AUTO_INCREMENT PRIMARY KEY";
        }

        @Override
        protected String mapSpecificType(DataType type) {
            return switch (type) {
                case INT -> "INT";
                case DECIMAL -> "DECIMAL(12,2)";
                case FLOAT -> "DOUBLE";
                case BOOLEAN -> "TINYINT(1)";
                case TIMESTAMP -> "DATETIME";
                default -> super.mapSpecificType(type);
            };
        }
    };

    private final String displayName;

    /**
     * Létrehoz egy új SQL dialektust a megadott néven.
     * */
    SqlDialect(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Előállítja egy oszlop teljes SQL definicióját megszorításokkal egyött.
     *
     * @param column a leképezendő adatbázisoszlop
     * @return az oszlop
     * */
    public String mapColumn(Column column) {
        String colName = formatIdentifier(column.name());
        String typeSql = formatIdentifier(column.type().name());
        String constraints = resolveConstraints(column);

        return constraints.isEmpty() ? colName + " " + typeSql :
                colName + " " + typeSql + " " + constraints;
    }

    /**
     * A szintaktikai szabályoknak megfelelően formázza a tábla és oszlopneveket.
     * */
    public String formatIdentifier(String identifier) {
        return identifier;
    }

    /**
     * Visszadja az elsődleges kulcs oszlopához tartozó megszorítást.
     *
     * @return az elsődleges kulcs SQL kulcsszava
     * */
    protected String primaryKeyConstraint() {
        return "PRIMARY KEY";
    }

    /**
     * Leképzi a belső {@link DataType} adattípust a dialektusnak megfelelő SQL típusra.
     *
     * @param dataType a belső adattípus
     * @return az SQL-ben használandó típus
     * */
    protected String mapSpecificType(DataType dataType) {
        return switch (dataType) {
            case BIGINT -> "BIGINT";
            case VARCHAR -> "VARCHAR(255)";
            case DATE -> "DATE";
            default -> dataType.name();
        };
    }

    /**
     * Összeállítja az oszlopra vonatkozó integritás megszorításokat.
     *
     * @param column viszgált oszlop
     * */
    private String resolveConstraints(Column column) {
        if (column.isPrimaryKey()) return primaryKeyConstraint();
        return column.isNullable() ? "" : "NOT NULL";
    }
}
