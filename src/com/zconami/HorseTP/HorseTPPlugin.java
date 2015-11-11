package com.zconami.HorseTP;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

import com.zconami.Core.ZconamiPlugin;
import com.zconami.Core.util.Utils;
import com.zconami.HorseTP.listener.ChunkEventListener;
import com.zconami.HorseTP.listener.MountEventListener;
import com.zconami.HorseTP.repository.MountRepository;

public class HorseTPPlugin extends ZconamiPlugin {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String PLUGIN_NAME = "HorseTP";

    private static final Logger log = Logger.getLogger("Minecraft");

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static HorseTPPlugin instance;

    private MountRepository mountRepository;

    private ChunkEventListener chunkEventListener;
    private MountEventListener mountEventListener;

    private HorseTPCommandExecutor commandExecutor;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public HorseTPPlugin() {
        super();

        instance = this;

        this.mountRepository = new MountRepository(this);

        this.chunkEventListener = new ChunkEventListener(mountRepository);
        this.mountEventListener = new MountEventListener(mountRepository);

        this.commandExecutor = new HorseTPCommandExecutor(mountRepository);

    }

    public static HorseTPPlugin getHorseTPPlugin() {
        return instance;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public MountRepository getMountRepository() {
        return this.mountRepository;
    }

    public static FileConfiguration getHorseTPConfig() {
        return Utils.getPluginConfig(PLUGIN_NAME);
    }

    // ===================================
    // IMPLEMENTATION OF JavaPlugin
    // ===================================

    @Override
    public void onEnable() {
        getLogger().info("=== ENABLE START ===");
        this.saveDefaultConfig();
        getLogger().info("Registering command executors...");
        this.getCommand("h").setExecutor(commandExecutor);
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(chunkEventListener, this);
        getServer().getPluginManager().registerEvents(mountEventListener, this);
        getLogger().info("=== ENABLE COMPLETE ===");
    }

    @Override
    public void onDisable() {
        getLogger().info("=== DISABLE START ===");
        getLogger().info("Unloading repositories...");
        mountRepository.unload();
        getLogger().info("Unregistering listeners...");
        HandlerList.unregisterAll(chunkEventListener);
        HandlerList.unregisterAll(mountEventListener);
        getLogger().info("=== DISABLE COMPLETE ===");
    }

}
