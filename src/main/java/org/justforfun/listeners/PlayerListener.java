package org.justforfun.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.justforfun.Main;
import org.justforfun.config.Config;

import java.util.Iterator;

public class PlayerListener implements Listener {
    private final Config configManager = Main.getInstance().getConfigManager();
    private final Main plugin = Main.getInstance();

    public PlayerListener() {
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String soundName = this.configManager.getConfig().getString("item_held_sound.sound", "UI_BUTTON_CLICK");
        float volume = (float) this.configManager.getConfig().getDouble("item_held_sound.volume", 1.0);
        float pitch = (float) this.configManager.getConfig().getDouble("item_held_sound.pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException var7) {
            Main.getInstance().getLogger().warning("Invalid sound name '" + soundName + "' in config.yml.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        Iterator<String> var4 = this.configManager.getConfig().getStringList("join_commands").iterator();

        while (var4.hasNext()) {
            String command = var4.next();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName));
        }

        // Load and show the player's scoreboard
        plugin.getScoreboardManager().loadPlayerScoreboard(player);

        // Load and show the player's temporary scoreboard
        plugin.getScoreboardManager().loadTempScoreboard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String scoreboardId = plugin.getScoreboardManager().getCurrentScoreboardId(player);
        if (scoreboardId != null) {
            plugin.getDataCenter().savePlayerScoreboard(player.getUniqueId().toString(), scoreboardId);
        }

        // Save the player's temporary scoreboard
        plugin.getScoreboardManager().saveTempScoreboard(player);
    }
}
