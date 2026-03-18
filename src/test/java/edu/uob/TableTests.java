package edu.uob;

import edu.uob.ds.Row;
import edu.uob.ds.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TableTests {

    private Table students;

    @BeforeEach
    public void setup() {
        /*
         * Table Name: students
         * id   name    age   mark
         * 1    Alice   30    80
         * 2    Bob     20    60
         * 3    Charlie 25    70
         */
        students = new Table();
        students.addCol("name");
        students.addCol("age");
        students.addCol("mark");

        Row alice = new Row();
        alice.setValue("name", "Alice");
        alice.setValue("age", "30");
        alice.setValue("mark", "80");
        students.addRow(1, alice);

        Row bob = new Row();
        bob.setValue("name", "Bob");
        bob.setValue("age", "20");
        bob.setValue("mark", "60");
        students.addRow(2, bob);

        Row charlie = new Row();
        charlie.setValue("name", "Charlie");
        charlie.setValue("age", "25");
        charlie.setValue("mark", "70");
        students.addRow(3, charlie);
    }

    @Test
    public void testFilter() {
        // Filter for students with age >= 25
        Table result = students.filter(row -> Integer.parseInt(row.getValue("age")) >= 25);

        List<String> names = result.getRowValues().stream()
                .map(row -> row.getValue("name"))
                .toList();

        assertEquals(2, result.getRowValues().size());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Charlie"));
    }

    @Test
    public void filterNoRowsMatch() {
        Table result = students.filter(row -> false);
        assertEquals(0, result.getRowValues().size());
        assertEquals(List.of("name", "age", "mark"), result.getColNames());
    }

    @Test
    public void testProjectColOrder() {
        // Columns should appear in the order given to project(), not the original table order
        Table result = students.project(List.of("mark", "name"));
        assertEquals(List.of("mark", "name"), result.getColNames());
    }

    @Test
    public void testProject() {
        Table result = students.project(List.of("name", "mark"));
        assertEquals(List.of("name", "mark"), result.getColNames());
        assertEquals(3, result.getRowValues().size());
    }

    @Test
    public void testFilterThenProject() {
        Table result = students
                .filter(row -> Integer.parseInt(row.getValue("age")) >= 25)
                .project(List.of("name", "mark"));

        assertEquals(List.of("name", "mark"), result.getColNames());
        assertEquals(2, result.getRowValues().size());

        List<String> names = new ArrayList<>();
        for (Row row : result.getRowValues()) {
            names.add(row.getValue("name"));
        }

        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Charlie"));
    }
}