package org.lostslvtt.darateria;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnerPickaxe extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new SpawnerBreakListener(), this);
        this.getCommand("givepickaxe").setExecutor(new GivePickCmd());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
