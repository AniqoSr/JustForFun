package org.justforfun.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.justforfun.Main;
import org.justforfun.db.TempScoreboardData;
import org.justforfun.util.PlaceholderUtil;

import java.util.*;

public class ScoreboardListener {
    private final Main plugin;
    private final Map<String, Scoreboard> scoreboards = new HashMap<>();
    private final Map<Player, String> currentScoreboards = new HashMap<>();
    private final Map<Player, Scoreboard> tempScoreboards = new HashMap<>();

    public ScoreboardListener(Main plugin) {
        this.plugin = plugin;
        loadScoreboards();
        loadTempScoreboards();
        startUpdateTask();
    }

    public void loadScoreboards() {
        scoreboards.clear();
        ConfigurationSection scoreboardsSection = plugin.getConfigManager().getScoreboardConfig().getConfigurationSection("scoreboards");
        if (scoreboardsSection == null) {
            plugin.getLogger().warning("No scoreboards section found in configuration!");
            return;
        }

        for (String id : scoreboardsSection.getKeys(false)) {
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective(id, "dummy", ChatColor.translateAlternateColorCodes('&', scoreboardsSection.getString(id + ".title")));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<String> lines = scoreboardsSection.getStringList(id + ".lines");
            setScoreboardLines(objective, lines);

            scoreboards.put(id, scoreboard);
        }
    }

    public void reloadScoreboards() {
        plugin.getConfigManager().reloadScoreboardConfig();
        plugin.reloadConfig();
        loadScoreboards();
        updateAllPlayerScoreboards();
    }

    public void loadTempScoreboards() {
        tempScoreboards.clear();
        Map<String, TempScoreboardData> tempData = plugin.getTempScoreboardConfig().getTempScoreboards();
        for (Map.Entry<String, TempScoreboardData> entry : tempData.entrySet()) {
            String playerId = entry.getKey();
            TempScoreboardData data = entry.getValue();

            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("temp", "dummy", ChatColor.translateAlternateColorCodes('&', data.getTitle()));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            setScoreboardLines(objective, new ArrayList<>(data.getLines().values()));

            Player player = Bukkit.getPlayer(UUID.fromString(playerId));
            if (player != null) {
                tempScoreboards.put(player, scoreboard);
            }
        }
    }

    public void reloadTempScoreboards() {
        plugin.getTempScoreboardConfig().loadUnparsedData();
        loadTempScoreboards();
        updateTempScoreboards();
    }

    public Set<String> getScoreboardIds() {
        return scoreboards.keySet();
    }

    public boolean createScoreboard(String id) {
        if (scoreboards.containsKey(id)) {
            return false;
        }

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(id, "dummy", ChatColor.translateAlternateColorCodes('&', "&fNew Scoreboard"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboards.put(id, scoreboard);

        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id + ".title", "&fNew Scoreboard");
        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id + ".lines", new ArrayList<>());
        plugin.getConfigManager().saveScoreboardConfig();

        return true;
    }

