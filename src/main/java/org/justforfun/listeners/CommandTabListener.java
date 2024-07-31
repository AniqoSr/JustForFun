package org.justforfun.listeners;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.justforfun.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTabListener implements TabCompleter {
    private final List<String> subcommands = Arrays.asList("reload", "show", "hide", "create", "setline", "settitle", "rename", "delete", "tempsb");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("justforfun")) {
            if (args.length == 1) {
                return subcommands.stream()
                        .filter(subcommand -> subcommand.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && Arrays.asList("show", "hide", "setline", "settitle", "rename", "delete").contains(args[0].toLowerCase())) {
                return new ArrayList<>(Main.getInstance().getScoreboardManager().getScoreboardIds());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("tempsb")) {
                return Arrays.asList("create", "hide", "setline", "show").stream()
                        .filter(subcommand -> subcommand.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 3 && (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("hide") || (args[0].equalsIgnoreCase("tempsb") && Arrays.asList("create", "hide", "setline", "show").contains(args[1].toLowerCase())))) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 4 && args[0].equalsIgnoreCase("tempsb") && args[1].equalsIgnoreCase("setline")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
