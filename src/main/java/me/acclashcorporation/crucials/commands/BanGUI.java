package me.acclashcorporation.crucials.commands;

import me.acclashcorporation.crucials.utils.BanMenuUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanGUI implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            BanMenuUtils.openBanMenu(player);
        }
        return true;
    }
}
