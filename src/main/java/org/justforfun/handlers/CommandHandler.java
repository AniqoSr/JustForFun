package org.justforfun.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.commands.*;
import org.justforfun.listeners.ScoreboardListener;

import java.util.Arrays;

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

            if (!sender.isOp()) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    plugin.getConfigManager().reloadConfig();
                    plugin.getConfigManager().reloadScoreboardConfig();
                    scoreboardManager.reloadScoreboards();  // Reload scoreboards after config reload
                    plugin.reloadConfig();
                    sender.sendMessage("Configuration reloaded.");
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
                case "tempsb":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /justforfun tempsb <create|hide|setline|show> [args]");
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "create":
                            if (args.length < 3) {
                                sender.sendMessage("Usage: /justforfun tempsb create <player>");
                                return true;
                            }
                            Player tempCreatePlayer = Bukkit.getPlayer(args[2]);
                            if (tempCreatePlayer != null) {
                                scoreboardManager.createTempScoreboard(tempCreatePlayer);
                                sender.sendMessage("Temporary scoreboard created for " + tempCreatePlayer.getName() + ".");
                            } else {
                                sender.sendMessage("Player not found.");
                            }
                            return true;
                        case "hide":
                            if (args.length < 3) {
                                sender.sendMessage("Usage: /justforfun tempsb hide <player>");
                                return true;
                            }
                            Player tempHidePlayer = Bukkit.getPlayer(args[2]);
                            if (tempHidePlayer != null) {
                                scoreboardManager.hideTempScoreboard(tempHidePlayer);
                                sender.sendMessage("Temporary scoreboard hidden for " + tempHidePlayer.getName() + ".");
                            } else {
                                sender.sendMessage("Player not found.");
                            }
                            return true;
                        case "setline":
                            if (args.length < 5) {
                                sender.sendMessage("Usage: /justforfun tempsb setline <player> <line> <content>");
                                return true;
                            }
                            Player tempSetlinePlayer = Bukkit.getPlayer(args[2]);
                            if (tempSetlinePlayer != null) {
                                int line;
                                try {
                                    line = Integer.parseInt(args[3]);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("Line number must be an integer.");
                                    return true;
                                }
                                String content = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                                scoreboardManager.setTempScoreboardLine(tempSetlinePlayer, line, content);
                                sender.sendMessage("Line " + line + " of temporary scoreboard set for " + tempSetlinePlayer.getName() + ".");
                            } else {
                                sender.sendMessage("Player not found.");
                            }
                            return true;
                        case "show":
                            if (args.length < 3) {
                                sender.sendMessage("Usage: /justforfun tempsb show <player>");
                                return true;
                            }
                            Player tempShowPlayer = Bukkit.getPlayer(args[2]);
                            if (tempShowPlayer != null) {
                                scoreboardManager.showTempScoreboard(tempShowPlayer);
                                sender.sendMessage("Temporary scoreboard shown to " + tempShowPlayer.getName() + ".");
                            } else {
                                sender.sendMessage("Player not found.");
                            }
                            return true;
                        default:
                            sender.sendMessage("Unknown tempsb subcommand.");
                            return true;
                    }
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