package me.acclashcorporation.crucials.commands;

import me.acclashcorporation.crucials.Crucials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveServer implements CommandExecutor {

    private final Crucials plugin;

    private final String SUBCOMMAND_CONNECT = "connect";
    private final String SUBCOMMAND_DISCONNECT = "disconnect";
    private final String SUBCOMMAND_PUSH = "push";
    private final String SUBCOMMAND_UPDATE = "update";

    public SaveServer(Crucials plugin) {
        this.plugin = plugin;
    }

    /**
     * Acceptable args:
     * - {@link #SUBCOMMAND_CONNECT} - create local repository linked to remote git server and enables consecutive saves
     * - {@link #SUBCOMMAND_DISCONNECT} - deletes link to remote git server and disables consecutive saves
     * - {@link #SUBCOMMAND_PUSH} - assumes already connected. Creates a new commit in the local repo and pushes it to remote
     * - {@link #SUBCOMMAND_UPDATE} - attempts to pull data from remote git repository and write to local repository
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        refreshDefaultCredentials();

        if (args.length == 0) {
            // show usage
            return false;
        } else  {
            return switch (args[0]) {
                case SUBCOMMAND_CONNECT ->  connectToGit();
                case SUBCOMMAND_DISCONNECT -> disconnectFromGit();
                case SUBCOMMAND_PUSH -> pushToGit(args);
                case SUBCOMMAND_UPDATE -> updateFromGit();
                default -> false;
            };
        }
    }

    /**
     * Creates a local repository and links it to remote git url.
     * The repository remains in the filesystem even between server restarts,
     * so it should only be required to run it once.
     */
    private boolean connectToGit() {
        try( Git git = Git.wrap(new RepositoryBuilder()
                .setWorkTree(getLocalRepository())
                .build()) ){
            if(git.getRepository().getDirectory().exists()) {
                return false;
            } else {
                git.getRepository().create();
            }
            git.remoteAdd()
                    .setUri(new URIish(getGitUrl()))
                    .setName("origin")
                    .call();
            return true;
        } catch (IOException | URISyntaxException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Destroys GIT_DIR in local repository, effectively unlinking if from remote git server
     */
    private boolean disconnectFromGit() {
        deleteRecursively(new File(getLocalRepository(), ".git"));
        return true;
    }


    private boolean pushToGit(String[] commandArgs) {
        String message = Arrays.stream(commandArgs).skip(2).collect(Collectors.joining(" "));
        if(message.isBlank()) {
            message = "Update " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        try(Git git = Git.open(getLocalRepository())){
            git.add()
                    .addFilepattern(".")
                    .call();
            git.commit()
                    .setMessage(message)
                    .setAuthor(getGitAuthorName(), getGitAuthorEmail())
                    .setCommitter(getGitAuthorName(), getGitAuthorEmail())
                    .setSign(false)
                    .call();
            git.push()
                    .setPushAll()
                    .setForce(true)
                    .call();
            return true;
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pulls changes from Git to local workspace.
     * WARNING: potentially requires server to restart for changes to be applied.
     */
    private boolean updateFromGit() {
        try (Git git = Git.open(getLocalRepository())){
            git.pull()
                    .call();
            return true;
        } catch (IOException | GitAPIException e) {
            return false;
        }
    }


    private String getGitUrl() {
        return plugin.getConfig().getString("git.repository.url");
    }

    private File getLocalRepository() {
        String localRepository = plugin.getConfig().getString("git.local.directory");
        if (localRepository == null || localRepository.isBlank()) {
            return plugin.getDataFolder().getParentFile().getParentFile();
        } else {
            File localRepositoryDir = new File(localRepository);
            if (!localRepositoryDir.exists()) {
                try {
                    Files.createDirectories(localRepositoryDir.toPath());
                } catch (IOException e) {
                    throw new RuntimeException("Could not create repository: " + localRepository, e);
                }
            }
            return localRepositoryDir;
        }
    }

    private String getGitAuthorName() {
        return plugin.getConfig().getString("git.author.name");
    }

    private String getGitAuthorEmail() {
        return plugin.getConfig().getString("git.author.email");
    }

    private String getGitUserName() {
        return plugin.getConfig().getString("git.username");
    }

    private String getGitPassword() {
        return plugin.getConfig().getString("git.password");
    }

    private File getSshDirectory() {
        String sshDirectory = plugin.getConfig().getString("git.ssh.dir");
        if (sshDirectory == null || sshDirectory.isBlank()) {
            return new File(FS.detect().userHome(), ".ssh");
        } else {
            return new File(sshDirectory);
        }
    }

    private void refreshDefaultCredentials() {
        CredentialsProvider.setDefault(
                new UsernamePasswordCredentialsProvider(getGitUserName(), getGitPassword()));
        SshSessionFactory.setInstance(
                new SshdSessionFactoryBuilder()
                        .setHomeDirectory(FS.DETECTED.userHome())
                        .setSshDirectory(getSshDirectory())
                        .build(null));
    }

    private void deleteRecursively(File fileOrDirectory) {
        if(fileOrDirectory.exists()) {
            try(Stream<Path> content = Files.walk(fileOrDirectory.toPath())) {
                content.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}