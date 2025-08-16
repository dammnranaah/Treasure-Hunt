package com.dammnranaah.treasurehunt;

import com.dammnranaah.treasurehunt.commands.CommandHandler;
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
        // Set instance
        instance = this;
        
        // Initialize config
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialize managers
        chestManager = new ChestManager(this);
        lootManager = new LootManager(this);
        notificationManager = new NotificationManager(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        
        // Register commands
        getCommand("treasurehunt").setExecutor(new CommandHandler(this));
        
        // Start scheduled tasks if auto-spawn is enabled
        if (configManager.isAutoSpawnEnabled()) {
            startSpawnTask();
        }
        
        getLogger().info("TreasureHunt has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data
        if (chestManager != null) {
            chestManager.saveChests();
        }
        
        // Cancel tasks
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        // Cancel notifications
        if (notificationManager != null) {
            notificationManager.cancelCountdowns();
        }
        
        getLogger().info("TreasureHunt has been disabled!");
    }
    
    /**
     * Start the automatic chest spawn task
     */
    public void startSpawnTask() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        int interval = configManager.getSpawnInterval() * 20 * 60; // Convert minutes to ticks
        spawnTask = new ChestSpawnTask(this);
        spawnTask.runTaskTimer(this, interval, interval);
        
        getLogger().info("Treasure chest spawn task started with interval: " + 
                configManager.getSpawnInterval() + " minutes");
    }
    
    /**
     * Stop the automatic chest spawn task
     */
    public void stopSpawnTask() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
            getLogger().info("Treasure chest spawn task stopped");
        }
    }
    
    /**
     * Get the instance of the plugin
     * @return Plugin instance
     */
    public static TreasureHunt getInstance() {
        return instance;
    }
    
    /**
     * Get the config manager
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Get the chest manager
     * @return ChestManager instance
     */
    public ChestManager getChestManager() {
        return chestManager;
    }
    
    /**
     * Get the loot manager
     * @return LootManager instance
     */
    public LootManager getLootManager() {
        return lootManager;
    }
    
    /**
     * Get the notification manager
     * @return NotificationManager instance
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
}
