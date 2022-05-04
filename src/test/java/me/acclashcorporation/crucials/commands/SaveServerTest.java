package me.acclashcorporation.crucials.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.acclashcorporation.crucials.Crucials;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SaveServerTest {
    private static Crucials crucials;
    private static PlayerMock player;

    private static final Logger logger = LoggerFactory.getLogger(SaveServerTest.class);
    private static final Properties testProperties = new Properties();

    @TempDir
    Path testedRepository;

    @TempDir
    Path verificationRepository;

    @TempDir
    static Path localSshConfig;

    @BeforeAll
    static void setup() {
        Assumptions.assumeTrue(loadTestProperties());
        System.setProperty("bstats.relocatecheck", "false");
        ServerMock server = MockBukkit.mock();
        crucials = MockBukkit.load(Crucials.class);
        server.setPlayers(0);
        player = server.addPlayer();
        crucials.reloadConfig();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Order(1)
    @ParameterizedTest
    @EnumSource(ConnectionType.class)
    void testConnectUpdateAndPush(ConnectionType connectionType) throws GitAPIException, IOException {
        configureFor(connectionType);
        Path file = addRandomFileToLocalRepository();
        player.performCommand("saveserver connect");
        player.performCommand("saveserver update");
        player.performCommand("saveserver push");
        try(Git git = cloneTestRepo()) {
            assertThat(git.getRepository().getWorkTree())
                    .isDirectoryContaining("glob:**" + file.getFileName().toString());
        }
    }

    @Order(2)
    @ParameterizedTest
    @EnumSource(ConnectionType.class)
    void testPushWithoutUpdatingOverwritesRepoHistory(ConnectionType connectionType) throws GitAPIException {
        configureFor(connectionType);
        player.performCommand("saveserver connect");
        player.performCommand("saveserver push");
        try(Git git = cloneTestRepo()) {
            assertThat(git.getRepository().getWorkTree())
                    .isDirectoryNotContaining("glob:**RANDOM");
            assertThat(git.log().call()).hasSize(1);
        }
    }

    @Order(3)
    @ParameterizedTest
    @EnumSource(ConnectionType.class)
    void testConnectAndPushWithCustomMessage(ConnectionType connectionType) throws IOException, GitAPIException {
        configureFor(connectionType);
        addRandomFileToLocalRepository();
        player.performCommand("saveserver connect");
        player.performCommand("saveserver update");
        player.performCommand("saveserver push using my custom message");
        try(Git git = cloneTestRepo()) {
            assertThat(git.log().call()).anyMatch(commit -> commit.getFullMessage().equals("my custom message"));
        }
    }

    @Order(4)
    @ParameterizedTest
    @EnumSource(ConnectionType.class)
    void testMultipleConnectAttempts(ConnectionType connectionType) {
        configureFor(connectionType);
        assertThatCode(() -> {
            player.performCommand("saveserver connect");
            player.performCommand("saveserver connect");
            player.performCommand("saveserver disconnect");
            player.performCommand("saveserver disconnect");
        }).doesNotThrowAnyException();
    }

    @Test
    void testMissingSubcommand() {
        assertThat(player.performCommand("saveserver")).isFalse();
    }

    @Test
    void testUnsupportedSubcommand() {
        assertThat(player.performCommand("saveserver unknownSubcommand")).isFalse();
    }


    private enum ConnectionType {
        SSH, HTTPS;
    }

    private void configureFor(ConnectionType connectionType) {
        switch (connectionType) {
            case SSH -> {
                crucials.getConfig().set("git.local.directory", testedRepository);
                crucials.getConfig().set("git.ssh.dir", localSshConfig);
                crucials.getConfig().set("git.repository.url", getTestSshRepoUrl());
            }
            case HTTPS -> {
                crucials.getConfig().set("git.local.directory", testedRepository);
                crucials.getConfig().set("git.repository.url", getTestHttpsRepoUrl());
                crucials.getConfig().set("git.username", getTestRepoUsername());
                crucials.getConfig().set("git.password", getTestRepoPassword());
            }
        }
    }

    private Path addRandomFileToLocalRepository() throws IOException {
        Path randomFile = Files.createTempFile(testedRepository, "", ".RANDOM");
        Files.write(randomFile, UUID.randomUUID().toString().getBytes());
        assertThat(testedRepository).isNotEmptyDirectory();
        return randomFile;
    }

    private Git cloneTestRepo() throws GitAPIException {
        return Git.cloneRepository()
                .setDirectory(verificationRepository.toFile())
                .setURI(getTestHttpsRepoUrl())
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(getTestRepoUsername(), getTestRepoPassword()))
                .call();
    }

    private String getTestHttpsRepoUrl () {
        return testProperties.getProperty("github-https-url");
    }

    private String getTestSshRepoUrl () {
        return testProperties.getProperty("github-ssh-url");
    }

    private String getTestRepoUsername () {
        return testProperties.getProperty("github-email");
    }

    private String getTestRepoPassword () {
        return testProperties.getProperty("github-token");
    }

    private static String getTestRepoUserPrivateKey () {
        return testProperties.getProperty("github-ssh-private-key");
    }

    private static String getTestRepoSshConfigFileContent() {
        return testProperties.getProperty("github-ssh-config");
    }

    private static boolean loadTestProperties() {
        try {
            testProperties.load(ClassLoader.getSystemResourceAsStream("test-git.properties"));
            // Extract minimal ssh configuration: id file + config with hostname checking disabled
            Files.writeString(localSshConfig.resolve("id_rsa"), getTestRepoUserPrivateKey());
            Files.writeString(localSshConfig.resolve("config"), getTestRepoSshConfigFileContent());
            Files.writeString(localSshConfig.resolve("known_hosts"), "");
            if(testProperties.isEmpty()) {
                throw new IllegalStateException("Properties file empty");
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("Create test-git.properties not found under src/test/resources");
            logger.error("Cannot execute tests on actual repository");
            return false;
        }
    }
}