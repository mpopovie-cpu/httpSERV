package ru.otus.java.basic.april.server;

import ru.otus.java.basic.april.server.app.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository {
    private final DatabaseConfig databaseConfig;

    public ItemRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        initializeSchema();
        seedDefaultItems();
    }

    public List<Item> findAll() {
        String sql = "SELECT id, name, price FROM items ORDER BY id";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Item> items = new ArrayList<>();
            while (resultSet.next()) {
                items.add(mapItem(resultSet));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load items from database", e);
        }
    }

    public Optional<Item> findById(long id) {
        String sql = "SELECT id, name, price FROM items WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapItem(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item by id", e);
        }
    }

    public Item save(Item item) {
        String sql = "INSERT INTO items (name, price) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, item.getName());
            statement.setInt(2, item.getPrice());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getLong(1));
                }
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save item", e);
        }
    }

    public boolean deleteById(long id) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item", e);
        }
    }

    private void initializeSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS items (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    price INTEGER NOT NULL CHECK (price >= 0)
                )
                """;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to initialize database schema. Check PostgreSQL connection settings in src/main/resources/db.properties " +
                            "or APP_DB_URL / APP_DB_USER / APP_DB_PASSWORD environment variables.",
                    e
            );
        }
    }

    private void seedDefaultItems() {
        String countSql = "SELECT COUNT(*) FROM items";
        String insertSql = "INSERT INTO items (name, price) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement countStatement = connection.prepareStatement(countSql);
             ResultSet resultSet = countStatement.executeQuery()) {
            if (resultSet.next() && resultSet.getLong(1) > 0) {
                return;
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertDefault(insertStatement, "Bread", 50);
                insertDefault(insertStatement, "Milk", 150);
                insertDefault(insertStatement, "Cheese", 400);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed default items", e);
        }
    }

    private void insertDefault(PreparedStatement insertStatement, String name, int price) throws SQLException {
        insertStatement.setString(1, name);
        insertStatement.setInt(2, price);
        insertStatement.executeUpdate();
    }

    private Item mapItem(ResultSet resultSet) throws SQLException {
        return new Item(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getInt("price")
        );
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }
}
