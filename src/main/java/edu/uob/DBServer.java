package edu.uob;

import edu.uob.exceptions.ParseException;
import edu.uob.nodes.Stmt;
import edu.uob.parse.Lexer;
import edu.uob.parse.Parser;
import edu.uob.parse.Token;
import edu.uob.parse.TokenStream;
import edu.uob.visitors.ExecuteStmtVisitor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    private String currDatabaseName;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        this.storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        this.currDatabaseName = null;
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
        try {
            TokenStream stream = new TokenStream(new Lexer().tokenize(command));
            Stmt stmt = new Parser().parse(stream);
            return stmt.accept(new ExecuteStmtVisitor(this));
        } catch (ParseException e) {
            return "[ERROR] " + e.getMessage();
        } catch (Exception e) {
            return "[ERROR] Unexpected error: " + e.getMessage();
        }
    }

    public void setDatabaseName(String databaseName) {
        this.currDatabaseName = databaseName;
    }

    public String getDatabaseName() {
        return currDatabaseName;
    }

    public File getStorageFolder() {
        return new File(storageFolderPath);
    }

    public File getDatabaseFolder() {
        if (currDatabaseName == null) {
            throw new IllegalStateException("No database selected. Use 'USE <database>;' first.");
        }
        return new File(storageFolderPath, currDatabaseName);
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
