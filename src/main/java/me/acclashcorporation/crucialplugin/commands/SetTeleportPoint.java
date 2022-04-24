package me.acclashcorporation.crucialplugin.commands;

import me.acclashcorporation.crucialplugin.CrucialPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeleportPoint implements CommandExecutor {

    private final CrucialPlugin plugin;

    public SetTeleportPoint(CrucialPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;
            Location location = player.getLocation();

            if (args.length == 0){
                player.sendMessage(ChatColor.RED + "You need to enter some arguments.");
                player.sendMessage(ChatColor.YELLOW + "To set teleport point: /setpoint <nameofpoint>");
            }else if (args.length == 1){
                try{
                    plugin.getConfig().set(args[0], location);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GOLD + "Point sucessfully saved as " + ChatColor.GREEN + args[0]);
                }catch(NullPointerException e) {
                    player.sendMessage(ChatColor.RED + "Unable to save point.");
                }
            }

        }



        return true;
    }
}