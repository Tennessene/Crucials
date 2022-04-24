package me.acclashcorporation.crucialplugin;

import me.acclashcorporation.crucialplugin.commands.*;
import me.acclashcorporation.crucialplugin.events.BanInventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrucialPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getCommand("bangui").setExecutor(new BanGUI());
        getCommand("saveserver").setExecutor(new SaveServer());
        getCommand("tpplayer").setExecutor(new Teleport());
        getCommand("tpall").setExecutor(new TeleportAll());
        getCommand("setpoint").setExecutor(new SetTeleportPoint(this));
        getCommand("tppoint").setExecutor(new TeleportPoint(this));
        getServer().getPluginManager().registerEvents(new BanInventoryListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}


