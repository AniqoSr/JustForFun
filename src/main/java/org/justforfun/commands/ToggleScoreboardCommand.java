package org.justforfun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class ToggleScoreboardCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public ToggleScoreboardCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage("Usage: /justforfun show <id>");
                return true;
            }

            String id = args[1];
            if (scoreboardManager.isScoreboardActive(player, id)) {
                scoreboardManager.hideScoreboard(player);
                player.sendMessage("Scoreboard " + id + " hidden.");
            } else {
                scoreboardManager.showScoreboard(player, id);
                player.sendMessage("Scoreboard " + id + " shown.");
            }
            return true;
        } else {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
    }
}