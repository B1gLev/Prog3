import hu.levente.prog3.factory.RelationalEntityFactory;
import hu.levente.prog3.model.*;
import hu.levente.prog3.parser.SchemaParseException;
import hu.levente.prog3.parser.SchemaParser;
import hu.levente.prog3.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ellenőrzi az üres és kommentelt forráskódokot, az érvényes tábla és relációk feldolgozását,
 * valamint a hibás szintaxist és a duplikációkat.
 * */
public class SchemaParserTest {
    private SchemaParser schemaParser;

    @BeforeEach
    void setUp() {
        schemaParser = new SchemaParser(new RelationalEntityFactory());
    }

    @Test
    void testEmptyAndCommentOnlyInputs() {
        String[] emptyInputs = {
                null,
                "",
                "// Csak egy egysoros komment\n# És egy másik komment"
        };

        for (String input : emptyInputs) {
            Result<Schema, SchemaParseException> res = schemaParser.parse(input);
            assertInstanceOf(Result.Success.class, res, "Üres bemenetnél Success-nek kell visszatérnie");
            Schema schema = ((Result.Success<Schema, SchemaParseException>) res).value();
            assertTrue(schema.tables().isEmpty());
            assertTrue(schema.relationships().isEmpty());
        }
    }

    @Test
    void testValidSchema() {
        String dsl = """
                Table users {
                  id int [pk]
                  external_uuid varchar [not null]
                  bio varchar [null]
                  balance decimal
                  rating float
                  is_active boolean [not null]
                  birth_date date
                  created_at timestamp
                }

                Table orders {
                  id bigint [primary]
                  user_id int [not null]
                }

                Ref: orders.user_id > users.id
                """;

        Result<Schema, SchemaParseException> res = schemaParser.parse(dsl);
        assertInstanceOf(Result.Success.class, res, () -> "Váratlan elemzési hiba: " + res);

        Schema schema = ((Result.Success<Schema, SchemaParseException>) res).value();
        assertEquals(2, schema.tables().size());
        assertEquals(1, schema.relationships().size());

        Table users = schema.tables().getFirst();
        assertEquals("users", users.name());
        assertEquals(8, users.columns().size());

        Column id = users.columns().getFirst();
        assertEquals("id", id.name());
        assertEquals(DataType.INT, id.type());
        assertTrue(id.isPrimaryKey());
        assertFalse(id.isNullable());

        Column uuidCol = users.columns().get(1);
        assertEquals(DataType.VARCHAR, uuidCol.type());
        assertFalse(uuidCol.isPrimaryKey());
        assertFalse(uuidCol.isNullable());

        Column bio = users.columns().get(2);
        assertEquals(DataType.VARCHAR, bio.type());
        assertTrue(bio.isNullable());

        assertEquals(DataType.DECIMAL, users.columns().get(3).type());
        assertEquals(DataType.FLOAT, users.columns().get(4).type());
        assertEquals(DataType.BOOLEAN, users.columns().get(5).type());
        assertEquals(DataType.DATE, users.columns().get(6).type());
        assertEquals(DataType.TIMESTAMP, users.columns().get(7).type());

        Table orders = schema.tables().get(1);
        assertEquals("orders", orders.name());
        Column orderId = orders.columns().getFirst();
        assertEquals(DataType.BIGINT, orderId.type());
        assertTrue(orderId.isPrimaryKey());
        assertFalse(orderId.isNullable());

        assertEquals(new Relationship("orders", "user_id", "users", "id"), schema.relationships().getFirst());
    }

    @Test
    void testUnknowDataType() {
        String dsl = """
                Table users {
                  id int [pk]
                  age test
                }
                """;

        Result<Schema, SchemaParseException> res = schemaParser.parse(dsl);
        assertInstanceOf(Result.Failure.class, res);

        SchemaParseException ex = ((Result.Failure<Schema, SchemaParseException>) res).error();
        assertEquals(3, ex.getLineNumber());
        assertEquals(7, ex.getColumnNumber());
        assertTrue(ex.getMessage().contains("Ismeretlen adattípus"));
        assertTrue(ex.getMessage().contains("test"));
    }

    @Test
    void testSyntaxError() {
        Result<Schema, SchemaParseException> res = schemaParser.parse("Table { id int }");
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("Hiányzó táblanév"));

        res = schemaParser.parse("Table users id int }");
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("'{'"));

        res = schemaParser.parse("""
                Table users {
                  id int [pk]
                  username varchar
                """);
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("Lezáratlan tábladefiníció"));
    }

    @Test
    void testColumnModifierError() {
        Result<Schema, SchemaParseException> res = schemaParser.parse("""
                Table users {
                  email varchar [not]
                }
                """);
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("'not'"));

        res = schemaParser.parse("""
                Table users {
                  email varchar [unique_something]
                }
                """);
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("Ismeretlen módosító"));
    }

    @Test
    void testDuplicateTableAndColumnNames() {
        String duplicateTables = """
                Table Users { id int [pk] }
                Table users { id int [pk] }
                """;

        Result<Schema, SchemaParseException> res = schemaParser.parse(duplicateTables);
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("már korábban deklarálva"));

        String duplicateColumns = """
                Table users {
                  id int [pk]
                  ID int
                }
                """;
        res = schemaParser.parse(duplicateColumns);
        assertInstanceOf(Result.Failure.class, res);
        assertTrue(((Result.Failure<Schema, SchemaParseException>) res).error().getMessage().contains("Duplikált mezőnév"));
    }
}
