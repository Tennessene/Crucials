package me.acclashcorporation.crucials.commands;

import me.acclashcorporation.crucials.Crucials;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SaveServer implements CommandExecutor {

    private final Crucials plugin;

    public SaveServer(Crucials plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You need to enter some arguments");
                player.sendMessage(ChatColor.YELLOW + "To save the server network to the GitHub repo: /saveserver <what you built/did>");
            } else if (args.length == 1) {
                if (plugin.getConfig().getString("Repository URL") != null && plugin.getConfig().getString("GitHub username") != null && plugin.getConfig().getString("GitHub password") != null) {
                    Path repofolder = Path.of("Server Network Repo");
                    File test = new File(String.valueOf(repofolder));
                    if (Files.exists(repofolder)) {
                        test.delete();
                    }
                    player.sendMessage(ChatColor.GREEN + "Saving server with caption: " + args[0] + "...");
                    File curdir = new File(System.getProperty("user.dir"));
                    Path networkFolder = curdir.toPath().getParent();
                    Path finalFolder = Path.of(repofolder + File.separator + "Minecraft Servers");
                    Date date = new Date();
                    char subColon = '\uA789';
                    String filename = date.toString().replace(':', subColon) + "-" + args[0].toUpperCase().replace("_", " ") + ".txt";

                    File txt = new File(networkFolder + File.separator + filename);
                    try {
                        txt.createNewFile();
                        Git.init().setDirectory(new File("Server Network Repo")).call();
                        Git git = Git.open(new File("Server Network Repo"));
                        git.add().addFilepattern(networkFolder.toString()).call();

                        Files.copy(networkFolder, finalFolder, REPLACE_EXISTING);
                        // add remote repo:
                        RemoteAddCommand remoteAddCommand = git.remoteAdd();
                        remoteAddCommand.setName("origin");
                        remoteAddCommand.setUri(new URIish(plugin.getConfig().getString("Repository URL")));
                        remoteAddCommand.call();

                        // push to remote:
                        PushCommand pushCommand = git.push();
                        pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(plugin.getConfig().getString("GitHub username"), plugin.getConfig().getString("GitHub password")));
                        pushCommand.call();
                    } catch (GitAPIException | IOException | URISyntaxException e) {
                        player.sendMessage(ChatColor.RED + "Unable to retrieve repository.");
                    }
                    player.sendMessage("Filename test: " + filename);
                } else {
                    player.sendMessage(ChatColor.RED + "You need to fill in all the fields under Save Server in the configuration file before you can use this feature.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Too many arguments");
                player.sendMessage(ChatColor.YELLOW + "To save the server network to the GitHub repo: /saveserver <what you built/did>");
            }
        }
        return true;
    }
}