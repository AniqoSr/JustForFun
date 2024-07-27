package org.justforfun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.justforfun.Main;
import org.justforfun.listeners.ScoreboardListener;

public class SetScoreboardLineCommand implements CommandExecutor {
    private final Main plugin;
    private final ScoreboardListener scoreboardManager;

    public SetScoreboardLineCommand(Main plugin, ScoreboardListener scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 4) {
                player.sendMessage("Usage: /justforfun setline <id> <line> <content>");
                return true;
            }

            String id = args[1];
            int line;
            try {
                line = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("Line must be a number.");
                return true;
            }

            // Join the rest of the arguments into a single string
            StringBuilder contentBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i > 3) {
                    contentBuilder.append(" ");
                }
                contentBuilder.append(args[i]);
            }
            String content = contentBuilder.toString();

            if (scoreboardManager.setScoreboardLine(id, line, content)) {
                player.sendMessage("Line " + line + " set to " + content + " on scoreboard " + id + ".");
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