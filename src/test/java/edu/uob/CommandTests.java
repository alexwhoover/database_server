package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTests {

    private DBServer server;

    @BeforeEach
    void setUp() {
        server = new DBServer();
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void testUse() {
        String response = sendCommandToServer("USE mydb;");
        assertEquals("[OK]", response);
        assertEquals("mydb", server.getDatabaseName());
    }

    @Test
    void testCreateDropDatabase() {
        String response = sendCommandToServer("CREATE DATABASE mydb;");
        assertEquals("[OK]", response);
        assertTrue(new File(server.getStorageFolder(), "mydb").exists());

        response = sendCommandToServer("DROP DATABASE mydb;");
        assertEquals("[OK]", response);
        assertFalse(new File(server.getStorageFolder(), "mydb").exists());
    }

//    @Test
//    void testSelectCommand();
}