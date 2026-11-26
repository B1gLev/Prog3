package hu.levente.prog3.parser;

import java.awt.*;

public class SchemaLayout {
    private static final int START_X = 40;
    private static final int START_Y = 40;
    private static final int COL_SPACING = 280;
    private static final int ROW_SPACING = 240;
    private static final int COLS_PER_ROW = 3;

    public Point calculatePosition(int tableIndex) {
        int x = START_X + (tableIndex % COLS_PER_ROW) * COL_SPACING;
        int y = START_Y + (tableIndex / COLS_PER_ROW) * ROW_SPACING;
        return new Point(x, y);
    }
}
