package hu.levente.prog3.parser;

/**
 * Szöveges elemző {@link Lexer} feldarabolja a begépelt szöveget tokenerke,
 * amelyeket a {@link SchemaParser} fel tud dolgozni.
 *
 * @param type a token típusa
 * @param value az eredeti szöveg
 * @param line sor száma, ahol a token található
 * @param column az oszlop száma a soron belül
 * */
public record Token(TokenType type, String value, int line, int column) {
    public enum TokenType {
        KEYWORD_TABLE,
        KEYWORD_REF,
        IDENTIFIER,
        LBRACE,
        RBRACE,
        LBRACKET,
        RBRACKET,
        DOT,
        GT,
        EOF
    }
}
