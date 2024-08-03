package org.justforfun.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.justforfun.Main;
import org.justforfun.util.PlaceholderUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ScoreboardListener {
    private final Main plugin;
    private final Map<String, Scoreboard> scoreboards = new HashMap<>();
    private final Map<Player, String> currentScoreboards = new HashMap<>();
    private final Map<Player, String> tempScoreboardIds = new HashMap<>();
    private final Map<String, Scoreboard> tempScoreboards = new HashMap<>();

    public ScoreboardListener(Main plugin) {
        this.plugin = plugin;
        loadScoreboards();
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
            setScoreboardLines(objective, lines, null);

            scoreboards.put(id, scoreboard);
        }
    }

    public void reloadScoreboards() {
        plugin.getConfigManager().reloadScoreboardConfig();
        loadScoreboards();
        updateAllPlayerScoreboards();
    }

    public void reloadTempScoreboards() {
        plugin.getTempScoreboardConfig().loadConfig();
        loadTempScoreboards();
        updateTempScoreboards();
    }

    public void loadTempScoreboards() {
        // Implement this method to load temp scoreboards from the configuration if necessary
        // This method can be empty if you only need to handle temp scoreboards in the database
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
            List<String> lines = plugin.getConfigManager().getScoreboardConfig().getStringList("scoreboards." + id + ".lines");
            while (lines.size() <= line) {
                lines.add("");
            }
            lines.set(line, content);
            setScoreboardLines(objective, lines, null);

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

    public void updatePlayerScoreboard(Player player, String id) {
        if (!scoreboards.containsKey(id)) {
            return;
        }

        Scoreboard scoreboard = scoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            List<String> lines = plugin.getConfigManager().getScoreboardConfig().getStringList("scoreboards." + id + ".lines");
            setScoreboardLines(objective, lines, player);
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

    private String getUniqueSuffix(int index) {
        return ChatColor.RESET.toString() + ChatColor.values()[index % ChatColor.values().length];
    }

    private void setScoreboardLines(Objective objective, List<String> lines, Player player) {
        // Reset existing scores
        for (String entry : new HashSet<>(objective.getScoreboard().getEntries())) {
            objective.getScoreboard().resetScores(entry);
        }

        // Add new lines
        for (int i = 0; i < lines.size(); i++) {
            String lineContent = lines.get(i);
            if (player != null) {
                lineContent = PlaceholderUtil.applyPlaceholders(player, lineContent);
            }
            lineContent = ChatColor.translateAlternateColorCodes('&', lineContent) + getUniqueSuffix(i);
            objective.getScore(lineContent).setScore(20 - i);
        }
    }

    public void createTempScoreboard(Player player) {
        String id = generateRandomId();
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        String title = plugin.getConfigManager().getConfig().getString("temp_scoreboard.title", "&6Temporary Scoreboard");
        Objective objective = scoreboard.registerNewObjective(id, "dummy", ChatColor.translateAlternateColorCodes('&', title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        tempScoreboards.put(id, scoreboard);
        tempScoreboardIds.put(player, id);
        player.setScoreboard(scoreboard);

        saveTempScoreboard(player);
    }

    public void hideTempScoreboard(Player player) {
        if (tempScoreboardIds.containsKey(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            String id = tempScoreboardIds.remove(player);
            tempScoreboards.remove(id);
            plugin.getTempScoreboardConfig().removeTempScoreboard(id);
            plugin.getDataCenter().removeTempScoreboardId(player.getUniqueId().toString());
        }
    }

    public void setTempScoreboardLine(Player player, int line, String content) {
        if (!tempScoreboardIds.containsKey(player)) {
            return;
        }

        String id = tempScoreboardIds.get(player);
        Scoreboard scoreboard = tempScoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            content = PlaceholderUtil.applyPlaceholders(player, content); // Parse the placeholder
            content = ChatColor.translateAlternateColorCodes('&', content) + getUniqueSuffix(line);

            for (String entry : new HashSet<>(scoreboard.getEntries())) {
                if (objective.getScore(entry).getScore() == 20 - line) {
                    scoreboard.resetScores(entry);
                    break;
                }
            }

            objective.getScore(content).setScore(20 - line);

            saveTempScoreboard(player);
        }
    }

    public void showTempScoreboard(Player player) {
        loadTempScoreboard(player);

        if (tempScoreboardIds.containsKey(player)) {
            String id = tempScoreboardIds.get(player);
            Scoreboard scoreboard = tempScoreboards.get(id);
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                List<String> entries = new ArrayList<>(scoreboard.getEntries());
                for (String entry : entries) {
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
        for (Map.Entry<Player, String> entry : tempScoreboardIds.entrySet()) {
            Player player = entry.getKey();
            String id = entry.getValue();
            Scoreboard scoreboard = tempScoreboards.get(id);
            if (scoreboard != null) {
                Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
                if (objective != null) {
                    List<String> entries = new ArrayList<>(scoreboard.getEntries());
                    for (String entryContent : entries) {
                        String newEntry = PlaceholderUtil.applyPlaceholders(player, entryContent);
                        int score = objective.getScore(entryContent).getScore();
                        scoreboard.resetScores(entryContent);
                        objective.getScore(newEntry).setScore(score);
                    }
                }
                player.setScoreboard(scoreboard);
            }
        }
    }

    public void saveTempScoreboard(Player player) {
        if (!tempScoreboardIds.containsKey(player)) {
            return;
        }

        String id = tempScoreboardIds.get(player);
        Scoreboard scoreboard = tempScoreboards.get(id);
        if (scoreboard != null) {
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                String title = objective.getDisplayName();
                Map<Integer, String> lines = new HashMap<>();
                for (String entry : scoreboard.getEntries()) {
                    int score = objective.getScore(entry).getScore();
                    String line = entry.replace(ChatColor.RESET.toString(), "");
                    lines.put(20 - score, line);
                }
                StringBuilder linesString = new StringBuilder();
                for (Map.Entry<Integer, String> entry : lines.entrySet()) {
                    linesString.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
                }
                plugin.getDataCenter().saveTempScoreboard(player.getUniqueId().toString(), id, title, linesString.toString());
            }
        }
    }

    public void loadTempScoreboard(Player player) {
        ResultSet rs = plugin.getDataCenter().getTempScoreboard(player.getUniqueId().toString());
        if (rs != null) {
            try {
                if (rs.next()) {
                    String id = rs.getString("temp_scoreboard_id");
                    String title = rs.getString("title");
                    String linesString = rs.getString("lines");

                    org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
                    Scoreboard scoreboard = manager.getNewScoreboard();
                    Objective objective = scoreboard.registerNewObjective(id, "dummy", ChatColor.translateAlternateColorCodes('&', title));
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                    String[] linesArray = linesString.split(";");
                    for (String line : linesArray) {
                        String[] parts = line.split(":");
                        int score = Integer.parseInt(parts[0]);
                        String content = PlaceholderUtil.applyPlaceholders(player, parts[1]); // Parse the placeholder
                        content = ChatColor.translateAlternateColorCodes('&', content) + getUniqueSuffix(score);
                        objective.getScore(content).setScore(20 - score);
                    }

                    tempScoreboards.put(id, scoreboard);
                    tempScoreboardIds.put(player, id);
                    player.setScoreboard(scoreboard);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load player temp scoreboard", e);
            }
        }
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateTempScoreboards, 0L, 20L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAllPlayerScoreboards, 0L, 20L);
    }

    // Save the current scoreboard ID of the player
    private void savePlayerScoreboard(Player player) {
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

    // Get the current scoreboard ID of the player
    public String getCurrentScoreboardId(Player player) {
        return currentScoreboards.get(player);
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
    }
}
