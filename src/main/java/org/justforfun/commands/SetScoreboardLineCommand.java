package org.justforfun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

import java.util.Arrays;

public class SetScoreboardLineCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public SetScoreboardLineCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /justforfun setline <id> <line> <content>");
            return true;
        }

        String id = args[1];
        int line;
        try {
            line = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Line number must be an integer.");
            return true;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (scoreboardManager.setScoreboardLine(id, line, content)) {
            sender.sendMessage("Line " + line + " of scoreboard " + id + " set to " + content + ".");
            // Update the scoreboard for all players viewing it
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (scoreboardManager.isScoreboardActive(player, id)) {
                    scoreboardManager.updatePlayerScoreboard(player, id);
                }
            }
        } else {
            sender.sendMessage("Scoreboard with ID " + id + " does not exist.");
        }
        return true;
    }
}