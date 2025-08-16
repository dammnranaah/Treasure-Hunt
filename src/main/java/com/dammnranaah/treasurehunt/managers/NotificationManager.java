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

public class NotificationManager {

    private final TreasureHunt plugin;
    private final Map<Integer, BukkitTask> countdownTasks;

    public NotificationManager(TreasureHunt plugin) {
        this.plugin = plugin;
        this.countdownTasks = new HashMap<>();
    }
    public void broadcastMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void notifyChestSpawn(TreasureChest chest, boolean showLocation) {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        broadcastMessage(plugin.getConfigManager().getMessage("spawn"));
        

        if (showLocation && chest != null) {
            String areaName = LocationUtils.getAreaName(chest.getLocation());
            String message = plugin.getConfigManager().getMessage("spawn-location-hint")
                    .replace("%location%", areaName);
            
            broadcastMessage(message);
        }
    }


    public void notifyChestFound(Player player, TreasureChest chest) {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        String message = plugin.getConfigManager().getMessage("chest-found")
                .replace("%player%", player.getName());
        
        broadcastMessage(message);
    }


    public void notifyChestDespawn() {
        if (!plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            return;
        }
        
        broadcastMessage(plugin.getConfigManager().getMessage("chest-despawn"));
    }


    public void scheduleCountdowns(int delayMinutes) {
        if (!plugin.getConfigManager().isShowCountdownEnabled()) {
            return;
        }
        

        cancelCountdowns();
        

        for (int countdown : plugin.getConfigManager().getCountdownTimes()) {
            if (countdown < delayMinutes) {
                int timeUntilNotification = (delayMinutes - countdown) * 60 * 20;
                
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String message = plugin.getConfigManager().getMessage("countdown")
                            .replace("%time%", String.valueOf(countdown));
                    
                    broadcastMessage(message);
                }, timeUntilNotification);
                
                countdownTasks.put(countdown, task);
            }
        }
    }


    public void cancelCountdowns() {
        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }
        
        countdownTasks.clear();
    }


    public void sendPlayerMessage(Player player, String message) {
        if (player != null && message != null && !message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
