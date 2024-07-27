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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreboardListener {
    private final Main plugin;
    private final Map<String, Scoreboard> scoreboards = new HashMap<>();
    private final Map<Player, String> currentScoreboards = new HashMap<>();

    public ScoreboardListener(Main plugin) {
        this.plugin = plugin;
        loadScoreboards();
    }

    public void loadScoreboards() {
        scoreboards.clear();  // Clear existing scoreboards
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

            int line = 0;
            for (String lineContent : scoreboardsSection.getStringList(id + ".lines")) {
                lineContent = ChatColor.translateAlternateColorCodes('&', lineContent);
                objective.getScore(lineContent).setScore(line++);
            }

            scoreboards.put(id, scoreboard);
        }
    }

    public void reloadScoreboards() {
        plugin.getConfigManager().reloadScoreboardConfig(); // Ensure the file is reloaded from disk
        loadScoreboards();
        updateAllPlayerScoreboards(); // Update all currently active scoreboards
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

        Scoreboard scoreboard = scoreboards.get(id);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            content = ChatColor.translateAlternateColorCodes('&', content);

            // Hapus skor lama jika ada
            for (String entry : scoreboard.getEntries()) {
                if (objective.getScore(entry).getScore() == line) {
                    scoreboard.resetScores(entry);
                    break;
                }
            }

            objective.getScore(content).setScore(line);
        }

        List<String> lines = plugin.getConfigManager().getScoreboardConfig().getStringList("scoreboards." + id + ".lines");
        while (lines.size() <= line) {
            lines.add("");
        }
        lines.set(line, content);
        plugin.getConfigManager().getScoreboardConfig().set("scoreboards." + id + ".lines", lines);
        plugin.getConfigManager().saveScoreboardConfig();

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
        } else {
            player.sendMessage(ChatColor.RED + "Scoreboard with id " + id + " does not exist.");
        }
    }

    public void hideScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        currentScoreboards.remove(player);
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
            for (String entry : scoreboard.getEntries()) {
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
}