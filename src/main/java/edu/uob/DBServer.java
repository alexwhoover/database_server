package edu.uob;

import edu.uob.exceptions.MalformedDBFileException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        Table table = server.readTabFile("people.tab");
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        return "";
    }

    public Table readTabFile(String filePath) throws IOException {
        File file = new File(storageFolderPath + File.separator + filePath);
        FileReader reader = new FileReader(file);
        BufferedReader buffReader = new BufferedReader(reader);

        String line = buffReader.readLine(); // Get First Line

        if (line == null) {
            buffReader.close();
            throw new MalformedDBFileException.EmptyFile(filePath);
        }

        String[] headers = line.split("\t");

        if (headers.length <= 2) {
            buffReader.close();
            throw new MalformedDBFileException.InsufficientColumns(filePath);
        }

        if (!headers[0].equals("id")) {
            buffReader.close();
            throw new MalformedDBFileException.MissingIdColumn(filePath);
        }

        // Fill in table data structure
        Table table = new Table(filePath.replace(".tab", ""));

        // Add column names (skip "id" at index 0)
        for (int i = 1; i < headers.length; i++) {
            table.getColNames().add(headers[i]);
        }

        while ((line = buffReader.readLine()) != null) {
            processRow(line, table);
        }

        buffReader.close();
        return table;
    }

    private void processRow(String line, Table table) throws MalformedDBFileException {
        /**
         * Processes a line from a .tab file and adds row to table
         * Throws a MalformedDBFileException if line does not follow specified format
         */
        String[] values = line.split("\t");
        ArrayList<String> colNames = table.getColNames();

        if (values.length != colNames.size() + 1) {
            throw new MalformedDBFileException.RowWidthMismatch(values.length - 1, colNames.size());
        }

        int id;
        try {
            id = Integer.parseInt(values[0]);
        } catch (NumberFormatException e) {
            throw new MalformedDBFileException.InvalidRowId(values[0]);
        }

        Row row = new Row(id);
        for (int i = 0; i < colNames.size(); i++) {
            row.setValue(colNames.get(i), values[i + 1]);
        }

        table.addRow(row);
    }

    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
