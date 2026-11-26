package hu.levente.prog3.parser;

import java.util.List;

public class ListCursor<T> implements Cursor<T> {
    private final List<T> items;
    private final T eofItem;
    private  int current = 0;

    public ListCursor(List<T> items, T eofItem) {
        this.items = items;
        this.eofItem = eofItem;
    }

    @Override
    public T peek() {
        if (isAtEnd()) return eofItem;
        return items.get(current);
    }

    @Override
    public T advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    @Override
    public T previous() {
        return items.get(current-1);
    }

    @Override
    public boolean isAtEnd() {
        return current >= items.size() || items.get(current).equals(eofItem);
    }

    public int pos() {
        return current;
    }
}
