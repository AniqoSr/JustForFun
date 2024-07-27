package org.justforfun.handlers;

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
                        plugin.getConfigManager().loadConfig();
                        scoreboardManager.loadScoreboards();  // Reload scoreboards after config reload
                        sender.sendMessage("Configuration reloaded.");
                    } else {
                        sender.sendMessage("You do not have permission to use this command.");
                    }
                    return true;
                case "show":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /justforfun show <id>");
                        return true;
                    }
                    String showId = args[1];
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        scoreboardManager.hideCurrentScoreboard(player);
                        return new ShowScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                    } else {
                        sender.sendMessage("This command can only be run by a player.");
                    }
                    return true;
                case "hide":
                    return new HideScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "create":
                    return new CreateScoreboardCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "setline":
                    return new SetScoreboardLineCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                case "settitle":
                    return new SetScoreboardTitleCommand(plugin, scoreboardManager).onCommand(sender, command, label, args);
                default:
                    sender.sendMessage("Unknown subcommand.");
                    return true;
            }
        }
        return false;
    }
}