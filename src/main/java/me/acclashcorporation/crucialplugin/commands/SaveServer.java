package me.acclashcorporation.crucialplugin.commands;

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

public class SaveServer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You need to enter some arguments");
                player.sendMessage(ChatColor.YELLOW + "To save the server network to the GitHub website: /saveserver <what you built/did>");
            } else if (args.length == 1) {
                Path repofolder = Path.of("Server Network Repo");
                File test = new File(String.valueOf(repofolder));
                if (Files.exists(repofolder)) {
                    test.delete();
                }
                player.sendMessage(ChatColor.GREEN + "Saving server with caption: " + args[0] + "...");
                File curdir = new File(System.getProperty("user.dir"));
                Path networkfolder = curdir.toPath().getParent().getParent();
                try {
                    Git git = Git.open(new File("Server Network Repo"));
                    git.add().addFilepattern(networkfolder + "\\Minecraft Servers");

                    // add remote repo:
                    RemoteAddCommand remoteAddCommand = git.remoteAdd();
                    remoteAddCommand.setName("origin");
                    remoteAddCommand.setUri(new URIish("https://github.com/Tennessene/tennessene.github.io"));
                    // you can add more settings here if needed
                    remoteAddCommand.call();

                    // push to remote:
                    PushCommand pushCommand = git.push();
                    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("Tennessene", "anston06@"));
                    // you can add more settings here if needed
                    pushCommand.call();
                } catch (GitAPIException | IOException | URISyntaxException e) {
                    player.sendMessage(ChatColor.RED + "Unable to retrieve repository.");
                }
                player.sendMessage("Directory test: " + networkfolder + "\\Minecraft Servers");
            }
        }
        return true;
    }
}