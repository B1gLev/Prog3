package hu.levente.prog3.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lehetővé teszi, hogy az elemző ne álljon le az első hiba észlelésekor, hanem
 * a hiba helyreállítás után tovább folytassa a kód ellenőrzését.
 * */
public class ParseDiagnostics {
    private final List<SchemaParseException> exceptions = new ArrayList<>();

    /**
     * Hozzáad egy új hibát a megadott üzenettel és pontos sor, oszlop koordinátákkal.
     *
     * @param message a hiba szöveges leírása
     * @param line a hiba sor indexe
     * @param column a hiba oszlop indexe
     * */
    public void add(String message, int line, int column) {
        add(new SchemaParseException(message, line, column));
    }

    /**
     * Hozzáad egy új hibát a megadott üzenettel és a Tokennel.
     *
     * @param message a hiba szöveges leírása
     * @param token a hibás token
     * */
    public void add(String message, Token token) {
        add(new SchemaParseException(message, token.line(), token.column()));
    }

    /**
     * Hozzáad egy már meglévő példányt a dignosztika listához.
     * */
    public void add(SchemaParseException exception) {
        exceptions.add(exception);
    }

    /**
     * Megvizsgálja, hogy rögzítve van-e már legalább egy elmzési hiba
     * */
    public boolean hasErrors() {
        return !exceptions.isEmpty();
    }

    /**
     * Visszadja az elsőként rögzített hibát
     * */
    public SchemaParseException getFirstException() {
        return exceptions.isEmpty() ? null : exceptions.getFirst();
    }

    /**
     * Törli az összes korábban rögzített hibát
     * */
    public void clear() {
        exceptions.clear();
    }
}
