package hu.levente.prog3.parser;

import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SemanticValidator {
    /**
     * Szemantikai ellenőrzést futtat az egész adatbázis scheman, majd az észlelt hibákat pedig rögzíti a {@link ParseDiagnostics}
     *
     * @param schema validálandó adatbázis schema
     * @param diagnostics a hibákat gyűjtő objektum
     * */
    public void validate(Schema schema, ParseDiagnostics diagnostics) {
        Map<String, Map<String, Column>> tableMap = schema.tables().stream()
                .collect(Collectors.toMap(
                        table -> table.name().toLowerCase(),
                        table -> table.columns().stream()
                                .collect(Collectors.toMap(
                                        column -> column.name().toLowerCase(),
                                        column -> column,
                                        (existing, replacement) -> existing,
                                        LinkedHashMap::new
                                )),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        schema.relationships().forEach(relationship ->
                validateRelationship(relationship, tableMap, diagnostics));
    }

    /**
     * Egyetlen idegenkulcs kapcsolat helyességét ellenőrzi
     *
     * @param relationship a vizsgált kapcsolat
     * @param tableMap a schemban szereplő táblák és oszlopok
     * @param diagnostics hibákat gyűjtő objektum
     * */
    private void validateRelationship(Relationship relationship, Map<String, Map<String, Column>> tableMap, ParseDiagnostics diagnostics) {
        Map<String, Column> sourceCols = tableMap.get(relationship.sourceTable().toLowerCase());
        Map<String, Column> targetCols = tableMap.get(relationship.targetTable().toLowerCase());
        if (sourceCols == null || targetCols == null) {
            diagnostics.add((sourceCols == null) ?
                    "A hivatkozott forrástábla nem létezik: " + relationship.sourceTable() :
                    "A hivatkozott céltábla nem létezik: ", 0, 0);
            return;
        }

        Column sourceCol = sourceCols.get(relationship.sourceColumn().toLowerCase());
        Column targetCol = targetCols.get(relationship.targetColumn().toLowerCase());
        if (sourceCol == null || targetCol == null) {
            diagnostics.add((sourceCol == null) ?
                    "A(z) " + relationship.sourceTable() + " táblában nincs " + relationship.sourceColumn() + " mező" :
                    "A(z) " + relationship.targetTable() + " táblában nincs " + relationship.targetColumn() + " mező", 0, 0);
            return;
        }

        if (sourceCol.type() != targetCol.type()) {
            diagnostics.add(String.format(
                    "Típuseltérés az idegen kulcsnál: %s.%s (%s) -> %s.%s (%s)",
                    relationship.sourceTable(), relationship.sourceColumn(), sourceCol.type(),
                    relationship.targetTable(), relationship.targetColumn(), targetCol.type()
            ), 0, 0);
        }
    }
}
