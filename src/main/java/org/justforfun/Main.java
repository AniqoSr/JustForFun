package org.justforfun;

import org.bukkit.command.PluginCommand;
import org.justforfun.config.Config;
import org.justforfun.handlers.CommandHandler;
import org.justforfun.listeners.CommandTabListener;
import org.justforfun.listeners.PlayerListener;
import org.justforfun.listeners.ScoreboardListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        registerCommands();

        // Schedule the scoreboard update task
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                scoreboardManager.updatePlayerScoreboard(player);
            }
            scoreboardManager.saveAllScoreboards(); // Save the updated scoreboards to scoreboard.yml
        }, 0L, 20L); // 20L = 20 ticks (1 second)
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

    private void registerCommands() {
        PluginCommand justForFunCommand = this.getCommand("justforfun");
        if (justForFunCommand != null) {
            justForFunCommand.setExecutor(new CommandHandler(this, scoreboardManager));
            justForFunCommand.setTabCompleter(new CommandTabListener());
            this.getLogger().info("Command 'justforfun' registered successfully.");
        } else {
            this.getLogger().warning("Failed to register 'justforfun' command! Plugin might not work as expected.");
        }
    }
}