package org.justforfun.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TempScoreboardConfig {
    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, TempScoreboardData> tempScoreboards = new HashMap<>();
    private FileConfiguration parsedConfig;
    private File parsedConfigFile;
    private File unparsedDataFile;

    public TempScoreboardConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadUnparsedData();
    }

    public void loadConfig() {
        parsedConfigFile = new File(plugin.getDataFolder().getParentFile(), "GenoSuperPlugin/.data/tempsbdata.yml");
        if (!parsedConfigFile.exists()) {
            try {
                parsedConfigFile.getParentFile().mkdirs();
                parsedConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        parsedConfig = YamlConfiguration.loadConfiguration(parsedConfigFile);
    }

    public void saveConfig() {
        try {
            parsedConfig.save(parsedConfigFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save tempsbdata.yml to " + parsedConfigFile.getName());
        }
    }

    public FileConfiguration getParsedConfig() {
        if (parsedConfig == null) {
            loadConfig();
        }
        return parsedConfig;
    }

    public void loadUnparsedData() {
        unparsedDataFile = new File(plugin.getDataFolder().getParentFile(), "GenoSuperPlugin/.data/tempdata.json");
        if (!unparsedDataFile.exists()) {
            try {
                unparsedDataFile.getParentFile().mkdirs();
                unparsedDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileReader reader = new FileReader(unparsedDataFile)) {
            Type type = new TypeToken<Map<String, TempScoreboardData>>() {}.getType();
            tempScoreboards = gson.fromJson(reader, type);
            if (tempScoreboards == null) {
                tempScoreboards = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUnparsedData() {
        try (FileWriter writer = new FileWriter(unparsedDataFile)) {
            gson.toJson(tempScoreboards, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save tempdata.json to " + unparsedDataFile.getName());
        }
    }

    public Map<String, TempScoreboardData> getTempScoreboards() {
        return tempScoreboards;
    }

    public void setTempScoreboards(Map<String, TempScoreboardData> tempScoreboards) {
        this.tempScoreboards = tempScoreboards;
        saveUnparsedData();
    }

    public void setTempScoreboard(String uuid, TempScoreboardData data) {
        tempScoreboards.put(uuid, data);
        saveUnparsedData();
    }

    public TempScoreboardData getTempScoreboard(String uuid) {
        return tempScoreboards.get(uuid);
    }

    public void removeTempScoreboard(String uuid) {
        tempScoreboards.remove(uuid);
        saveUnparsedData();
    }
}
