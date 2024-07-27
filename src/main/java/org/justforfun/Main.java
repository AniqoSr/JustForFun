package org.justforfun;

import org.justforfun.config.Config;
import org.justforfun.handlers.CommandHandler;
import org.justforfun.listeners.CommandTabListener;
import org.justforfun.listeners.PlayerListener;
import org.justforfun.listeners.ScoreboardListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private Config configManager;
    private PlayerListener playerListener;
    private ScoreboardListener scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("JustForFun has been enabled.");
        this.configManager = new Config(this);
        this.configManager.loadConfig();
        this.scoreboardManager = new ScoreboardListener(this);
        this.playerListener = new PlayerListener();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("PlaceholderAPI found and hooked.");
        } else {
            this.getLogger().warning("PlaceholderAPI not found. Placeholders will not be parsed.");
        }

        getCommand("justforfun").setExecutor(new CommandHandler(this, scoreboardManager));
        getCommand("justforfun").setTabCompleter(new CommandTabListener());

        // Schedule the scoreboard update task
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                configManager.reloadScoreboardConfig();
                scoreboardManager.reloadScoreboards();
            }
        }, 0L, 20L);

        getLogger().info("JustForFun has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("JustForFun has been disabled.");
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
}