package edu.uob.io;

import edu.uob.ds.Row;
import edu.uob.ds.Table;
import edu.uob.exceptions.MalformedDBFileException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    public static Table readTable(File dbFolder, String tableName) throws IOException, MalformedDBFileException {
        /*
         * Reads a .tab file of specified format and converts it into a Table object
         * Returns the Table object, or null if file not found
         * throws IOException or MalformedDBException if error
         */
        File tableFile = new File(dbFolder, tableName + ".tab");
        try (BufferedReader buffReader = new BufferedReader(new FileReader(tableFile))){
            String filePath = tableFile.getPath(); // For passing to exceptions for debugging
            String line = buffReader.readLine(); // Get First Line

            if (line == null) {
                throw new MalformedDBFileException.EmptyFile(filePath);
            }

            String[] headers = line.split("\t");

            if (headers.length <= 2) {
                throw new MalformedDBFileException.InsufficientColumns(filePath);
            }

            if (!headers[0].equals("id")) {
                throw new MalformedDBFileException.MissingIdColumn(filePath);
            }

            // Fill in table data structure
            Table table = new Table();

            // Add column names (skip "id" at index 0)
            for (int i = 1; i < headers.length; i++) {
                table.getColNames().add(headers[i]);
            }

            while ((line = buffReader.readLine()) != null) {
                processRow(line, table);
            }

            return table;
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

        private static void processRow(String line, Table table) throws MalformedDBFileException {
        /*
         * Processes a line from a .tab file and adds row to table
         * Throws a MalformedDBFileException if line does not follow specified format
         */
        String[] values = line.split("\t");
        List<String> colNames = table.getColNames();

        if (values.length != colNames.size() + 1) {
            throw new MalformedDBFileException.RowWidthMismatch(values.length - 1, colNames.size());
        }

        int id;
        try {
            id = Integer.parseInt(values[0]);
        } catch (NumberFormatException e) {
            throw new MalformedDBFileException.InvalidRowId(values[0]);
        }

        Row row = new Row();
        for (int i = 0; i < colNames.size(); i++) {
            row.setValue(colNames.get(i), values[i + 1]);
        }

        table.addRow(id, row);
    }

    public static List<String> readTableNames(File dbFolder) {
        List<String> tableNames = new ArrayList<>();

        File[] tableFiles = dbFolder.listFiles(
                f -> f.getName().endsWith(".tab")
        );
        if (tableFiles == null) return tableNames;

        for (File f : tableFiles) {
            String filename = f.getName();
            String tableName = filename.substring(0, filename.lastIndexOf('.'));
            tableNames.add(tableName);
        }

        return tableNames;
    }

    public static List<String> readDatabaseNames(File storageFolder) {
        List<String> databaseNames = new ArrayList<>();

        File[] folders = storageFolder.listFiles(
                f -> f.isDirectory()
        );
        if (folders == null) return databaseNames;

        for (File f : folders) {
            databaseNames.add(f.getName());
        }

        return databaseNames;
    }
}
