package edu.uob.io;

import edu.uob.ds.Table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Writer {
    public static void writeTable(File dbFolder, String tableName, Table table) throws IOException {
        /*
         * Writes a Table object to a .tab file in the specified folder.
         * Throws an IOException if the folder does not exist.
         */
        if (!dbFolder.exists()) {
            throw new IOException("Database folder does not exist: " + dbFolder.getPath());
        }

        File tableFile = new File(dbFolder, tableName + ".tab");

        try (BufferedWriter buffWriter = new BufferedWriter(new FileWriter(tableFile))) {
            buffWriter.write(table.toTabString());
        }
    }

    public static void createDatabase(File storageFolder, String databaseName) {
        /*
         * Creates a new database folder inside the storage folder.
         */
        new File(storageFolder, databaseName).mkdir();
    }

    public static void deleteTable(File dbFolder, String tableName) {
        /*
         * Deletes the .tab file for the given table.
         */
        new File(dbFolder, tableName + ".tab").delete();
    }

    public static void deleteDatabase(File storageFolder, String databaseName) {
        /*
         * Deletes a database folder and all .tab files inside it.
         */
        File dbFolder = new File(storageFolder, databaseName);
        File[] files = dbFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        dbFolder.delete();
    }
}
