package org.justforfun.db;

import org.justforfun.Main;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class DataCenter {
    private final Main plugin;
    private Connection connection;

    public DataCenter(Main plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            File dataFolder = new File(plugin.getDataFolder().getParentFile(), "GenoSuperPlugin/.data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File databaseFile = new File(dataFolder, "playerdata.db");
            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_scoreboards (" +
                            "uuid TEXT PRIMARY KEY," +
                            "scoreboard_id TEXT NOT NULL)")) {
                ps.execute();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_temp_scoreboards (" +
                            "uuid TEXT PRIMARY KEY," +
                            "title TEXT NOT NULL," +
                            "lines TEXT NOT NULL)")) {
                ps.execute();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            File dataFolder = new File(plugin.getDataFolder().getParentFile(), "GenoSuperPlugin/.data");
            File databaseFile = new File(dataFolder, "playerdata.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
        }
        return connection;
    }

    public void savePlayerScoreboard(String uuid, String scoreboardId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT OR REPLACE INTO player_scoreboards (uuid, scoreboard_id) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setString(2, scoreboardId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player scoreboard", e);
        }
    }

    public String getPlayerScoreboard(String uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT scoreboard_id FROM player_scoreboards WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("scoreboard_id");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get player scoreboard", e);
        }
        return null;
    }
}
