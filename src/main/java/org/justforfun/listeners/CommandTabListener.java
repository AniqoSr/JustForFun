package org.justforfun.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.justforfun.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabListener implements TabCompleter {
    private final List<String> subcommands = Arrays.asList("reload", "show", "hide", "create", "setline", "settitle");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("justforfun")) {
            if (args.length == 1) {
                List<String> suggestions = new ArrayList<>();
                for (String subcommand : subcommands) {
                    if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        suggestions.add(subcommand);
                    }
                }
                return suggestions;
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("hide") || args[0].equalsIgnoreCase("setline") || args[0].equalsIgnoreCase("settitle"))) {
                // Suggest IDs of available scoreboards for the 'show', 'hide', 'setline', or 'settitle' subcommands
                return new ArrayList<>(Main.getInstance().getScoreboardManager().getScoreboardIds());
            }
        }
        return null;
    }
}
