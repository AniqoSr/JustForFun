package org.justforfun.config;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration scoreboardConfig;
    private File scoreboardConfigFile;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        this.plugin.saveDefaultConfig();
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        loadScoreboardConfig();
    }

    public void loadScoreboardConfig() {
        this.scoreboardConfigFile = new File(this.plugin.getDataFolder(), "scoreboard.yml");
        if (!this.scoreboardConfigFile.exists()) {
            this.plugin.saveResource("scoreboard.yml", false);
        }
        this.scoreboardConfig = YamlConfiguration.loadConfiguration(this.scoreboardConfigFile);
    }

    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.loadConfig();
        }
        return this.config;
    }

    public FileConfiguration getScoreboardConfig() {
        if (this.scoreboardConfig == null) {
            this.loadScoreboardConfig();
        }
        return this.scoreboardConfig;
    }

    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        } catch (IOException var2) {
            this.plugin.getLogger().warning("Could not save config to " + this.configFile.getName());
        }
    }

    public void saveScoreboardConfig() {
        try {
            this.scoreboardConfig.save(this.scoreboardConfigFile);
        } catch (IOException var2) {
            this.plugin.getLogger().warning("Could not save scoreboard config to " + this.scoreboardConfigFile.getName());
        }
    }

    public void reloadConfig() {
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void reloadScoreboardConfig() {
        this.scoreboardConfigFile = new File(this.plugin.getDataFolder(), "scoreboard.yml");
        this.scoreboardConfig = YamlConfiguration.loadConfiguration(this.scoreboardConfigFile);
    }
}