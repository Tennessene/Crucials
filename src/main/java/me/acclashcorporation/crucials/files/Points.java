package me.acclashcorporation.crucials.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Points {

    private static File file;
    private static FileConfiguration pointsConfig;

    public static void setup(File dataDir) {
        file = new File(dataDir, "points.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Unable to create points.yml");
            }
        }
        pointsConfig = YamlConfiguration.loadConfiguration(file);
    }
    public static FileConfiguration get() {
        return pointsConfig;
    }

    public static void save() {
        try {
            pointsConfig.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save points.yml");
        }
    }

    public static void reload() {
        pointsConfig = YamlConfiguration.loadConfiguration(file);
    }
}
