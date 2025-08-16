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

    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Configuration reloaded successfully");
    }

    public boolean isAutoSpawnEnabled() {
        return config.getBoolean("chest-spawn.auto-spawn", true);
    }


    public int getSpawnInterval() {
        return config.getInt("chest-spawn.interval", 30);
    }


    public int getChestCount() {
        return config.getInt("chest-spawn.count", 3);
    }


    public int getMaxChests() {
        return config.getInt("chest-spawn.max-chests", 10);
    }


    public boolean isDespawnEnabled() {
        return config.getBoolean("chest-spawn.despawn-enabled", true);
    }


    public int getDespawnTime() {
        return config.getInt("chest-spawn.despawn-time", 60);
    }


    public List<String> getEnabledWorlds() {
        return config.getStringList("chest-spawn.enabled-worlds");
    }


    public int getMinDistance() {
        return config.getInt("chest-spawn.min-distance", 100);
    }


    public int getMaxDistance() {
        return config.getInt("chest-spawn.max-distance", 2000);
    }


    public int getMinY() {
        return config.getInt("chest-spawn.min-y", 40);
    }


    public int getMaxY() {
        return config.getInt("chest-spawn.max-y", 120);
    }


    public boolean requireSolidGround() {
        return config.getBoolean("chest-spawn.require-solid-ground", true);
    }


    public boolean isBroadcastSpawnEnabled() {
        return config.getBoolean("notifications.broadcast-spawn", true);
    }


    public boolean isShowCountdownEnabled() {
        return config.getBoolean("notifications.show-countdown", true);
    }


    public List<Integer> getCountdownTimes() {
        return config.getIntegerList("notifications.countdown-times");
    }


    public String getMessage(String path) {
        String message = config.getString("notifications.messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }


    public int getLocateCooldown() {
        return config.getInt("commands.locate-cooldown", 300);
    }


    public boolean isLocatePenaltyEnabled() {
        return config.getBoolean("commands.locate-penalty", true);
    }


    public boolean reduceLocateLootQuality() {
        return config.getBoolean("commands.locate-penalty-options.reduce-loot-quality", true);
    }


    public boolean broadcastLocateUsage() {
        return config.getBoolean("commands.locate-penalty-options.broadcast-usage", true);
    }


    public boolean areGuardsEnabled() {
        return config.getBoolean("guards.enabled", true);
    }


    public int getGuardChance() {
        return config.getInt("guards.chance", 50);
    }


    public String getStorageType() {
        return config.getString("storage.type", "YAML").toUpperCase();
    }


    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
}
