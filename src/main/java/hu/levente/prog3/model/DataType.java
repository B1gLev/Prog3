package hu.levente.prog3.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * A DSL leiró nyelvben támogatott realációs adatbázis adattípusok.
 * Minden adattípus rendelkezik egy szemantikai kategóriával és opcionális aliasokkal, amelyek
 * alapján a parser felismeri a forráskódban megadott típusokat.
 * */
public enum DataType {
    INT(TypeCategory.NUMERIC, "int", "integer"),
    BIGINT(TypeCategory.NUMERIC, "bigint"),
    DECIMAL(TypeCategory.NUMERIC, "deicaml", "numeric", "number"),
    FLOAT(TypeCategory.NUMERIC, "float", "double"),
    VARCHAR(TypeCategory.TEXT, "varchar", "string"),
    BOOLEAN(TypeCategory.LOGICAL, "bool", "boolean"),
    DATE(TypeCategory.TEMPORAL, "date"),
    TIMESTAMP(TypeCategory.TEMPORAL, "timestamp", "datetime");

    public enum TypeCategory {
        NUMERIC,
        TEXT,
        LOGICAL,
        TEMPORAL
    }

    private final TypeCategory typeCategory;
    private final String[] aliases;

    DataType(TypeCategory typeCategory, String... aliases) {
        this.typeCategory = typeCategory;
        this.aliases = aliases;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }

    /**
     * Típusmegnevezést csinál a megfelelő példányra az enum nevek és a definiált aliasok alapján.
     *
     * @param type a vizsgálandó típus
     * */
    public static Optional<DataType> fromString(String type) {
        if (type == null || type.isBlank()) return Optional.empty();

        String normalized = type.trim().toLowerCase();
        return Arrays.stream(values()).filter(
                dataType ->
                        dataType.name().equalsIgnoreCase(normalized) ||
                        Arrays.stream(dataType.aliases).anyMatch(a -> a.equalsIgnoreCase(normalized)))
                .findFirst();
    }

}
