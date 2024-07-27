package org.justforfun.listeners;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.justforfun.config.Config;
import org.justforfun.Main;

public class PlayerListener implements Listener {
    private final Config configManager = Main.getInstance().getConfigManager();

    public PlayerListener() {
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String soundName = this.configManager.getConfig().getString("item_held_sound.sound", "UI_BUTTON_CLICK");
        float volume = (float)this.configManager.getConfig().getDouble("item_held_sound.volume", 1.0);
        float pitch = (float)this.configManager.getConfig().getDouble("item_held_sound.pitch", 1.0);

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
        Iterator var4 = this.configManager.getConfig().getStringList("join_commands").iterator();

        while(var4.hasNext()) {
            String command = (String)var4.next();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName));
        }

    }
}