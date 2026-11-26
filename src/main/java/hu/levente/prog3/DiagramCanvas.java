package hu.levente.prog3;

import hu.levente.prog3.model.Column;
import hu.levente.prog3.model.Relationship;
import hu.levente.prog3.model.Schema;
import hu.levente.prog3.model.Table;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;

public class DiagramCanvas extends JPanel {
    private static final int TABLE_WIDTH = 225;
    private static final int ROW_HEIGHT = 28;
    private static final int HEADER_HEIGHT = 38;
    private static final int CORNER_RADIUS = 10;
    private static final int STUB_LENGTH = 18;

    private static final Color GRID_DOT_COLOR = new Color(60, 64, 75);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_HEADER_BG = new Color(245, 247, 250);
    private static final Color CARD_BORDER = new Color(210, 215, 224);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color PK_GOLD = new Color(200, 140, 20);
    private static final Color FK_CYAN = new Color(20, 140, 180);

    private Schema schema = Schema.empty();
    private final Map<String, Point> tableCoordinates = new HashMap<>();
    private final Set<String> foreignKeyColumns = new HashSet<>();

    private String draggedTableName = null;
    private String hoveredTableName = null;
    private Point dragOffset = new Point(0, 0);

    public DiagramCanvas() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Table hit = findTableAt(e.getPoint());
                if (hit != null) {
                    Point pos = tableCoordinates.get(hit.name());
                    draggedTableName = hit.name();
                    dragOffset = new Point(e.getX() - pos.x, e.getY() - pos.y);
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedTableName != null) {
                    draggedTableName = null;
                    setCursor(hoveredTableName != null
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                    revalidate();
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedTableName != null) {
                    int newX = Math.max(10, e.getX() - dragOffset.x);
                    int newY = Math.max(10, e.getY() - dragOffset.y);
                    tableCoordinates.put(draggedTableName, new Point(newX, newY));
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Table hit = findTableAt(e.getPoint());
                String newHovered = (hit != null) ? hit.name() : null;
                if (!Objects.equals(hoveredTableName, newHovered)) {
                    hoveredTableName = newHovered;
                    setCursor(hoveredTableName != null
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    /**
     * Frissíti a vásznon megjelenített adatbázis schemat, miközben megőrzi a felhasználó által
     * korábban kézzel elmozgatott táblák koordinátáit.
     *
     * @param newSchema az új hozzáadandó schema
     */
    public synchronized void updateSchema(Schema newSchema) {
        this.schema = newSchema;

        Set<String> names = new HashSet<>();
        newSchema.tables().forEach(table -> {
            names.add(table.name());
            tableCoordinates.putIfAbsent(table.name(), new Point(table.pos()));
        });
        tableCoordinates.keySet().retainAll(names);

        foreignKeyColumns.clear();
        for (Relationship rel : newSchema.relationships()) {
            foreignKeyColumns.add(rel.sourceTable().toLowerCase() + "." + rel.sourceColumn().toLowerCase());
        }

        revalidate();
        repaint();
    }

    /**
     * Kiszámolja a vászonnak a méretét az elhelyezett táblák pozíciója alapján.
     * */
    @Override
    public Dimension getPreferredSize() {
        int maxX = 900;
        int maxY = 700;
        for (Table table : schema.tables()) {
            Point p = tableCoordinates.getOrDefault(table.name(), table.pos());
            int h = getTableHeight(table);
            maxX = Math.max(maxX, p.x + TABLE_WIDTH + 80);
            maxY = Math.max(maxY, p.y + h + 80);
        }
        return new Dimension(maxX, maxY);
    }

    /**
     * Megkeresi a megadott táblát
     *
     * @param pt kurzor vagy kattintás koordinátáka
     * @return a megtalált tábla
     * */
    private Table findTableAt(Point pt) {
        List<Table> tables = schema.tables();
        for (int i = tables.size() - 1; i >= 0; i--) {
            Table table = tables.get(i);
            Point pos = tableCoordinates.get(table.name());
            if (pos != null) {
                Rectangle bounds = new Rectangle(pos.x, pos.y, TABLE_WIDTH, getTableHeight(table));
                if (bounds.contains(pt)) return table;
            }
        }
        return null;
    }

    /**
     * Kiszámítja egy tábla magasságát a fejléc és az oszlopok száma alapján
     * */
    private int getTableHeight(Table table) {
        return HEADER_HEIGHT + (table.columns().size() * ROW_HEIGHT) + 10;
    }

    /**
     * Kirajzolja a teljes diagrammot, a relációs vonalakat, a pöttyösrácsot, táblákat.
     * */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Háttér pöttyrácsos
        drawGrid(g2);

        String activeTable = draggedTableName != null ? draggedTableName : hoveredTableName;

        List<Relationship> normalRels = new ArrayList<>();
        List<Relationship> highlightedRels = new ArrayList<>();

        for (Relationship rel : schema.relationships()) {
            boolean isHighlighted = activeTable != null &&
                    (rel.sourceTable().equalsIgnoreCase(activeTable) || rel.targetTable().equalsIgnoreCase(activeTable));
            if (isHighlighted) highlightedRels.add(rel);
            else normalRels.add(rel);
        }

        for (Relationship rel : normalRels) drawRelationship(g2, rel, false);
        for (Relationship rel : highlightedRels) drawRelationship(g2, rel, true);

        Table draggedTable = null;
        for (Table table : schema.tables()) {
            if (table.name().equals(draggedTableName)) {
                draggedTable = table;
                continue;
            }
            Point p = tableCoordinates.getOrDefault(table.name(), table.pos());
            boolean isHovered = table.name().equals(hoveredTableName);
            drawTable(g2, table, p.x, p.y, isHovered);
        }

        if (draggedTable != null) {
            Point p = tableCoordinates.getOrDefault(draggedTable.name(), draggedTable.pos());
            drawTable(g2, draggedTable, p.x, p.y, true);
        }
    }

    /**
     * Kirjazolja a hátteret alkotó pöttyösrácsot.
     * */
    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_DOT_COLOR);
        int gridSize = 20;
        for (int x = 10; x < getWidth(); x += gridSize) {
            for (int y = 10; y < getHeight(); y += gridSize) {
                g2.fillOval(x, y, 2, 2);
            }
        }
    }

    /**
     * Kirajzolja a két tábla közötti idegenkulcs kapcsolatot.
     *
     * @param rel kirajzolandó kapcsolat
     * @param highlighted igaz, ha kapcsolat valamelyik végpontja éppen kiemelt állapotban van
     * */
    private void drawRelationship(Graphics2D g2, Relationship rel, boolean highlighted) {
        Table srcTable = findTable(rel.sourceTable());
        Table tgtTable = findTable(rel.targetTable());
        if (srcTable == null || tgtTable == null) return;

        Point srcPos = tableCoordinates.get(srcTable.name());
        Point tgtPos = tableCoordinates.get(tgtTable.name());
        if (srcPos == null || tgtPos == null) return;

        int y1 = getColumnCenterY(srcTable, srcPos, rel.sourceColumn());
        int y2 = getColumnCenterY(tgtTable, tgtPos, rel.targetColumn());

        int srcCenterX = srcPos.x + TABLE_WIDTH / 2;
        int tgtCenterX = tgtPos.x + TABLE_WIDTH / 2;

        int x1, x2;
        int dir1, dir2;

        if (srcCenterX < tgtCenterX) {
            x1 = srcPos.x + TABLE_WIDTH;
            dir1 = 1;
            x2 = tgtPos.x;
            dir2 = -1;
        } else {
            x1 = srcPos.x;
            dir1 = -1;
            x2 = tgtPos.x + TABLE_WIDTH;
            dir2 = 1;
        }

        g2.setColor(highlighted ? ACCENT_BLUE : new Color(110, 116, 132));
        g2.setStroke(new BasicStroke(highlighted ? 2.2f : 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D path = new Path2D.Float();
        path.moveTo(x1, y1);

        int p1X = x1 + (dir1 * STUB_LENGTH);
        int p2X = x2 + (dir2 * STUB_LENGTH);
        path.lineTo(p1X, y1);

        boolean canDoSimpleS = (dir1 == 1 && p1X < p2X) || (dir1 == -1 && p1X > p2X);

        if (canDoSimpleS) {
            int midX = (p1X + p2X) / 2;
            int r = Math.min(12, Math.abs(p2X - p1X) / 2);
            int rY = Math.min(r, Math.abs(y2 - y1) / 2);

            if (Math.abs(y1 - y2) < 4) {
                path.lineTo(x2, y2);
            } else {
                int signY = y2 > y1 ? 1 : -1;
                path.lineTo(midX - (dir1 * r), y1);
                path.quadTo(midX, y1, midX, y1 + (signY * rY));
                path.lineTo(midX, y2 - (signY * rY));
                path.quadTo(midX, y2, midX + (dir1 * r), y2);
                path.lineTo(x2, y2);
            }
        } else {
            int midY = (y1 + y2) / 2;
            path.lineTo(p1X, midY);
            path.lineTo(p2X, midY);
            path.lineTo(p2X, y2);
            path.lineTo(x2, y2);
        }

        g2.draw(path);

        int bar1X = x2 + (dir2 * 5);
        int bar2X = x2 + (dir2 * 9);
        g2.drawLine(bar1X, y2 - 5, bar1X, y2 + 5);
        g2.drawLine(bar2X, y2 - 5, bar2X, y2 + 5);

        g2.drawLine(x1, y1, x1 + (dir1 * 8), y1 - 5);
        g2.drawLine(x1, y1, x1 + (dir1 * 8), y1 + 5);
    }

    /**
     * Kiszámolja egy adott oszlopsor függőleges középpontjának Y koordinátáját, majd a relációs vonalat a megfelelő sorhoz csatlakoztatja.
     *
     * @param table a tábla
     * @param tablePos a tábla bal felső sarkának pozíciója
     * @param columnName a keresett oszlop neve
     * @return az oszlop föggőleges középpontjának Y koordinátája
     * */
    private int getColumnCenterY(Table table, Point tablePos, String columnName) {
        int colIndex = 0;
        for (int i = 0; i < table.columns().size(); i++) {
            if (table.columns().get(i).name().equalsIgnoreCase(columnName)) {
                colIndex = i;
                break;
            }
        }
        return tablePos.y + HEADER_HEIGHT + 4 + (colIndex * ROW_HEIGHT) + (ROW_HEIGHT / 2);
    }

    /**
     * Név alapján megkeres egy táblát
     *
     * @param name a keresett tábla neve
     * @return a megtalált tábla vagy {@code NULL} ha nem létezik
     * */
    private Table findTable(String name) {
        return schema.tables().stream()
                .filter(t -> t.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Kirajzolja az adatbázis táblát a megfelelő adatokkal és mezőtípusokkal.
     *
     * @param table kirajzolandó tábla
     * @param highlighted igaz, ha a kártya felett van az egér vagy éppen húzás alatt van
     * @param y a kártya bal felső sarkának Y koordinátája
     * @param x a kártya bal felső sarkának X koordinátája
     * */
    private void drawTable(Graphics2D g2, Table table, int x, int y, boolean highlighted) {
        int totalHeight = getTableHeight(table);

        // Sima fehér kártyatest
        g2.setColor(CARD_BG);
        g2.fillRoundRect(x, y, TABLE_WIDTH, totalHeight, CORNER_RADIUS, CORNER_RADIUS);

        // Fejléc háttér
        g2.setColor(CARD_HEADER_BG);
        g2.fillRoundRect(x, y, TABLE_WIDTH, HEADER_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);
        g2.fillRect(x, y + HEADER_HEIGHT - 8, TABLE_WIDTH, 8);

        // Elválasztó vonal a fejléc alatt
        g2.setColor(CARD_BORDER);
        g2.drawLine(x, y + HEADER_HEIGHT, x + TABLE_WIDTH, y + HEADER_HEIGHT);

        // Tábla ikon
        g2.setColor(highlighted ? ACCENT_BLUE : new Color(90, 98, 112));
        g2.drawRect(x + 12, y + 14, 11, 9);
        g2.drawLine(x + 12, y + 17, x + 23, y + 17);

        // Táblanév
        g2.setColor(new Color(25, 28, 36));
        String title = truncateText(table.name(), g2.getFontMetrics(), TABLE_WIDTH - 44);
        g2.drawString(title, x + 30, y + 24);

        // Oszlopok kirajzolása
        int currentY = y + HEADER_HEIGHT + 4;

        for (int i = 0; i < table.columns().size(); i++) {
            Column col = table.columns().get(i);
            boolean isFk = foreignKeyColumns.contains(table.name().toLowerCase() + "." + col.name().toLowerCase());

            if (i % 2 == 1) {
                g2.setColor(new Color(248, 249, 251));
                g2.fillRect(x + 2, currentY, TABLE_WIDTH - 4, ROW_HEIGHT);
            }

            int textX = x + 12;

            // PK vagy FK
            if (col.isPrimaryKey()) {
                drawBadge(g2, "PK", textX, currentY + 7, PK_GOLD);
                textX += 24;
            } else if (isFk) {
                drawBadge(g2, "FK", textX, currentY + 7, FK_CYAN);
                textX += 24;
            }

            // Típus szöveg jobbra igazítva
            String typeStr = col.type().name().toLowerCase() + (col.isNullable() ? "" : "!");
            int typeWidth = g2.getFontMetrics().stringWidth(typeStr);
            g2.setColor(new Color(115, 122, 136));
            g2.drawString(typeStr, x + TABLE_WIDTH - typeWidth - 12, currentY + 18);

            // Oszlopnév
            g2.setColor(col.isPrimaryKey() ? new Color(17, 24, 39) : new Color(55, 65, 81));
            int maxNameWidth = (TABLE_WIDTH - typeWidth - 20) - (textX - x);
            String colName = truncateText(col.name(), g2.getFontMetrics(), Math.max(30, maxNameWidth));
            g2.drawString(colName, textX, currentY + 18);

            currentY += ROW_HEIGHT;
        }

        // Külső keret
        g2.setStroke(new BasicStroke(highlighted ? 1.8f : 1.0f));
        g2.setColor(highlighted ? ACCENT_BLUE : CARD_BORDER);
        g2.drawRoundRect(x, y, TABLE_WIDTH, totalHeight, CORNER_RADIUS, CORNER_RADIUS);
        g2.setStroke(new BasicStroke(1.0f));
    }

    /**
     * Kirajzolja a kulcsjelvényt {@code PK} vagy {@code FK}, az oszlopnév előtt jelennek meg.
     *
     * @param label a jelvény felirata
     * @param x a jelvény bal felső sarkának X koordinátája
     * @param y a jelvény bal felső sarkának Y koordinátája
     * @param color a jelvény alapszíne
     * */
    private void drawBadge(Graphics2D g2, String label, int x, int y, Color color) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        g2.fillRoundRect(x, y, 20, 14, 4, 4);
        g2.setColor(color);
        g2.drawRoundRect(x, y, 20, 14, 4, 4);
        g2.drawString(label, x + 3, y + 10);
    }

    /**
     * Levágja a megadott szöveget és három ponttal egészíti ki, amennyiben meghaladha a szöveg a rendelekzésre álló helyet.
     *
     * @param text megjelenítendő szöveg
     * @param fm használt betűtípus
     * @param maxWidth maximálisan engedélyezett szélesség pixelben
     * @return a rövidített szöveg
     * */
    private String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "…";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        for (int i = text.length() - 1; i > 0; i--) {
            String sub = text.substring(0, i);
            if (fm.stringWidth(sub) + ellipsisWidth <= maxWidth) {
                return sub + ellipsis;
            }
        }
        return ellipsis;
    }
}