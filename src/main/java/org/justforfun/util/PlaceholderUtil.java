package org.justforfun.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderUtil {
    public static String applyPlaceholders(Player player, String text) {
        if (player != null && text != null && org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}
