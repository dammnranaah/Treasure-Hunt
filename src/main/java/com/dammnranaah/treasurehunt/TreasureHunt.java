package com.dammnranaah.treasurehunt;

impo    @Override
    public void onDisable() {
        if (configManager != null) {
            configManager.saveConfig();
        }
        
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        if (notificationManager != null) {
            notificationManager.cancelAllNotifications();
        }
        
        getLogger().info("TreasureHunt has been disabled!");
    }treasurehunt.commands.CommandHandler;
import com.dammnranaah.treasurehunt.config.ConfigManager;
import com.dammnranaah.treasurehunt.listeners.ChestListener;
import com.dammnranaah.treasurehunt.managers.ChestManager;
import com.dammnranaah.treasurehunt.managers.LootManager;
import com.dammnranaah.treasurehunt.managers.NotificationManager;
import com.dammnranaah.treasurehunt.tasks.ChestSpawnTask;
import org.bukkit.plugin.java.JavaPlugin;


public final class TreasureHunt extends JavaPlugin {

    private static TreasureHunt instance;
    private ConfigManager configManager;
    private ChestManager chestManager;
    private LootManager lootManager;
    private NotificationManager notificationManager;
    private ChestSpawnTask spawnTask;

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        chestManager = new ChestManager(this);
        lootManager = new LootManager(this);
        notificationManager = new NotificationManager(this);
        
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        
        getCommand("treasurehunt").setExecutor(new CommandHandler(this));
        
        if (configManager.isAutoSpawnEnabled()) {
            startSpawnTask();
        }
        
        getLogger().info("TreasureHunt has been enabled!");
    }

    @Override
    public void onDisable() {
        if (chestManager != null) {
            chestManager.saveChests();
        }
        
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        if (notificationManager != null) {
            notificationManager.cancelCountdowns();
        }
        
        getLogger().info("TreasureHunt has been disabled!");
    }
    
    public void startSpawnTask() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        int interval = configManager.getSpawnInterval() * 20 * 60;
        spawnTask = new ChestSpawnTask(this);
        spawnTask.runTaskTimer(this, interval, interval);
        
        getLogger().info("Treasure chest spawn task started with interval: " + 
                configManager.getSpawnInterval() + " minutes");
    }
    
    public void stopSpawnTask() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
            getLogger().info("Treasure chest spawn task stopped");
        }
    }
    
    public static TreasureHunt getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ChestManager getChestManager() {
        return chestManager;
    }
    
    public LootManager getLootManager() {
        return lootManager;
    }
    
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
}
