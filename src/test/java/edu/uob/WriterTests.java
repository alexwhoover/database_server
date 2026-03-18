package edu.uob;

import edu.uob.ds.Row;
import edu.uob.ds.Table;
import edu.uob.io.Reader;
import edu.uob.io.Writer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WriterTests {

    @TempDir
    File tempDir;

    private Table people;

    @BeforeEach
    void setUp() {
        people = new Table();
        people.addCol("Name");
        people.addCol("Age");
        people.addCol("Email");

        Row r1 = new Row();
        r1.setValue("Name", "Bob");
        r1.setValue("Age", "21");
        r1.setValue("Email", "bob@bob.net");
        people.addRow(1, r1);

        Row r2 = new Row();
        r2.setValue("Name", "Harry");
        r2.setValue("Age", "32");
        r2.setValue("Email", "harry@harry.com");
        people.addRow(4, r2);

        Row r3 = new Row();
        r3.setValue("Name", "Chris");
        r3.setValue("Age", "42");
        r3.setValue("Email", "chris@chris.ac.uk");
        people.addRow(3, r3);
    }

    @Test
    void testWriteTable() throws Exception {
        Writer.writeTable(tempDir, "people", people);
        Table readBack = Reader.readTable(tempDir, "people");

        assertNotNull(readBack);
        assertEquals(List.of("Name", "Age", "Email"), readBack.getColNames());
        assertEquals("Bob", readBack.getRow(1).getValue("Name"));
        assertEquals("21", readBack.getRow(1).getValue("Age"));
        assertEquals("bob@bob.net", readBack.getRow(1).getValue("Email"));
        assertEquals("Harry", readBack.getRow(4).getValue("Name"));
        assertEquals("Chris", readBack.getRow(3).getValue("Name"));
    }

    @Test
    void testDeleteTable() throws Exception {
        Writer.writeTable(tempDir, "people", people);
        assertTrue(new File(tempDir, "people.tab").exists());
        Writer.deleteTable(tempDir, "people");
        assertFalse(new File(tempDir, "people.tab").exists());
    }

    @Test
    void testDeleteDatabase() throws Exception {
        Writer.createDatabase(tempDir, "mydb");
        File dbFolder = new File(tempDir, "mydb");
        Writer.writeTable(dbFolder, "people", people);
        assertTrue(new File(dbFolder, "people.tab").exists());

        Writer.deleteDatabase(tempDir, "mydb");
        assertFalse(dbFolder.exists());
    }
}
