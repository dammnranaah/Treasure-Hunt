package com.dammnranaah.treasurehunt.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.dammnranaah.treasurehunt.TreasureHunt;

import java.io.File;
import java.util.List;

public class ConfigManager {

    private final TreasureHunt plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(TreasureHunt plugin) {
        this.plugin = plugin;
    }

    /**
     * Load the configuration file
     */
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration loaded successfully");
    }

    /**
     * Reload the configuration file
     */
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Configuration reloaded successfully");
    }

    /**
     * Check if auto spawn is enabled
     * @return true if auto spawn is enabled
     */
    public boolean isAutoSpawnEnabled() {
        return config.getBoolean("chest-spawn.auto-spawn", true);
    }

    /**
     * Get the spawn interval in minutes
     * @return spawn interval
     */
    public int getSpawnInterval() {
        return config.getInt("chest-spawn.interval", 30);
    }

    /**
     * Get the number of chests to spawn each time
     * @return number of chests
     */
    public int getChestCount() {
        return config.getInt("chest-spawn.count", 3);
    }

    /**
     * Get the maximum number of chests that can exist at once
     * @return maximum number of chests
     */
    public int getMaxChests() {
        return config.getInt("chest-spawn.max-chests", 10);
    }

    /**
     * Check if chest despawn is enabled
     * @return true if chest despawn is enabled
     */
    public boolean isDespawnEnabled() {
        return config.getBoolean("chest-spawn.despawn-enabled", true);
    }

    /**
     * Get the time in minutes before unlooted chests despawn
     * @return despawn time
     */
    public int getDespawnTime() {
        return config.getInt("chest-spawn.despawn-time", 60);
    }

    /**
     * Get the list of worlds where chests can spawn
     * @return list of world names
     */
    public List<String> getEnabledWorlds() {
        return config.getStringList("chest-spawn.enabled-worlds");
    }

    /**
     * Get the minimum distance from spawn point
     * @return minimum distance
     */
    public int getMinDistance() {
        return config.getInt("chest-spawn.min-distance", 100);
    }

    /**
     * Get the maximum distance from spawn point
     * @return maximum distance
     */
    public int getMaxDistance() {
        return config.getInt("chest-spawn.max-distance", 2000);
    }

    /**
     * Get the minimum Y level for chest spawning
     * @return minimum Y level
     */
    public int getMinY() {
        return config.getInt("chest-spawn.min-y", 40);
    }

    /**
     * Get the maximum Y level for chest spawning
     * @return maximum Y level
     */
    public int getMaxY() {
        return config.getInt("chest-spawn.max-y", 120);
    }

    /**
     * Check if chests should only spawn on solid blocks
     * @return true if chests should only spawn on solid blocks
     */
    public boolean requireSolidGround() {
        return config.getBoolean("chest-spawn.require-solid-ground", true);
    }

    /**
     * Check if broadcast spawn messages are enabled
     * @return true if broadcast spawn messages are enabled
     */
    public boolean isBroadcastSpawnEnabled() {
        return config.getBoolean("notifications.broadcast-spawn", true);
    }

    /**
     * Check if countdown messages are enabled
     * @return true if countdown messages are enabled
     */
    public boolean isShowCountdownEnabled() {
        return config.getBoolean("notifications.show-countdown", true);
    }

    /**
     * Get the countdown intervals in minutes
     * @return list of countdown intervals
     */
    public List<Integer> getCountdownTimes() {
        return config.getIntegerList("notifications.countdown-times");
    }

    /**
     * Get a message from the config
     * @param path Path to the message
     * @return Formatted message
     */
    public String getMessage(String path) {
        String message = config.getString("notifications.messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Get the cooldown for the locate command in seconds
     * @return locate command cooldown
     */
    public int getLocateCooldown() {
        return config.getInt("commands.locate-cooldown", 300);
    }

    /**
     * Check if a penalty should be applied for using the locate command
     * @return true if a penalty should be applied
     */
    public boolean isLocatePenaltyEnabled() {
        return config.getBoolean("commands.locate-penalty", true);
    }

    /**
     * Check if loot quality should be reduced when using locate
     * @return true if loot quality should be reduced
     */
    public boolean reduceLocateLootQuality() {
        return config.getBoolean("commands.locate-penalty-options.reduce-loot-quality", true);
    }

    /**
     * Check if locate usage should be broadcast
     * @return true if locate usage should be broadcast
     */
    public boolean broadcastLocateUsage() {
        return config.getBoolean("commands.locate-penalty-options.broadcast-usage", true);
    }

    /**
     * Check if guards/traps are enabled
     * @return true if guards/traps are enabled
     */
    public boolean areGuardsEnabled() {
        return config.getBoolean("guards.enabled", true);
    }

    /**
     * Get the chance that a chest will have a guard
     * @return guard chance
     */
    public int getGuardChance() {
        return config.getInt("guards.chance", 50);
    }

    /**
     * Get the storage type
     * @return storage type (YAML or MYSQL)
     */
    public String getStorageType() {
        return config.getString("storage.type", "YAML").toUpperCase();
    }

    /**
     * Check if debug mode is enabled
     * @return true if debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
}
