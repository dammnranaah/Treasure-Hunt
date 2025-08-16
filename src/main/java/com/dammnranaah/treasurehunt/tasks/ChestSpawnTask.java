package com.dammnranaah.treasurehunt.tasks;

import com.dammnranaah.treasurehunt.TreasureHunt;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ChestSpawnTask extends BukkitRunnable {

    private final TreasureHunt plugin;

    public ChestSpawnTask(TreasureHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int chestCount = plugin.getConfigManager().getChestCount();
        
        int spawned = plugin.getChestManager().spawnChests(chestCount);
        
        if (spawned > 0 && plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("spawn"));
        }
        
        plugin.getLogger().info("Spawned " + spawned + " treasure chests");
    }
    
    public void scheduleCountdowns() {
        if (!plugin.getConfigManager().isShowCountdownEnabled()) {
            return;
        }
        
        int interval = plugin.getConfigManager().getSpawnInterval();
        
        for (int countdown : plugin.getConfigManager().getCountdownTimes()) {
            if (countdown < interval) {
                int delay = (interval - countdown) * 20 * 60;
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String message = plugin.getConfigManager().getMessage("countdown")
                            .replace("%time%", String.valueOf(countdown));
                    Bukkit.broadcastMessage(message);
                }, delay);
            }
        }
    }
}
