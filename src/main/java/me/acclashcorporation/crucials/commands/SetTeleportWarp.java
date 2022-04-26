package me.acclashcorporation.crucials.commands;

import me.acclashcorporation.crucials.Crucials;
import me.acclashcorporation.crucials.files.Points;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeleportWarp implements CommandExecutor {

    private final Crucials plugin;

    public SetTeleportWarp(Crucials plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;
            Location location = player.getLocation();

            if (args.length == 0){
                player.sendMessage(ChatColor.RED + "You need to enter some arguments.");
                player.sendMessage(ChatColor.YELLOW + "To set warp to teleport to: /setwarp <nameofwarp>");
            }else if (args.length == 1){
                try{
                    Points.get().set(args[0], location);
                    Points.save();
                    player.sendMessage(ChatColor.GOLD + "Point sucessfully saved as " + ChatColor.GREEN + args[0]);
                }catch(NullPointerException e) {
                    player.sendMessage(ChatColor.RED + "Unable to save point.");
                }
            }
        }
        return true;
    }
}