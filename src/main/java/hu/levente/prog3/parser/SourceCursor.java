package hu.levente.prog3.parser;

import java.util.Objects;

/**
 * Karakternekénti bejárást biztosító osztály, amely implementálja a {@link Cursor} interfacet.
 * */
public class SourceCursor implements Cursor<Character> {
    private final String source;
    private final int length;
    private int cursor = 0;

    public SourceCursor(String source) {
        this.source = Objects.requireNonNullElse(source, "");
        this.length = this.source.length();
    }

    /**
     * {@inheritDoc}
     * @return az aktuális karakter, vagy {@code '\0'}
     * */
    @Override
    public Character peek() {
        return isAtEnd() ? '\0' : source.charAt(cursor);
    }

    /**
     * {@inheritDoc}
     * @return az aktuális karakter a léptetés elptt, vagy {@code '\0'}
     * */
    @Override
    public Character advance() {
        return isAtEnd() ? '\0' : source.charAt(cursor++);
    }

    /**
     * {@inheritDoc}
     * @return az előző karakter, vagy {@code '\0'}m ha a kurzor még az elején áll
     * */
    @Override
    public Character previous() {
        return (cursor <= 0) ? '\0' : source.charAt(cursor - 1);
    }

    @Override
    public boolean isAtEnd() {
        return cursor >= source.length();
    }

    /**
     * Egy karakterrel előretekint az aktuális pozícióhoz képest anélkül, hogy a kurzor aktuális pozicyióját változtatná.
     * */
    public char peekNext() {
        return (cursor + 1 >= length) ? '\0' : source.charAt(cursor + 1);
    }


    /**
     * Visszadja a kurzor aktuális 0-tól indexelt karakterpozícióját.
     * @return az aktuális index
     * */
    public int pos() {
        return cursor;
    }

    /**
     * Kinyeri a forrásszöveg részletét a megadott indextől az aktuális pozicióig.
     *
     * @param start a kezdőindex
     * @return kivágott szövegrész
     * */
    public String extractSubstring(int start) {
        return source.substring(start, cursor);
    }
}