    public boolean setScoreboardTitle(String id, String title) {
        if (!scoreboards.containsKey(id)) {
            return false;
        }

        Scoreboard scoreboard = scoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            title = ChatColor.translateAlternateColorCodes('&', title);
            objective.setDisplayName(title);
        }

        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id + ".title", title);
        plugin.getConfigManager().saveScoreboardConfig();

        return true;
    }

    public boolean setScoreboardLine(String id, int line, String content) {
        if (!scoreboards.containsKey(id)) {
            return false;
        }

        if (line < 0 || line >= 20) {
            return false;
        }

        Scoreboard scoreboard = scoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            for (String entry : new HashSet<>(scoreboard.getEntries())) {
                scoreboard.resetScores(entry);
            }

            List<String> lines = plugin.getConfigManager().getScoreboardConfig().getStringList("scoreboards." + id + ".lines");
            while (lines.size() <= line) {
                lines.add("");
            }
            lines.set(line, content);
            setScoreboardLines(objective, lines);

            plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id + ".lines", lines);
            plugin.getConfigManager().saveScoreboardConfig();
        }

        return true;
    }

    public boolean renameScoreboard(String oldId, String newId) {
        if (!scoreboards.containsKey(oldId) || scoreboards.containsKey(newId)) {
            return false;
        }

        Scoreboard scoreboard = scoreboards.remove(oldId);
        scoreboards.put(newId, scoreboard);

        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + newId, plugin.getConfigManager().getScoreboardConfig().getConfigurationSection("scoreboards." + oldId));
        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + oldId, null);
        plugin.getConfigManager().saveScoreboardConfig();

        return true;
    }

    public boolean deleteScoreboard(String id) {
        if (!scoreboards.containsKey(id)) {
            return false;
        }

        scoreboards.remove(id);
        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id, null);
        plugin.getConfigManager().saveScoreboardConfig();

        return true;
    }

    public void showScoreboard(Player player, String id) {
        if (scoreboards.containsKey(id)) {
            hideCurrentScoreboard(player);
            updatePlayerScoreboard(player, id);
            currentScoreboards.put(player, id);
            savePlayerScoreboard(player);
        } else {
            player.sendMessage(ChatColor.RED + "Scoreboard with id " + id + " does not exist.");
        }
    }

    public void hideScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        currentScoreboards.remove(player);
        savePlayerScoreboard(player);
    }

    public void hideCurrentScoreboard(Player player) {
        if (currentScoreboards.containsKey(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public boolean isScoreboardActive(Player player, String id) {
        return id.equals(currentScoreboards.get(player));
    }

    public void updatePlayerScoreboard(Player player) {
        String id = currentScoreboards.get(player);
        if (id != null) {
            updatePlayerScoreboard(player, id);
        }
    }

    public void updatePlayerScoreboard(Player player, String id) {
        if (!scoreboards.containsKey(id)) {
            return;
        }

        Scoreboard scoreboard = scoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            for (String entry : new HashSet<>(scoreboard.getEntries())) {
                String newEntry = PlaceholderUtil.applyPlaceholders(player, entry);
                int score = objective.getScore(entry).getScore();
                scoreboard.resetScores(entry);
                objective.getScore(newEntry).setScore(score);
            }
        }
        player.setScoreboard(scoreboard);
    }

    public void updateAllPlayerScoreboards() {
        for (Map.Entry<Player, String> entry : currentScoreboards.entrySet()) {
            Player player = entry.getKey();
            String id = entry.getValue();
            updatePlayerScoreboard(player, id);
        }
    }

    public void saveAllScoreboards() {
        ConfigurationSection scoreboardsSection = plugin.getConfigManager().getScoreboardConfig().createSection("scoreboards");
        for (Map.Entry<String, Scoreboard> entry : scoreboards.entrySet()) {
            String id = entry.getKey();
            Scoreboard scoreboard = entry.getValue();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                scoreboardsSection.set(id + ".title", objective.getDisplayName());
                List<String> lines = new ArrayList<>();
                for (String entryContent : scoreboard.getEntries()) {
                    lines.add(entryContent);
                }
                scoreboardsSection.set(id + ".lines", lines);
            }
        }
        plugin.getConfigManager().saveScoreboardConfig();
    }

    private String getUniqueSuffix(int index) {
        return ChatColor.RESET.toString() + ChatColor.values()[index % ChatColor.values().length];
    }

    private void setScoreboardLines(Objective objective, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String lineContent = ChatColor.translateAlternateColorCodes('&', lines.get(i)) + getUniqueSuffix(i);
            objective.getScore(lineContent).setScore(20 - i);
        }
    }

    public void createTempScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        String title = plugin.getConfigManager().getConfig().getString("temp_scoreboard.title", "&6Temporary Scoreboard");
        Objective objective = scoreboard.registerNewObjective("temp", "dummy", ChatColor.translateAlternateColorCodes('&', title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        tempScoreboards.put(player, scoreboard);
        player.setScoreboard(scoreboard);

        saveTempScoreboard(player, false); // false to avoid parsing placeholders when saving
    }

    public void hideTempScoreboard(Player player) {
        if (tempScoreboards.containsKey(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            tempScoreboards.remove(player);
            plugin.getTempScoreboardConfig().getParsedConfig().set("temp_scoreboards." + player.getUniqueId().toString(), null);
            plugin.getTempScoreboardConfig().saveConfig();
            plugin.getTempScoreboardConfig().removeTempScoreboard(player.getUniqueId().toString());
        }
    }

    public void setTempScoreboardLine(Player player, int line, String content) {
        if (!tempScoreboards.containsKey(player)) {
            return;
        }

        Scoreboard scoreboard = tempScoreboards.get(player);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            content = ChatColor.translateAlternateColorCodes('&', content) + getUniqueSuffix(line);

            for (String entry : new HashSet<>(scoreboard.getEntries())) {
                if (objective.getScore(entry).getScore() == 20 - line) {
                    scoreboard.resetScores(entry);
                    break;
                }
            }

            objective.getScore(content).setScore(20 - line);

            saveTempScoreboard(player, false); // false to avoid parsing placeholders when saving
        }
    }

    public void showTempScoreboard(Player player) {
        loadTempScoreboard(player);

        if (tempScoreboards.containsKey(player)) {
            Scoreboard scoreboard = tempScoreboards.get(player);
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                for (String entry : new HashSet<>(scoreboard.getEntries())) {
                    String newEntry = PlaceholderUtil.applyPlaceholders(player, entry);
                    int score = objective.getScore(entry).getScore();
                    scoreboard.resetScores(entry);
                    objective.getScore(newEntry).setScore(score);
                }
            }
            player.setScoreboard(scoreboard);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have a temporary scoreboard to show.");
        }
    }

    public void updateTempScoreboards() {
        for (Map.Entry<Player, Scoreboard> entry : tempScoreboards.entrySet()) {
            Player player = entry.getKey();
            Scoreboard scoreboard = entry.getValue();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                for (String entryContent : new HashSet<>(scoreboard.getEntries())) {
                    String newEntry = PlaceholderUtil.applyPlaceholders(player, entryContent);
                    int score = objective.getScore(entryContent).getScore();
                    scoreboard.resetScores(entryContent);
                    objective.getScore(newEntry).setScore(score);
                }
            }
            player.setScoreboard(scoreboard);
        }
    }

    public void saveTempScoreboard(Player player, boolean parsePlaceholders) {
        if (!tempScoreboards.containsKey(player)) {
            return;
        }

        Scoreboard scoreboard = tempScoreboards.get(player);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            String title = objective.getDisplayName();
            Map<Integer, String> lines = new HashMap<>();
            for (String entry : scoreboard.getEntries()) {
                int score = objective.getScore(entry).getScore();
                String line = entry.replace(ChatColor.RESET.toString(), "");
                if (parsePlaceholders) {
                    line = PlaceholderUtil.applyPlaceholders(player, line);
                }
                lines.put(20 - score, line);
            }
            TempScoreboardData data = new TempScoreboardData(title, lines);
            plugin.getTempScoreboardConfig().setTempScoreboard(player.getUniqueId().toString(), data);

            // Save parsed data to tempsbdata.yml
            plugin.getTempScoreboardConfig().getParsedConfig().set("temp_scoreboards." + player.getUniqueId().toString() + ".title", title);
            List<String> parsedLines = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : lines.entrySet()) {
                parsedLines.add(entry.getKey() + ":" + entry.getValue());
            }
            plugin.getTempScoreboardConfig().getParsedConfig().set("temp_scoreboards." + player.getUniqueId().toString() + ".lines", parsedLines);
            plugin.getTempScoreboardConfig().saveConfig();
        }
    }

    public void loadTempScoreboard(Player player) {
        TempScoreboardData data = plugin.getTempScoreboardConfig().getTempScoreboard(player.getUniqueId().toString());
        if (data != null) {
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("temp", "dummy", ChatColor.translateAlternateColorCodes('&', data.getTitle()));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (Map.Entry<Integer, String> entry : data.getLines().entrySet()) {
                int score = entry.getKey();
                String content = ChatColor.translateAlternateColorCodes('&', entry.getValue()) + getUniqueSuffix(score);
                objective.getScore(content).setScore(20 - score);
            }

            tempScoreboards.put(player, scoreboard);
            player.setScoreboard(scoreboard);
        }
    }

    public String getCurrentScoreboardId(Player player) {
        return currentScoreboards.get(player);
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateTempScoreboards, 0L, 15L);
    }

    // Save the current scoreboard ID of the player
    public void savePlayerScoreboard(Player player) {
        String uuid = player.getUniqueId().toString();
        String scoreboardId = currentScoreboards.get(player);

        plugin.getDataCenter().savePlayerScoreboard(uuid, scoreboardId);
    }

    // Load the current scoreboard ID of the player
    public void loadPlayerScoreboard(Player player) {
        String uuid = player.getUniqueId().toString();
        String scoreboardId = plugin.getDataCenter().getPlayerScoreboard(uuid);

        if (scoreboardId != null && scoreboards.containsKey(scoreboardId)) {
            showScoreboard(player, scoreboardId);
        }
    }
}