package org.milanhdev.librarianPlus;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class LibrarianPlus extends JavaPlugin {
    private Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        this.log.info("[LibrarianPlus] Loading version " + this.getDescription().getVersion() + "...");

        GUICommand guiCommand = new GUICommand(this);
        getCommand("librarianmenu").setExecutor(guiCommand);
        getServer().getPluginManager().registerEvents(new GUIListener(guiCommand), this);
        getServer().getPluginManager().registerEvents(new VillagerTrade(this, guiCommand), this);

        this.log.info("[LibrarianPlus] Plugin loaded and enabled.");
    }

    @Override
    public void onDisable() {
        log.info("[LibrarianPlus] Disabling plugin...");
        log.info("[LibrarianPlus] Plugin disabled!");
    }
}
