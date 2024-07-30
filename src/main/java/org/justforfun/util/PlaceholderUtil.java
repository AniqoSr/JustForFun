package org.justforfun.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderUtil {
    private static final Pattern BETONQUEST_PLACEHOLDER_PATTERN = Pattern.compile("<betonquest_(.*?):(objective|point)\\.(.*?)\\.(amount|total)>");

    public static String applyPlaceholders(Player player, String text) {
        // First apply internal placeholders
        text = applyInternalPlaceholders(player, text);

        // Then apply PlaceholderAPI placeholders
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    private static String applyInternalPlaceholders(Player player, String text) {
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

    private static String convertToPlaceholderAPIFormat(String customPart, String type, String customObjective, String detail) {
        // Customize the conversion logic based on your needs
        return "betonquest_" + customPart + ":" + type + "." + customObjective + "." + detail;
    }
}