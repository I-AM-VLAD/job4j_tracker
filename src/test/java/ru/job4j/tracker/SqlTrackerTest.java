package ru.job4j.tracker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.tracker.model.Item;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SqlTrackerTest {

    private static Connection connection;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("db/liquibase_test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void testFindById() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertThat(tracker.findById(item.getId()), is(item));
    }

    @Test
    public void testAdd() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        assertThat(tracker.add(item), is(item));
    }

    @Test
    public void testFindByName() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item1");
        Item item2 = new Item("item2");
        Item item3 = new Item("item3");
        tracker.add(item1);
        tracker.add(item2);
        tracker.add(item3);
        assertThat(tracker.findByName("item1"), is(List.of(item1)));
    }

    @Test
    public void testFindAll() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item1");
        Item item2 = new Item("item2");
        Item item3 = new Item("item3");
        tracker.add(item1);
        tracker.add(item2);
        tracker.add(item3);
        assertThat(tracker.findAll(), is(List.of(item1, item2, item3)));
    }

    @Test
    public void testDelete() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertTrue(tracker.delete(item.getId()));
    }

    @Test
    public void testReplace() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item newItem = new Item("newItem");
        tracker.add(item);
        tracker.replace(item.getId(), newItem);
        assertThat(tracker.findById(item.getId()).getName(), is("newItem"));
    }
}
