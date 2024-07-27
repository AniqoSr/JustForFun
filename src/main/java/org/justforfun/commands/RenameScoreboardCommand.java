package org.justforfun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class RenameScoreboardCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public RenameScoreboardCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /justforfun rename <oldId> <newId>");
            return true;
        }

        String oldId = args[1];
        String newId = args[2];

        if (scoreboardManager.renameScoreboard(oldId, newId)) {
            sender.sendMessage("Scoreboard ID " + oldId + " renamed to " + newId + ".");
        } else {
            sender.sendMessage("Scoreboard with ID " + oldId + " does not exist or ID " + newId + " is already in use.");
        }
        return true;
    }
}