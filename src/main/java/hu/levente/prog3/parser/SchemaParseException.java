package hu.levente.prog3.parser;

public class SchemaParseException extends Exception {
  private final int lineNumber;
  private final int columnNumber;

  public SchemaParseException(String message, int lineNumber, int columnNumber) {
    super(String.format("Szintaktikai hiba a(z) %d. sor %d. oszlopában %s", lineNumber, columnNumber, message));
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }
}
