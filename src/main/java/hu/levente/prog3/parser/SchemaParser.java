package hu.levente.prog3.parser;

import hu.levente.prog3.factory.SchemaEntityFactory;
import hu.levente.prog3.model.*;
import hu.levente.prog3.util.Result;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SchemaParser {
    private final SchemaEntityFactory schemaEntityFactory;
    private final SemanticValidator validator;
    private final SchemaLayout layout;

    private Cursor<Token> tokens;
    private final ParseDiagnostics diagnostics = new ParseDiagnostics();

    public SchemaParser(SchemaEntityFactory factory) {
        this(factory, new SemanticValidator(), new SchemaLayout());
    }

    public SchemaParser(SchemaEntityFactory schemaEntityFactory, SemanticValidator validator, SchemaLayout layout) {
        this.schemaEntityFactory = Objects.requireNonNull(schemaEntityFactory, "A SchemaEntityFactory nem lehet null");
        this.validator = Objects.requireNonNull(validator, "A SemanticValidator nem lehet null");
        this.layout = Objects.requireNonNull(layout, "A SchemaLayout nem lehet null");
    }

    /**
     * Elemzi a megadott forráskódot, felépíti a modellt.
     *
     * @param text a feldolgozandó nyers forráskód
     * */
    public Result<Schema, SchemaParseException> parse(String text) {
        if (text == null || text.isBlank()) return Result.success(Schema.empty());

        try {
            List<Token> tokenList = new Lexer(text).tokenize();
            Token eofToken = tokenList.getLast();
            this.tokens = new ListCursor<>(tokenList, eofToken);
            this.diagnostics.clear();

            List<Table> tables = new ArrayList<>();
            List<Relationship> relationships = new ArrayList<>();
            Set<String> declaredTables = new HashSet<>();

            while (!tokens.isAtEnd()) {
                try {
                    switch (tokens.peek().type()) {
                        case KEYWORD_TABLE -> {
                            tokens.advance();
                            parseAndRegisterTable(tables, declaredTables);
                        }
                        case KEYWORD_REF -> {
                            tokens.advance();
                            Optional.ofNullable(parseRelationship()).ifPresent(relationships::add);
                        }
                        default -> {
                            Token unexpectedToken = tokens.peek();
                            diagnostics.add("Váratlan kifejezés: '" + unexpectedToken.value() + "'. Elvárt: 'Table' vagy 'Ref:'", unexpectedToken);
                            synchronize();
                        }
                    }
                } catch (SchemaParseException e) {
                    diagnostics.add(e);
                    synchronize();
                }
            }
            Schema schema = new Schema(tables, relationships);
            validator.validate(schema, diagnostics);

            if (diagnostics.hasErrors()) return Result.failure(diagnostics.getFirstException());
            return Result.success(schema);
        } catch (SchemaParseException e) {
            return Result.failure(e);
        }
    }

    /**
     * Feldolgoz egy táblát és ellenőrzi, hogy a táblanév egyedi-e a scheman belül.
     *
     * @param tables a táblák listája
     * @param declaredTables már deklarált táblanevek
     * */
    private void parseAndRegisterTable(List<Table> tables, Set<String> declaredTables) throws SchemaParseException {
        Table table = parseTable(tables.size());
        if (table == null) return;

        if (!declaredTables.add(table.name().toLowerCase())) {
            diagnostics.add("A(z) '" + table.name() + "' tábla már korábban deklarálva lett.", tokens.previous());
            return;
        }
        tables.add(table);
    }

    /**
     * Feldolgozza egyetlen tábla fejlécét és oszloplistáját a nyitó és záró kapcsos zárójelek között.
     *
     * @param index a tábla sorszáma az elrendezési pozició kiszámításához
     * @return a felépített objektum
     * */
    private Table parseTable(int index) throws SchemaParseException {
        Token nameToken = consume(Token.TokenType.IDENTIFIER, "Hiányzó táblanév a 'Table' kulcsszó után");
        consume(Token.TokenType.LBRACE, "Elvárt nyitó kapcsos zárójel '{' a(z) '" + nameToken.value() + "' táblanév után.");

        List<Column> columns = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();

        while (!check(Token.TokenType.RBRACE) && !tokens.isAtEnd()) {
            try {
                Column column = parseColumn();
                if (column != null) {
                    if (!columnNames.add(column.name().toLowerCase())) {
                        diagnostics.add("Duplikált mezőnév: '" + column.name() + "' a(z) '" + nameToken.value() + "' táblában.", tokens.previous());
                    } else columns.add(column);
                }
            } catch (SchemaParseException e) {
                diagnostics.add(e);
                recoverToNextColumn();
            }
        }

        consume(Token.TokenType.RBRACE, "Lezáratlan tábladefiníció: hiányzó '}' zárójel a(z) '" + nameToken.value() + "' táblánál.");
        Point pos = layout.calculatePosition(index);
        return schemaEntityFactory.createTable(nameToken.value(), columns, pos);
    }

    /**
     * Feldolgoz egyetlen oszlopot
     * @return a felépített objektum
     * */
    private Column parseColumn() throws SchemaParseException {
        Token nameToken = consume(Token.TokenType.IDENTIFIER, "Elvárt mezőnév");
        Token tokenType = consume(Token.TokenType.IDENTIFIER, "Elvárt adattípus a(z) " + nameToken.value() + " mezőhöz");

        Optional<DataType> dataType = DataType.fromString(tokenType.value());
        if (dataType.isEmpty()) throw new SchemaParseException(
                "Ismeretlen adattípus: '" + tokenType.value() + "'. Támogatott: int, bigint, varchar, text, decimal, float, boolean, date, timestamp",
                tokenType.line(), tokenType.column()
        );

        boolean isPk = false;
        boolean isNullable = true;

        if (match(Token.TokenType.LBRACKET)) {
            while (!check(Token.TokenType.RBRACKET) && !tokens.isAtEnd()) {
                Token modifier = consume(Token.TokenType.IDENTIFIER, "Elvárt meződmódosító");
                String modVal = modifier.value().toLowerCase();

                switch (modVal) {
                    case "pk", "primary" -> {
                        isPk = true;
                        isNullable = false;
                    }
                    case "not" -> {
                        if (check(Token.TokenType.IDENTIFIER) && tokens.peek().value().equalsIgnoreCase("null")) {
                            tokens.advance();
                            isNullable = false;
                        } else diagnostics.add("A 'not' kulcsszót 'null' kell kövesse.", modifier);
                    }
                    case "null" -> isNullable = true;
                    default -> diagnostics.add("Ismeretlen módosító: '" + modVal + "'", modifier);
                }
            }
            consume(Token.TokenType.RBRACKET, "Lezáratlan attribótum");
        }

        return schemaEntityFactory.createColumn(nameToken.value(), dataType.get(), isPk, isNullable);
    }

    /**
     * Feldolgoz egy relációs kifejezést
     * @return a felépített objektum
     **/
    private Relationship parseRelationship() throws SchemaParseException {
        Token sourceTable = consume(Token.TokenType.IDENTIFIER, "Hiányzó forrsátábla a 'Ref:' után");
        consume(Token.TokenType.DOT, "Elvárt '.' a forrástábla és oszlop között");
        Token sourceCol = consume(Token.TokenType.IDENTIFIER, "Hiányzó forrásoszlop");

        Token op = tokens.peek();
        boolean inverse = false;

        if (match(Token.TokenType.GT)) {
            inverse = false;
        } else if (match(Token.TokenType.IDENTIFIER) && tokens.previous().value().equals("<")) {
            inverse = true;
        } else throw new SchemaParseException("Elvárt '>' vagy '<' operátor a relációban", op.line(), op.column());

        Token targetTable = consume(Token.TokenType.IDENTIFIER, "Hiányzó céltábla");
        consume(Token.TokenType.DOT, "Elvárt '.' a céltábla és oszlop között");
        Token targetCol = consume(Token.TokenType.IDENTIFIER, "Hiányzó céloszlop");

        return inverse ? schemaEntityFactory.createRelationship(
                targetTable.value(),
                targetCol.value(),
                sourceTable.value(),
                sourceCol.value()) :
                schemaEntityFactory.createRelationship(
                        sourceTable.value(),
                        sourceCol.value(),
                        targetTable.value(),
                        targetCol.value());
    }

    /**
     * Ellenőrzi, hogy a soron következp toke illeszkedik-e a megadott típusok valamelyikére, majd egyezés esetén
     * lépteti a kurzort.
     * */
    private boolean match(Token.TokenType... types) {
        boolean matched = Arrays.stream(types).anyMatch(this::check);
        if (matched) tokens.advance();
        return matched;
    }

    /**
     * Elvárt típusu tokent vár a kurzoltól
     *
     * @param type az elvárt token típus
     * @param message a hibaüzenet ha nem várt token következik
     * */
    private Token consume(Token.TokenType type, String message) throws SchemaParseException {
        if (check(type)) return tokens.advance();

        Token currentToken = tokens.peek();
        throw new SchemaParseException(message, currentToken.line(), currentToken.column());
    }

    /**
     * Ellenőrzi, hogy az aktuális token típusa megegyezik-e a megadott típussal, a kurzor léptetése nélkül
     * */
    private boolean check(Token.TokenType type) {
        if (tokens.isAtEnd()) return type == Token.TokenType.EOF;
        return tokens.peek().type() == type;
    }

    /**
     * Szintaktikai hiba esetén átugorja a hibás tokeneket.
     * */
    private void synchronize() {
        tokens.advance();
        while (!tokens.isAtEnd()) {
            if (tokens.previous().type() == Token.TokenType.RBRACE) return;

            switch(tokens.peek().type()) {
                case KEYWORD_TABLE, KEYWORD_REF -> {
                    return;
                }
                default -> tokens.advance();
            }
        }
    }

    /**
     * Egy hibás oszlopdefinició után a következő sorra vagy tábla záró kapocsra lépteti a kurzort.
     * */
    private void recoverToNextColumn() {
        while (!tokens.isAtEnd() && !check(Token.TokenType.RBRACE)) {
            Token t = tokens.peek();
            if (t.line() > tokens.previous().line()) return;
            tokens.advance();
        }
    }
}
