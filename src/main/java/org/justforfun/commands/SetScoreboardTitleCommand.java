package org.justforfun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class SetScoreboardTitleCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public SetScoreboardTitleCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 3) {
                player.sendMessage("Usage: /justforfun settitle <id> <title>");
                return true;
            }

            String id = args[1];
            StringBuilder titleBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) {
                    titleBuilder.append(" ");
                }
                titleBuilder.append(args[i]);
            }
            String title = titleBuilder.toString();

            if (scoreboardManager.setScoreboardTitle(id, title)) {
                player.sendMessage("Title of scoreboard " + id + " set to " + title + ".");
                scoreboardManager.updatePlayerScoreboard(player, id);
            } else {
                player.sendMessage("Scoreboard with id " + id + " does not exist.");
            }
            return true;
        } else {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
    }
}