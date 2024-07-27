package org.justforfun.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

import java.util.Arrays;

public class SetScoreboardTitleCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public SetScoreboardTitleCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /justforfun settitle <id> <title>");
            return true;
        }

        String id = args[1];
        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        if (scoreboardManager.setScoreboardTitle(id, title)) {
            sender.sendMessage("Title of scoreboard " + id + " set to " + title + ".");
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