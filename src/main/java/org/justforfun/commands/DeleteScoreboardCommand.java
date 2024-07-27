package org.justforfun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class DeleteScoreboardCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public DeleteScoreboardCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /justforfun delete <id>");
            return true;
        }

        String id = args[1];

        if (scoreboardManager.deleteScoreboard(id)) {
            sender.sendMessage("Scoreboard with ID " + id + " has been deleted.");
        } else {
            sender.sendMessage("Scoreboard with ID " + id + " does not exist.");
        }
        return true;
    }
}