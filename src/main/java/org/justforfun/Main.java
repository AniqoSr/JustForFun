package org.justforfun;

import org.justforfun.config.Config;
import org.justforfun.db.DataCenter;
import org.justforfun.db.TempScoreboardConfig;
import org.justforfun.handlers.CommandHandler;
import org.justforfun.listeners.CommandTabListener;
import org.justforfun.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.justforfun.listeners.ScoreboardListener;

import java.sql.SQLException;
import java.util.logging.Level;

public class Main extends JavaPlugin {
    private static Main instance;
    private Config configManager;
    private PlayerListener playerListener;
    private ScoreboardListener scoreboardManager;
    private DataCenter dataCenter;
    private TempScoreboardConfig tempScoreboardConfig;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("JustForFun has been enabled.");
        this.configManager = new Config(this);
        this.configManager.loadConfig();
        this.dataCenter = new DataCenter(this);
        this.tempScoreboardConfig = new TempScoreboardConfig(this);
        this.scoreboardManager = new ScoreboardListener(this);
        this.playerListener = new PlayerListener();

        this.getServer().getPluginManager().registerEvents(playerListener, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("PlaceholderAPI found and hooked.");
        } else {
            this.getLogger().warning("PlaceholderAPI not found. Placeholders will not be parsed.");
        }

        CommandHandler commandHandler = new CommandHandler(this, scoreboardManager);
        getCommand("justforfun").setExecutor(commandHandler);
        getCommand("justforfun").setTabCompleter(new CommandTabListener());

        // Schedule the scoreboard config reload task
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                configManager.reloadScoreboardConfig();
                scoreboardManager.reloadScoreboards();
            }
        }, 0L, 10L);

        // Schedule the temporary scoreboard update task
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            scoreboardManager.updateTempScoreboards();
        }, 0L, 15L);

        getLogger().info("JustForFun has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JustForFun has been disabled.");
        try {
            if (dataCenter.getConnection() != null && !dataCenter.getConnection().isClosed()) {
                dataCenter.getConnection().close();
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
        return this.dataCenter;
    }

    public TempScoreboardConfig getTempScoreboardConfig() {
        return this.tempScoreboardConfig;
    }
}
