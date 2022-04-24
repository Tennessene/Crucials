package me.acclashcorporation.crucialplugin.commands;

import me.acclashcorporation.crucialplugin.CrucialPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportPoint implements CommandExecutor {

    private final CrucialPlugin plugin;

    public TeleportPoint(CrucialPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;

            if (args.length == 0){
                player.sendMessage(ChatColor.RED + "You need to enter some arguments.");
                player.sendMessage(ChatColor.YELLOW + "To teleport to point: /tppoint <nameofpoint>");
            }else if (args.length == 1){
                try{
                    Location location = plugin.getConfig().getLocation(args[0]);
                    player.teleport(location);
                    player.sendMessage(ChatColor.GOLD + "Sent to " + ChatColor.GREEN + args[0]);
                }catch(NullPointerException e) {
                    player.sendMessage(ChatColor.RED + "Unable to teleport to " + args[0] + ".");
                }
            }

        }



        return true;
    }
}