package org.justforfun.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.justforfun.MMTracker.storage.Database;
import net.seyarada.pandeloot.trackers.DamageBoard;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderUtil {
    private static final Pattern BETONQUEST_PLACEHOLDER_PATTERN = Pattern.compile("<betonquest_(.*?):(objective|point)\\.(.*?)\\.(amount|total)>");
    private static final Pattern CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("<(player\\.(name|damage))>");
    private static final Pattern MODIFIABLE_PLACEHOLDER_PATTERN = Pattern.compile("<(\\d+)\\.(name|damage|percent|ratio)>");
    private static final Pattern MMTRACK_PLACEHOLDER_PATTERN = Pattern.compile("<mmtrack_(topname|topdamage)_(.*?)_(\\d+)>");

    private static Database mmTrackerDatabase;

    public static void setMMTrackerDatabase(Database database) {
        mmTrackerDatabase = database;
    }

    public static String applyPlaceholders(OfflinePlayer player, String text) {
        // First apply internal placeholders
        text = applyInternalPlaceholders(player, text);

        // Then apply PlaceholderAPI placeholders if any remain
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    private static String applyInternalPlaceholders(OfflinePlayer player, String text) {
        text = convertBetonQuestPlaceholders(player, text);
        text = convertCustomPlaceholders(player, text);
        text = convertModifiablePlaceholders(player, text);
        text = convertMMTrackerPlaceholders(player, text);
        return text;
    }

    private static String convertBetonQuestPlaceholders(OfflinePlayer player, String text) {
        Matcher matcher = BETONQUEST_PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0);
            String customPart = matcher.group(1);
            String type = matcher.group(2);
            String customObjective = matcher.group(3);
            String detail = matcher.group(4);

            // Convert the internal placeholder to PlaceholderAPI format
            String convertedPlaceholder = convertToPlaceholderAPIFormat(customPart, type, customObjective, detail);

            // Apply PlaceholderAPI placeholder
            String value = PlaceholderAPI.setPlaceholders(player, "%" + convertedPlaceholder + "%");

            matcher.appendReplacement(buffer, value != null ? value : "N/A");
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String convertCustomPlaceholders(OfflinePlayer player, String text) {
        Matcher matcher = CUSTOM_PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0);
            String placeholderKey = matcher.group(1);

            // Apply custom logic for handling placeholders
            String value = applyCustomPlaceholder(player, placeholderKey);

            matcher.appendReplacement(buffer, value != null ? value : "N/A");
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String convertModifiablePlaceholders(OfflinePlayer player, String text) {
        Matcher matcher = MODIFIABLE_PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0);
            int rank = Integer.parseInt(matcher.group(1)) - 1;
            String placeholderType = matcher.group(2);

            // Apply logic for handling modifiable placeholders
            String value = applyModifiablePlaceholder(player, rank, placeholderType);

            matcher.appendReplacement(buffer, value != null ? value : "N/A");
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String convertMMTrackerPlaceholders(OfflinePlayer player, String text) {
        Matcher matcher = MMTRACK_PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0);
            String type = matcher.group(1);
            String mobType = matcher.group(2);
            int rank = Integer.parseInt(matcher.group(3));

            // Apply MMTracker logic for handling placeholders
            String value = applyMMTrackerPlaceholder(player, type, mobType, rank);

            matcher.appendReplacement(buffer, value != null ? value : "N/A");
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String convertToPlaceholderAPIFormat(String customPart, String type, String customObjective, String detail) {
        // Customize the conversion logic based on your needs
        return "betonquest_" + customPart + ":" + type + "." + customObjective + "." + detail;
    }

    private static String applyCustomPlaceholder(OfflinePlayer player, String placeholder) {
        switch (placeholder) {
            case "player.name":
                return player.getName();
            case "player.damage":
                return getPlayerDamage(player);
            default:
                return null;
        }
    }

    private static String applyModifiablePlaceholder(OfflinePlayer player, int rank, String placeholderType) {
        for (DamageBoard board : DamageBoard.damageBoards.values()) {
            if (rank < board.getSortedPlayers().size()) {
                Map.Entry<UUID, Double> entry = board.getSortedPlayers().get(rank);
                switch (placeholderType) {
                    case "name":
                        Player rankedPlayer = player.isOnline() ? ((Player) player.getPlayer()) : null;
                        return rankedPlayer != null ? rankedPlayer.getName() : "Unknown";
                    case "damage":
                        return String.valueOf(entry.getValue());
                    case "percent":
                        return board.getPercent(rank, false);
                    case "ratio":
                        return board.getPercent(rank, true);
                    default:
                        return null;
                }
            }
        }
        return "N/A";
    }

    private static String applyMMTrackerPlaceholder(OfflinePlayer player, String type, String mobType, int rank) {
        if (mmTrackerDatabase == null) {
            return "N/A";
        }

        if (type.equals("topname")) {
            return mmTrackerDatabase.getTopPlayerName(mobType, rank);
        } else if (type.equals("topdamage")) {
            return String.valueOf(mmTrackerDatabase.getTopPlayerDamage(mobType, rank));
        }

        return "N/A";
    }

    private static String getPlayerDamage(OfflinePlayer player) {
        UUID playerUUID = player.getUniqueId();
        for (DamageBoard board : DamageBoard.damageBoards.values()) {
            for (Map.Entry<UUID, Double> entry : board.playersAndDamage.entrySet()) {
                if (entry.getKey().equals(playerUUID)) {
                    return String.valueOf(entry.getValue());
                }
            }
        }
        return "0";
    }
}
