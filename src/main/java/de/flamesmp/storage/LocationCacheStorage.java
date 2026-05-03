package de.flamesmp.storage;

import de.flamesmp.FlameRTP;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationCacheStorage {

    private static final Logger LOGGER = Logger.getLogger(LocationCacheStorage.class.getName());

    private final @NotNull ExecutorService executor;
    private @Nullable Connection connection;

    public LocationCacheStorage() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void connect() throws SQLException {
        final File folder = FlameRTP.getInstance().getDataFolder();
        if (!folder.exists()) folder.mkdirs();

        final File file = new File(folder, "rtp.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

        try (final Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cooldowns (
                        uuid       TEXT NOT NULL,
                        world      TEXT NOT NULL,
                        expires_at INTEGER NOT NULL,
                        PRIMARY KEY (uuid, world)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cached_locations (
                        id    INTEGER PRIMARY KEY AUTOINCREMENT,
                        world TEXT NOT NULL,
                        x     REAL NOT NULL,
                        y     REAL NOT NULL,
                        z     REAL NOT NULL
                    );
                    """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cached_world ON cached_locations(world);");
        }
    }

    @SneakyThrows
    public void disconnect() {
        executor.shutdown();
        if (connection != null) {
            connection.close();
            LOGGER.info("Disconnected from SQLite database.");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    @NotNull
    public CompletableFuture<Map<String, Long>> loadCooldowns() {
        final long now = System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            final Map<String, Long> result = new HashMap<>();
            try (final PreparedStatement stmt = prepareStatement(
                    "SELECT uuid, world, expires_at FROM cooldowns WHERE expires_at > ?;", now);
                 final ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final String key = rs.getString("uuid") + ":" + rs.getString("world").toLowerCase();
                    result.put(key, rs.getLong("expires_at"));
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load cooldowns.", e);
                throw new RuntimeException(e);
            }
            return result;
        }, executor).thenCompose(result ->
                executeUpdateAsync("DELETE FROM cooldowns WHERE expires_at <= ?;", now)
                        .thenApply(deleted -> result)
        );
    }

    @NotNull
    public CompletableFuture<Integer> saveCooldown(final @NotNull UUID uuid, final @NotNull String worldName, final long expiresAt) {
        return executeUpdateAsync(
                "INSERT INTO cooldowns (uuid, world, expires_at) VALUES (?, ?, ?) " +
                        "ON CONFLICT(uuid, world) DO UPDATE SET expires_at = excluded.expires_at;",
                uuid.toString(), worldName.toLowerCase(), expiresAt
        );
    }

    @NotNull
    public CompletableFuture<Integer> deleteCooldown(final @NotNull UUID uuid, final @NotNull String worldName) {
        return executeUpdateAsync(
                "DELETE FROM cooldowns WHERE uuid = ? AND world = ?;",
                uuid.toString(), worldName.toLowerCase()
        );
    }

    @NotNull
    public CompletableFuture<List<double[]>> loadCachedLocations(final @NotNull String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            final List<double[]> result = new ArrayList<>();
            try (final PreparedStatement stmt = prepareStatement(
                    "SELECT x, y, z FROM cached_locations WHERE world = ?;", worldName.toLowerCase());
                 final ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z")});
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load cached locations for world " + worldName, e);
                throw new RuntimeException(e);
            }
            return result;
        }, executor);
    }

    @NotNull
    public CompletableFuture<Integer> saveCachedLocation(final @NotNull String worldName, final double x, final double y, final double z) {
        return executeUpdateAsync(
                "INSERT INTO cached_locations (world, x, y, z) VALUES (?, ?, ?, ?);",
                worldName.toLowerCase(), x, y, z
        );
    }

    @NotNull
    public CompletableFuture<Integer> clearCachedLocations(final @NotNull String worldName) {
        return executeUpdateAsync(
                "DELETE FROM cached_locations WHERE world = ?;",
                worldName.toLowerCase()
        );
    }

    @NotNull
    public CompletableFuture<Integer> executeUpdateAsync(final @NotNull String sql, final @NotNull Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (final PreparedStatement stmt = prepareStatement(sql, params)) {
                return stmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Update failed: " + sql, e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @NotNull
    public CompletableFuture<Void> executeQueryAsync(final @NotNull String sql, final @NotNull Consumer<ResultSet> consumer, final @NotNull Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (final PreparedStatement stmt = prepareStatement(sql, params);
                 final ResultSet rs = stmt.executeQuery()) {
                consumer.accept(rs);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Query failed: " + sql, e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @NotNull
    private PreparedStatement prepareStatement(final @NotNull String sql, final @NotNull Object... params) throws SQLException {
        final PreparedStatement stmt = requireConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
        return stmt;
    }

    @NotNull
    private Connection requireConnection() {
        if (connection == null) throw new IllegalStateException("Not connected! Call connect() first.");
        return connection;
    }
}