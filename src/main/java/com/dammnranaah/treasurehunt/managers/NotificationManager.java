package com.dammnranaah.treasurehunt.managers;

import com.dammnranaah.treasurehunt.TreasureHunt;
import com.dammnranaah.treasurehunt.models.TreasureChest;
import com.dammnranaah.treasurehunt.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages player notifications for the TreasureHunt plugin
 */
public class NotificationManager {

    private final TreasureHunt plugin;
    private final Map<Integer, BukkitTask> countdownTasks;

    public NotificationManager(TreasureHunt plugin) {
        this.plugin = plugin;
        this.countdownTasks = new HashMap<>();
    }

    /**
     * Send a notification to all players
     * @param message Message to send
     */
    public void broadcastMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Send a notification about a chest spawn
     * @param chest Spawned chest
     * @param showLocation Whether to include location hint
     */
    public void notifyChestSpawn(TreasureChest chest, boolean showLocation) {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        // Send general spawn message
        broadcastMessage(plugin.getConfigManager().getMessage("spawn"));
        
        // Send location hint if enabled
        if (showLocation && chest != null) {
            String areaName = LocationUtils.getAreaName(chest.getLocation());
            String message = plugin.getConfigManager().getMessage("spawn-location-hint")
                    .replace("%location%", areaName);
            
            broadcastMessage(message);
        }
    }

    /**
     * Send a notification about a chest being found
     * @param player Player who found the chest
     * @param chest Chest that was found
     */
    public void notifyChestFound(Player player, TreasureChest chest) {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        String message = plugin.getConfigManager().getMessage("chest-found")
                .replace("%player%", player.getName());
        
        broadcastMessage(message);
    }

    /**
     * Send a notification about chests despawning
     */
    public void notifyChestDespawn() {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        broadcastMessage(plugin.getConfigManager().getMessage("chest-despawn"));
    }

    /**
     * Schedule countdown notifications for the next chest spawn
     * @param delayMinutes Minutes until the next spawn
     */
    public void scheduleCountdowns(int delayMinutes) {
        if (!plugin.getConfigManager().isShowCountdownEnabled()) {
            return;
        }
        
        // Cancel any existing countdown tasks
        cancelCountdowns();
        
        // Schedule new countdown tasks
        for (int countdown : plugin.getConfigManager().getCountdownTimes()) {
            if (countdown < delayMinutes) {
                int timeUntilNotification = (delayMinutes - countdown) * 60 * 20; // Convert to ticks
                
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String message = plugin.getConfigManager().getMessage("countdown")
                            .replace("%time%", String.valueOf(countdown));
                    
                    broadcastMessage(message);
                }, timeUntilNotification);
                
                countdownTasks.put(countdown, task);
            }
        }
    }

    /**
     * Cancel all scheduled countdown tasks
     */
    public void cancelCountdowns() {
        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }
        
        countdownTasks.clear();
    }

    /**
     * Send a message to a specific player
     * @param player Player to send message to
     * @param message Message to send
     */
    public void sendPlayerMessage(Player player, String message) {
        if (player != null && message != null && !message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
