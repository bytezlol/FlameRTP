package de.flamesmp;

import com.tcoded.folialib.FoliaLib;
import de.flamesmp.api.*;
import de.flamesmp.cache.*;
import de.flamesmp.command.*;
import de.flamesmp.config.*;
import de.flamesmp.listener.*;
import de.flamesmp.manager.*;
import de.flamesmp.storage.*;
import de.flamesmp.tasks.*;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class FlameRTP extends JavaPlugin {

    @Getter
    private static FlameRTP instance;

    private FoliaLib foliaLib;

    private RTPConfig rtpConfig;

    private LocationCacheStorage locationCacheStorage;

    private CooldownCache cooldownCache;

    private MessageManager messageManager;
    private CooldownManager cooldownManager;
    private EconomyManager economyManager;
    private LocationFinderManager locationFinderManager;
    private RTPManager rtpManager;

    private LocationPrecacheTask locationPrecacheTask;

    private FlameRTPAPI api;

    public FlameRTP() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.foliaLib = new FoliaLib(this);

        this.rtpConfig = new RTPConfig();
        this.rtpConfig.load();

        this.locationCacheStorage = new LocationCacheStorage();
        this.locationCacheStorage.connect();

        this.cooldownCache = new CooldownCache();

        this.messageManager = new MessageManager();
        this.cooldownManager = new CooldownManager();
        this.economyManager = new EconomyManager();
        this.locationFinderManager = new LocationFinderManager();
        this.rtpManager = new RTPManager();

        this.api = new FlameRTPAPI();

        new RTPCommand();
        new RTPAdminCommand();
        new RTPReloadCommand();

        new PlayerMoveListener();
        new PlayerDamageListener();
        new PlayerQuitListener();

        this.locationPrecacheTask = new LocationPrecacheTask();
        this.locationPrecacheTask.start();
    }

    @Override
    public void onDisable() {
        if (locationPrecacheTask != null) locationPrecacheTask.stop();
        if (rtpManager != null) rtpManager.shutdown();
        if (locationFinderManager != null) locationFinderManager.shutdown();
        if (locationCacheStorage != null) locationCacheStorage.disconnect();
        if (foliaLib != null) foliaLib.getScheduler().cancelAllTasks();
    }
}