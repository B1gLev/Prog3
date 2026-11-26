package hu.levente.prog3.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A leírónyelv nyers szöveges forráskódját tokenekké alakítja.
 *
 * Kezeli a whitespace karaktereket, az egysoros megjegyzéseket és nyomon követi a pontos sor és
 * oszlopszámokat a hibák megmutatásához.
 * Felismeri a kulcsszavakat és az azonosítókat és írásjeleket.
 * */
public class Lexer {
    private final SourceCursor cursor;
    private int line = 1;
    private int lineStartOffset = 0;

    public Lexer(String source) {
        this.cursor = new SourceCursor(source);
    }

    /**
     * Ellenőrzi, hogy a megadott karakter kezdhet-e azonosítót kulcsszóval
     * @param c a vizsgált karakter
     * */
    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    /**
     * Ellebprzi, hogy a megadott karakter része lehet-e egy azonosítónak.
     * @param c a vizsgált karakter
     * */
    private boolean isIdentPart(char c) {
        return Character.isLetter(c) || c == '_' || c == '-';
    }

    /**
     * Átugorja a szóközöket, tabulátorokat, sortöréseket és a sorvégi megjegyzéseket, emellett frissíti az aktuális
     * sor és oszlopszímtáshoz szükséges eltolást.
     * */
    private void skipWhitespaceAndComments() {
        while (!cursor.isAtEnd()) {
            char c = cursor.peek();

            if (c == ' ' || c == '\t' || c == '\r') {
                cursor.advance();
            } else if (c == '\n') {
                line++;
                cursor.advance();
                lineStartOffset = cursor.pos();
            } else if (c == '/' && cursor.peekNext() == '/') {
                while (!cursor.isAtEnd() && cursor.peek() != '\n') cursor.advance();
            } else if (c == '#') {
                while (!cursor.isAtEnd() && cursor.peek() != '\n') cursor.advance();
            } else break;
        }
    }

    /**
     * Beolvas egy teljes azonosítót vagy fentartott kulcsszót
     *
     * @param col a token oszlop indexe az aktuális sorban
     * */
    private Token readIdentOrKeyword(int col) {
        int start = cursor.pos();

        while (!cursor.isAtEnd() && isIdentPart(cursor.peek()))
            cursor.advance();

        if (!cursor.isAtEnd() && cursor.peek() == ':') {
            String check = cursor.extractSubstring(start);
            if (check.equalsIgnoreCase("ref")) {
                cursor.advance();
                return new Token(Token.TokenType.KEYWORD_REF, "Ref:", line, col);
            }
        }

        String word = cursor.extractSubstring(start);
        if (word.equalsIgnoreCase("table"))
            return new Token(Token.TokenType.KEYWORD_TABLE, word, line, col);

        if (word.equalsIgnoreCase("ref"))
            return new Token(Token.TokenType.KEYWORD_REF, word, line, col);

        return new Token(Token.TokenType.IDENTIFIER, word, line, col);
    }

    /**
     * Végigolvassa a teljes forráskódot, majd előállítja a tokeneket és listába rakja őket.
     *
     * @return token lista
     * @throws SchemaParseException ha a nyelv által nem ismert karakter szerepel
     * */
    public List<Token> tokenize() throws SchemaParseException {
        List<Token> tokens = new ArrayList<>();

        while (!cursor.isAtEnd()) {
            skipWhitespaceAndComments();
            if (cursor.isAtEnd()) break;

            int col = cursor.pos() - lineStartOffset + 1;
            char c = cursor.peek();

            switch (c) {
                case '{' -> {
                    tokens.add(new Token(Token.TokenType.LBRACE, "{", line, col));
                    cursor.advance();
                }
                case '}' -> {
                    tokens.add(new Token(Token.TokenType.RBRACE, "}", line, col));
                    cursor.advance();
                }
                case '[' -> {
                    tokens.add(new Token(Token.TokenType.LBRACKET, "[", line, col));
                    cursor.advance();
                }
                case ']' -> {
                    tokens.add(new Token(Token.TokenType.RBRACKET, "]", line, col));
                    cursor.advance();
                }
                case '.' -> {
                    tokens.add(new Token(Token.TokenType.DOT, ".", line, col));
                    cursor.advance();
                }
                case '>' -> {
                    tokens.add(new Token(Token.TokenType.GT, ">", line, col));
                    cursor.advance();
                }
                default -> {
                    if (isIdentStart(c)) {
                        tokens.add(readIdentOrKeyword(col));
                    } else throw new SchemaParseException("Váratlan karakter: " + c, line, col);
                }
            }
        }
        tokens.add(new Token(Token.TokenType.EOF, "", line, cursor.pos() -lineStartOffset + 1));
        return tokens;
    }
}
