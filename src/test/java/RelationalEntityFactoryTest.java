import hu.levente.prog3.factory.RelationalEntityFactory;
import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.DataType;
import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ellenőrzi az entitások ({@link Column}, {@link Table}, {@link Relationship}) példányosítását, az alapértelmezett érékeket,
 * valamint az érvénytelen bemeneteket.
 * */
public class RelationalEntityFactoryTest {
    private RelationalEntityFactory entityFactory;

    @BeforeEach
    void setUp() {
        entityFactory = new RelationalEntityFactory();
    }

    @Test
    void testCreateColumnValid() {
        Column pkCol = entityFactory.createColumn("id", DataType.BIGINT, true, false);
        assertEquals("id", pkCol.name());
        assertEquals(DataType.BIGINT, pkCol.type());
        assertTrue(pkCol.isPrimaryKey());
        assertFalse(pkCol.isNullable());

        Column nullableCol = entityFactory.createColumn("test", DataType.VARCHAR, false, true);
        assertEquals("test", nullableCol.name());
        assertEquals(DataType.VARCHAR, nullableCol.type());
        assertFalse(nullableCol.isPrimaryKey());
        assertTrue(nullableCol.isNullable());
    }

    @Test
    void testCreateColumnInvalid() {
        assertThrows(NullPointerException.class, () -> entityFactory.createColumn(null, DataType.INT, false, true));
        assertThrows(IllegalArgumentException.class, () -> entityFactory.createColumn("", DataType.INT, false, true));
        assertThrows(NullPointerException.class, () -> entityFactory.createColumn("test", null, false, true));

    }

    @Test
    void testCreateTableValid() {
        List<Column> cols = List.of(
                entityFactory.createColumn("id", DataType.BIGINT, true, false),
                entityFactory.createColumn("test", DataType.VARCHAR, false, false)
        );

        Point pos = new Point(10, 10);
        Table table = entityFactory.createTable("test", cols, pos);
        assertEquals("test", table.name());
        assertEquals(2, table.columns().size());
        assertEquals(pos, table.pos());
    }

    @Test
    void testCreateTableWithNullPos() {
        Table table = entityFactory.createTable("test", List.of(), null);

        assertNotNull(table.pos());
        assertEquals(new Point(0, 0), table.pos());
    }

    @Test
    void testCreateTableInvalid() {
        assertThrows(NullPointerException.class, () -> entityFactory.createTable(null, List.of(), new Point(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> entityFactory.createTable("   ", List.of(), new Point(0, 0)));
        assertThrows(NullPointerException.class, () -> entityFactory.createTable("users", null, new Point(0, 0)));
    }

    @Test
    void testCreateRelationshipTrimsEndpoints() {
        Relationship rel = entityFactory.createRelationship(
                "  order_items \t",
                "\n product_id ",
                " products ",
                "  id  "
        );

        assertEquals("order_items", rel.sourceTable());
        assertEquals("product_id", rel.sourceColumn());
        assertEquals("products", rel.targetTable());
        assertEquals("id", rel.targetColumn());
    }

    @Test
    void testCreateRelationshipNullEndpoints() {
        assertThrows(NullPointerException.class,
                () -> entityFactory.createRelationship(null, "user_id", "users", "id"));
        assertThrows(NullPointerException.class,
                () -> entityFactory.createRelationship("orders", null, "users", "id"));
        assertThrows(NullPointerException.class,
                () -> entityFactory.createRelationship("orders", "user_id", null, "id"));
        assertThrows(NullPointerException.class,
                () -> entityFactory.createRelationship("orders", "user_id", "users", null));
    }
}
