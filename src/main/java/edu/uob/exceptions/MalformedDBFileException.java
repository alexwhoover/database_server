package edu.uob.exceptions;
import java.io.IOException;

public class MalformedDBFileException extends IOException {
    private static final long serialVersionUID = 1L;

    public MalformedDBFileException(String message) {
        super(message);
    }

    public static class EmptyFile extends MalformedDBFileException {
        private static final long serialVersionUID = 1L;
        public EmptyFile(String fileName) {
            super("File '" + fileName + "' is empty.");
        }
    }

    public static class MissingIdColumn extends MalformedDBFileException {
        private static final long serialVersionUID = 1L;
        public MissingIdColumn(String fileName) {
            super("File '" + fileName + "' does not have an 'id' column as its first column.");
        }
    }

    public static class InsufficientColumns extends MalformedDBFileException {
        private static final long serialVersionUID = 1L;
        public InsufficientColumns(String fileName) {
            super("File '" + fileName + "' must have at least one column in addition to 'id'.");
        }
    }

    public static class RowWidthMismatch extends MalformedDBFileException {
        private static final long serialVersionUID = 1L;
        public RowWidthMismatch(int rowValues, int colCount) {
            super("Row has " + rowValues + " values but table has " + colCount + " columns.");
        }
    }

    public static class InvalidRowId extends MalformedDBFileException {
        private static final long serialVersionUID = 1L;
        public InvalidRowId(String value) {
            super("Row ID '" + value + "' is not a valid integer.");
        }
    }
}