package me.acclashcorporation.crucials;

import me.acclashcorporation.crucials.commands.*;
import me.acclashcorporation.crucials.events.BanInventoryListener;
import me.acclashcorporation.crucials.files.Points;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public final class Crucials extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Points.setup();
        Points.get().options().copyDefaults();
        Points.save();
        getCommand("bangui").setExecutor(new BanGUI());
        getCommand("saveserver").setExecutor(new SaveServer(this));
        getCommand("tpplayer").setExecutor(new Teleport());
        getCommand("tpall").setExecutor(new TeleportAll());
        getCommand("setwarp").setExecutor(new SetTeleportWarp(this));
        getCommand("warp").setExecutor(new TeleportWarp(this));
        getServer().getPluginManager().registerEvents(new BanInventoryListener(), this);
        int pluginId = 15051;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new SimplePie("chart_id", () -> "My value"));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}


