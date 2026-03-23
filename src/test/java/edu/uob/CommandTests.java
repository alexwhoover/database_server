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

    @Test
    void testSelectWithWhere() {
        // Setup
        sendCommandToServer("CREATE DATABASE testdb;");
        sendCommandToServer("USE testdb;");
        sendCommandToServer("CREATE TABLE people (name, age, active);");
        sendCommandToServer("INSERT INTO people VALUES ('Alice', 30, TRUE);");
        sendCommandToServer("INSERT INTO people VALUES ('Bob', 17, FALSE);");
        sendCommandToServer("INSERT INTO people VALUES ('Charlie', 25, TRUE);");
        sendCommandToServer("INSERT INTO people VALUES ('Diana', 17, TRUE);");

        // Basic equality
        String response = sendCommandToServer("SELECT * FROM people WHERE age == 17;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Bob"));
        assertTrue(response.contains("Diana"));
        assertFalse(response.contains("Alice"));
        assertFalse(response.contains("Charlie"));

        // Greater than
        response = sendCommandToServer("SELECT * FROM people WHERE age > 18;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));
        assertFalse(response.contains("Bob"));
        assertFalse(response.contains("Diana"));

        // AND condition
        response = sendCommandToServer("SELECT * FROM people WHERE age == 17 AND active == TRUE;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Diana"));
        assertFalse(response.contains("Bob"));
        assertFalse(response.contains("Alice"));

        // OR condition
        response = sendCommandToServer("SELECT * FROM people WHERE age == 30 OR age == 25;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));
        assertFalse(response.contains("Bob"));
        assertFalse(response.contains("Diana"));

        // LIKE condition
        response = sendCommandToServer("SELECT * FROM people WHERE name LIKE '%i%';");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));
        assertTrue(response.contains("Diana"));
        assertFalse(response.contains("Bob"));

        // Project specific columns with WHERE
        response = sendCommandToServer("SELECT name FROM people WHERE age > 18;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));
        assertFalse(response.contains("30")); // age column should not appear
        assertFalse(response.contains("25"));

        // No results
        response = sendCommandToServer("SELECT * FROM people WHERE age == 999;");
        assertTrue(response.startsWith("[OK]"));
        assertFalse(response.contains("Alice"));
        assertFalse(response.contains("Bob"));

        // Nested: (age > 18 AND active == TRUE) OR age == 17
        // Expects: Alice, Charlie (age > 18 AND active), Bob and Diana (age == 17)
        response = sendCommandToServer("SELECT * FROM people WHERE (age > 18 AND active == TRUE) OR age == 17;");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));
        assertTrue(response.contains("Bob"));
        assertTrue(response.contains("Diana"));

        // Nested: age > 16 AND (active == TRUE AND name LIKE 'D%')
        // Expects: only Diana (active, name starts with D, age > 16)
        response = sendCommandToServer("SELECT * FROM people WHERE age > 16 AND (active == TRUE AND name LIKE 'D%');");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Diana"));
        assertFalse(response.contains("Alice"));
        assertFalse(response.contains("Bob"));
        assertFalse(response.contains("Charlie"));

        // Nested: (age > 20 AND age < 28) OR (age == 17 AND active == FALSE)
        // Expects: Charlie (25, in range) and Bob (17, inactive)
        response = sendCommandToServer("SELECT * FROM people WHERE (age > 20 AND age < 28) OR (age == 17 AND active == FALSE);");
        assertTrue(response.startsWith("[OK]"));
        assertTrue(response.contains("Charlie"));
        assertTrue(response.contains("Bob"));
        assertFalse(response.contains("Alice"));
        assertFalse(response.contains("Diana"));

        // Cleanup
        sendCommandToServer("DROP DATABASE testdb;");
    }

    @Test
    void testDelete() {
        sendCommandToServer("CREATE DATABASE testdb;");
        sendCommandToServer("USE testdb;");
        sendCommandToServer("CREATE TABLE people (name, age, active);");
        sendCommandToServer("INSERT INTO people VALUES ('Alice', 30, TRUE);");
        sendCommandToServer("INSERT INTO people VALUES ('Bob', 17, FALSE);");
        sendCommandToServer("INSERT INTO people VALUES ('Charlie', 25, TRUE);");
        sendCommandToServer("INSERT INTO people VALUES ('Diana', 17, TRUE);");

        // Basic delete
        sendCommandToServer("DELETE FROM people WHERE name == 'Bob';");
        String response = sendCommandToServer("SELECT * FROM people;");
        assertFalse(response.contains("Bob"));
        assertTrue(response.contains("Alice"));

        // Delete with AND - only Diana matches (17 AND active)
        sendCommandToServer("DELETE FROM people WHERE age == 17 AND active == TRUE;");
        response = sendCommandToServer("SELECT * FROM people;");
        assertFalse(response.contains("Diana"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));

        // Delete non-matching condition - no rows should be removed
        sendCommandToServer("DELETE FROM people WHERE age == 999;");
        response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Charlie"));

        sendCommandToServer("DROP DATABASE testdb;");
    }

    @Test
    void testUpdate() {
        sendCommandToServer("CREATE DATABASE testdb;");
        sendCommandToServer("USE testdb;");
        sendCommandToServer("CREATE TABLE people (name, age);");
        sendCommandToServer("INSERT INTO people VALUES ('Alice', 30);");
        sendCommandToServer("INSERT INTO people VALUES ('Bob', 17);");

        // Basic update
        sendCommandToServer("UPDATE people SET age = 18 WHERE name == 'Bob';");
        String response = sendCommandToServer("SELECT * FROM people WHERE name == 'Bob';");
        assertTrue(response.contains("18"));
        assertFalse(response.contains("17"));

        // Update should not affect other rows
        response = sendCommandToServer("SELECT * FROM people WHERE name == 'Alice';");
        assertTrue(response.contains("30"));

        // Cannot update id
        response = sendCommandToServer("UPDATE people SET id = 99 WHERE name == 'Alice';");
        assertTrue(response.startsWith("[ERROR]"));

        sendCommandToServer("DROP DATABASE testdb;");
    }
}