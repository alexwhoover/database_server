package edu.uob;

import edu.uob.ds.Table;
import edu.uob.io.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReaderTests {
    private Table t1;
    private Table t2;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() throws Exception {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(tempDir, "people.tab")))) {
            w.write("id\tName\tAge\tEmail\n");
            w.write("1\tBob\t21\tbob@bob.net\n");
            w.write("4\tHarry\t32\tharry@harry.com\n");
            w.write("3\tChris\t42\tchris@chris.ac.uk\n");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(tempDir, "students.tab")))) {
            w.write("id\tName\tNumber\n");
            w.write("6\tBob\t123456789\n");
            w.write("2\tHarry\t987654321\n");
            w.write("3\tChris\t555555555\n");
        }

        t1 = Reader.readTable(tempDir, "people");
        t2 = Reader.readTable(tempDir, "students");
    }

    @Test
    void testTableStructure() {
        assertEquals(List.of("Name", "Age", "Email"), t1.getColNames());
        assertEquals(List.of("Name", "Number"), t2.getColNames());
        assertEquals(3, t1.getRowValues().size());
        assertEquals(3, t2.getRowValues().size());
        assertEquals("21", t1.getRow(1).getValue("Age"));
        assertEquals("Harry", t1.getRow(4).getValue("Name"));
        assertEquals("123456789", t2.getRow(6).getValue("Number"));
    }

    @Test
    void testReadTableNames() {
        List<String> names = Reader.readTableNames(tempDir);
        assertEquals(2, names.size());
        assertTrue(names.containsAll(List.of("people", "students")));
    }
}
