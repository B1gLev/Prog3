package hu.levente.prog3.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A relációs adatbázis schemat reprezentáló record, amely összefogja a schema tábláit és az azok közötti kapcsolatokat.
 *
 * @param tables a schemahoz tartozó táblák
 * @param relationships a táblák közötti idegenkulcs kapcsolatok listája
 * */
public record Schema(List<Table> tables, List<Relationship> relationships) {

    public Schema {
        Objects.requireNonNull(tables, "A táblák nem lehetnek null");
        Objects.requireNonNull(relationships, "A kapcsolatok nem lehetnek null");
        tables = Collections.unmodifiableList(tables);
        relationships = Collections.unmodifiableList(relationships);
    }

    public static Schema empty() {
        return new Schema(List.of(), List.of());
    }
}
