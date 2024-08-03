package org.justforfun;

import net.justforfun.MMTracker.placeholders.MMTrackerPlaceholder;
import net.justforfun.MMTracker.storage.Database;
import org.justforfun.config.Config;
import org.justforfun.db.DataCenter;
import org.justforfun.db.TempScoreboardConfig;
import org.justforfun.handlers.CommandHandler;
import org.justforfun.listeners.CommandTabListener;
import org.justforfun.listeners.PlayerListener;
import org.justforfun.listeners.ScoreboardListener;
import org.justforfun.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    private static Main instance;
    private Config configManager;
    private ScoreboardListener scoreboardManager;
    private DataCenter databaseManager;
    private TempScoreboardConfig tempScoreboardConfig;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("JustForFun has been enabled.");
        this.configManager = new Config(this);
        this.configManager.loadConfig();
        this.scoreboardManager = new ScoreboardListener(this);
        this.databaseManager = new DataCenter(this);
        this.tempScoreboardConfig = new TempScoreboardConfig(this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("PlaceholderAPI found and hooked.");
            // Initialize PlaceholderUtil with PlaceholderAPIUtil from MMTracker
            PlaceholderUtil.setMMTrackerDatabase(new Database(net.justforfun.MMTracker.Main.getInstance()));
        } else {
            this.getLogger().warning("PlaceholderAPI not found. Placeholders will not be parsed.");
        }

        CommandHandler commandHandler = new CommandHandler(this, scoreboardManager);
        getCommand("justforfun").setExecutor(commandHandler);
        getCommand("justforfun").setTabCompleter(new CommandTabListener());

        getLogger().info("JustForFun has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JustForFun has been disabled.");
        try {
            if (databaseManager.getConnection() != null && !databaseManager.getConnection().isClosed()) {
                databaseManager.getConnection().close();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not close database connection", e);
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public Config getConfigManager() {
        return this.configManager;
    }

    public ScoreboardListener getScoreboardManager() {
        return this.scoreboardManager;
    }

    public DataCenter getDataCenter() {
        return databaseManager;
    }

    public TempScoreboardConfig getTempScoreboardConfig() {
        return tempScoreboardConfig;
    }
}
