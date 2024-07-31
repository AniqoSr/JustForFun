package org.justforfun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class ShowScoreboardCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public ShowScoreboardCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /justforfun show <id> <player>");
            return true;
        }

        String id = args[1];
        Player targetPlayer = Bukkit.getPlayer(args[2]);

        if (targetPlayer == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        scoreboardManager.showScoreboard(targetPlayer, id);
        sender.sendMessage("Scoreboard " + id + " shown for " + targetPlayer.getName() + ".");
        return true;
    }
}
