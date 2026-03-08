package edu.uob;
import java.io.IOException;

public class MalformedDBFileException extends IOException {
    public MalformedDBFileException(String message) {
        super(message);
    }

    public static class EmptyFile extends MalformedDBFileException {
        public EmptyFile(String fileName) {
            super("File '" + fileName + "' is empty.");
        }
    }

    public static class MissingIdColumn extends MalformedDBFileException {
        public MissingIdColumn(String fileName) {
            super("File '" + fileName + "' does not have an 'id' column as its first column.");
        }
    }

    public static class InsufficientColumns extends MalformedDBFileException {
        public InsufficientColumns(String fileName) {
            super("File '" + fileName + "' must have at least one column in addition to 'id'.");
        }
    }

    public static class RowWidthMismatch extends MalformedDBFileException {
        public RowWidthMismatch(int rowValues, int colCount) {
            super("Row has " + rowValues + " values but table has " + colCount + " columns.");
        }
    }

    public static class InvalidRowId extends MalformedDBFileException {
        public InvalidRowId(String value) {
            super("Row ID '" + value + "' is not a valid integer.");
        }
    }
}