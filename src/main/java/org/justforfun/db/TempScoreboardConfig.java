package org.justforfun.db;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class TempScoreboardConfig {
    private final JavaPlugin plugin;
    private FileConfiguration tempScoreboardConfig;
    private File tempScoreboardConfigFile;

    public TempScoreboardConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.tempScoreboardConfigFile = new File(plugin.getDataFolder().getParentFile(), "GenoSuperPlugin/.data/tempsbdata.yml");
        if (!this.tempScoreboardConfigFile.exists()) {
            this.plugin.saveResource("tempsbdata.yml", false);
        }
        this.tempScoreboardConfig = YamlConfiguration.loadConfiguration(this.tempScoreboardConfigFile);
    }

    public FileConfiguration getConfig() {
        if (this.tempScoreboardConfig == null) {
            loadConfig();
        }
        return this.tempScoreboardConfig;
    }

    public void saveConfig() {
        try {
            this.tempScoreboardConfig.save(this.tempScoreboardConfigFile);
        } catch (IOException e) {
            this.plugin.getLogger().warning("Could not save temp scoreboard config to " + this.tempScoreboardConfigFile.getName());
        }
    }

    public void removeTempScoreboard(String id) {
        tempScoreboardConfig.set("temp_scoreboards." + id, null);
        saveConfig();
    }
}
