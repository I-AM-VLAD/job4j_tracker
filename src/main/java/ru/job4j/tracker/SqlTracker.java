package ru.job4j.tracker;

import ru.job4j.tracker.model.Item;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SqlTracker implements Store {
    private Connection connection;

    SqlTracker(Connection connection) {
        this.connection = connection;
    }

    SqlTracker() {
    }

    @Override
    public Item add(Item item) {
        try (PreparedStatement statement =
                     connection.prepareStatement("insert into items (name, created) values (?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {
            Timestamp timestamp = Timestamp.valueOf(item.getCreated());
            statement.setString(1, item.getName());
            statement.setTimestamp(2, timestamp);
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public boolean replace(int id, Item item) {
        boolean result = false;
        try (PreparedStatement statement =
                     connection.prepareStatement("update items set name = ?, created = ? where id = ?")) {
            Timestamp timestamp = Timestamp.valueOf(item.getCreated());
            statement.setString(1, item.getName());
            statement.setTimestamp(2, timestamp);
            statement.setInt(3, id);
            result = statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public boolean delete(int id) {
        boolean result = false;
        try (PreparedStatement statement =
                     connection.prepareStatement("delete from items where id = ?")) {
            statement.setInt(1, id);
            result = statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from items")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp timestamp =  resultSet.getTimestamp("created");
                    LocalDateTime created = timestamp.toLocalDateTime();
                    items.add(objectItem(resultSet, created));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<Item> findByName(String key) {
        List<Item> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from items where name = ?")) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                        Timestamp timestamp = resultSet.getTimestamp("created");
                        LocalDateTime created = timestamp.toLocalDateTime();
                        result.add(objectItem(resultSet, created));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Item findById(int id) {
        Item result = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from items where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                        Timestamp timestamp = resultSet.getTimestamp("created");
                        LocalDateTime created = timestamp.toLocalDateTime();
                        result = objectItem(resultSet, created);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void init() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("app.properties")) {
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

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public Item objectItem(ResultSet resultSet, LocalDateTime created) throws SQLException {
        return new Item(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                created
        );
    }
}

