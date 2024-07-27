package org.justforfun.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.commands.*;
import org.justforfun.listeners.ScoreboardListener;

public class CommandHandler implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public CommandHandler(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("justforfun")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /justforfun <subcommand>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("justforfun.reload")) {
                        plugin.getConfigManager().reloadConfig();
                        plugin.getConfigManager().reloadScoreboardConfig();
                        scoreboardManager.reloadScoreboards();  // Reload scoreboards after config reload
                        sender.sendMessage("Configuration reloaded.");
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                    return true;
                case "show":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /justforfun show <id> [player]");
                        return true;
                    }
                    String showId = args[1];
                    Player targetShowPlayer = args.length > 2 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
                    if (targetShowPlayer != null) {
                        scoreboardManager.showScoreboard(targetShowPlayer, showId);
                        scoreboardManager.updatePlayerScoreboard(targetShowPlayer, showId);  // Update the scoreboard immediately
                        sender.sendMessage("Scoreboard " + showId + " shown to " + targetShowPlayer.getName() + ".");
                    } else {
                        sender.sendMessage("Player not found.");
                    }
                    return true;
                case "hide":
                    Player targetHidePlayer = args.length > 1 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player ? (Player) sender : null);
                    if (targetHidePlayer != null) {
                        scoreboardManager.hideScoreboard(targetHidePlayer);
                        sender.sendMessage("Scoreboard hidden for " + targetHidePlayer.getName() + ".");
                    } else {
                        sender.sendMessage("Player not found.");
                    }
                    return true;
                case "create":
                    return new CreateScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "setline":
                    return new SetScoreboardLineCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "settitle":
                    return new SetScoreboardTitleCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "rename":
                    return new RenameScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "delete":
                    return new DeleteScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                default:
                    sender.sendMessage("Unknown subcommand.");
                    return true;
            }
        }
        return false;
    }
}