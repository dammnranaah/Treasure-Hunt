package com.dammnranaah.treasurehunt.managers;

import com.dammnranaah.treasurehunt.TreasureHunt;
import com.dammnranaah.treasurehunt.models.TreasureChest;
import com.dammnranaah.treasurehunt.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ChestManager {

    private final TreasureHunt plugin;
    private final Map<UUID, TreasureChest> activeChests;
    private final File chestsFile;
    private FileConfiguration chestsConfig;

    public ChestManager(TreasureHunt plugin) {
        this.plugin = plugin;
        this.activeChests = new HashMap<>();
        this.chestsFile = new File(plugin.getDataFolder(), "chests.yml");
        loadChests();
    }

    private void loadChests() {
        if (!chestsFile.exists()) {
            try {
                chestsFile.getParentFile().mkdirs();
                chestsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create chests.yml", e);
            }
        }

        chestsConfig = YamlConfiguration.loadConfiguration(chestsFile);
        
        if (chestsConfig.contains("chests")) {
            for (String key : chestsConfig.getConfigurationSection("chests").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String worldName = chestsConfig.getString("chests." + key + ".world");
                double x = chestsConfig.getDouble("chests." + key + ".x");
                double y = chestsConfig.getDouble("chests." + key + ".y");
                double z = chestsConfig.getDouble("chests." + key + ".z");
                String tier = chestsConfig.getString("chests." + key + ".tier");
                long spawnTime = chestsConfig.getLong("chests." + key + ".spawn-time");
                
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    TreasureChest chest = new TreasureChest(uuid, location, tier, spawnTime);
                    activeChests.put(uuid, chest);
                }
            }
            
            plugin.getLogger().info("Loaded " + activeChests.size() + " treasure chests from storage");
        }
    }

    public void saveChests() {
        chestsConfig.set("chests", null);
        
        for (Map.Entry<UUID, TreasureChest> entry : activeChests.entrySet()) {
            UUID uuid = entry.getKey();
            TreasureChest chest = entry.getValue();
            
            String path = "chests." + uuid.toString();
            chestsConfig.set(path + ".world", chest.getLocation().getWorld().getName());
            chestsConfig.set(path + ".x", chest.getLocation().getX());
            chestsConfig.set(path + ".y", chest.getLocation().getY());
            chestsConfig.set(path + ".z", chest.getLocation().getZ());
            chestsConfig.set(path + ".tier", chest.getTier());
            chestsConfig.set(path + ".spawn-time", chest.getSpawnTime());
        }
        
        try {
            chestsConfig.save(chestsFile);
            plugin.getLogger().info("Saved " + activeChests.size() + " treasure chests to storage");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save chests.yml", e);
        }
    }

    public int spawnChests(int count) {
        int maxChests = plugin.getConfigManager().getMaxChests();
        int currentChests = activeChests.size();
        int chestsToSpawn = Math.min(count, maxChests - currentChests);
        
        if (chestsToSpawn <= 0) {
            plugin.getLogger().info("Cannot spawn more chests, maximum limit reached");
            return 0;
        }
        
        List<String> enabledWorldNames = plugin.getConfigManager().getEnabledWorlds();
        List<World> enabledWorlds = enabledWorldNames.stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (enabledWorlds.isEmpty()) {
            plugin.getLogger().warning("No valid worlds found for chest spawning");
            return 0;
        }
        
        int spawned = 0;
        for (int i = 0; i < chestsToSpawn; i++) {
            World world = enabledWorlds.get(ThreadLocalRandom.current().nextInt(enabledWorlds.size()));
            Location location = findSuitableLocation(world);
            
            if (location != null) {
                String tier = determineTier();
                spawnChest(location, tier);
                spawned++;
            }
        }
        
        return spawned;
    }

    private Location findSuitableLocation(World world) {
        int minDistance = plugin.getConfigManager().getMinDistance();
        int maxDistance = plugin.getConfigManager().getMaxDistance();
        int minY = plugin.getConfigManager().getMinY();
        int maxY = plugin.getConfigManager().getMaxY();
        boolean requireSolidGround = plugin.getConfigManager().requireSolidGround();
        
        Location spawnPoint = world.getSpawnLocation();
        
        for (int attempt = 0; attempt < 50; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = minDistance + ThreadLocalRandom.current().nextDouble() * (maxDistance - minDistance);
            
            int x = (int) (spawnPoint.getX() + distance * Math.cos(angle));
            int z = (int) (spawnPoint.getZ() + distance * Math.sin(angle));
            
            int y = findSuitableY(world, x, z, minY, maxY, requireSolidGround);
            
            if (y != -1) {
                Location location = new Location(world, x, y, z);
                
                if (isSuitableForChest(location)) {
                    return location;
                }
            }
        }
        
        plugin.getLogger().warning("Could not find a suitable location for chest in world: " + world.getName());
        return null;
    }

    private int findSuitableY(World world, int x, int z, int minY, int maxY, boolean requireSolidGround) {
        if (requireSolidGround) {

            for (int y = maxY; y >= minY; y--) {
                Block block = world.getBlockAt(x, y, z);
                Block blockAbove = world.getBlockAt(x, y + 1, z);
                
                if (block.getType().isSolid() && blockAbove.getType().isAir()) {
                    return y + 1;
                }
            }
        } else {
            return minY + ThreadLocalRandom.current().nextInt(maxY - minY + 1);
        }
        
        return -1;
    }
    private boolean isSuitableForChest(Location location) {
        Block block = location.getBlock();
        
        if (!block.getType().isAir()) {
            return false;
        }
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block nearbyBlock = location.clone().add(x, 0, z).getBlock();
                if (nearbyBlock.getType() == Material.CHEST) {
                    return false; 
                }
            }
        }
        
        return true;
    }

    private String determineTier() {
        Map<String, Integer> tierChances = new HashMap<>();
        tierChances.put("common", 60);
        tierChances.put("uncommon", 30);
        tierChances.put("rare", 8);
        tierChances.put("epic", 2);
        
        if (plugin.getConfig().contains("loot.tiers")) {
            for (String tier : plugin.getConfig().getConfigurationSection("loot.tiers").getKeys(false)) {
                int chance = plugin.getConfig().getInt("loot.tiers." + tier + ".chance", 0);
                if (chance > 0) {
                    tierChances.put(tier, chance);
                }
            }
        }
        
        int totalChance = tierChances.values().stream().mapToInt(Integer::intValue).sum();
        
        int random = ThreadLocalRandom.current().nextInt(totalChance);
        
        int currentSum = 0;
        for (Map.Entry<String, Integer> entry : tierChances.entrySet()) {
            currentSum += entry.getValue();
            if (random < currentSum) {
                return entry.getKey();
            }
        }
        

        return "common";
    }


    public UUID spawnChest(Location location, String tier) {
        Block block = location.getBlock();
        block.setType(Material.CHEST);
        
        UUID chestId = UUID.randomUUID();
        TreasureChest treasureChest = new TreasureChest(chestId, location, tier, System.currentTimeMillis());
        activeChests.put(chestId, treasureChest);
        

        Chest chest = (Chest) block.getState();
        plugin.getLootManager().fillChest(chest.getInventory(), tier);
        

        if (plugin.getConfigManager().isDespawnEnabled()) {
            int despawnTime = plugin.getConfigManager().getDespawnTime();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (activeChests.containsKey(chestId)) {
                    removeChest(chestId);
                    plugin.getLogger().info("Treasure chest despawned due to timeout");
                    

                    if (plugin.getConfigManager().isBroadcastSpawnEnabled()) {
                        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("chest-despawn"));
                    }
                }
            }, despawnTime * 20L * 60L);
        }
        
        plugin.getLogger().info("Spawned a " + tier + " treasure chest at " + LocationUtils.formatLocation(location));
        return chestId;
    }


    public void removeChest(UUID chestId) {
        TreasureChest chest = activeChests.get(chestId);
        if (chest != null) {
            Location location = chest.getLocation();
            Block block = location.getBlock();
            
            if (block.getType() == Material.CHEST) {
                block.setType(Material.AIR);
            }
            
            activeChests.remove(chestId);
        }
    }


    public TreasureChest getChestAt(Location location) {
        for (TreasureChest chest : activeChests.values()) {
            if (LocationUtils.isSameBlock(chest.getLocation(), location)) {
                return chest;
            }
        }
        return null;
    }


    public TreasureChest getNearestChest(Player player) {
        if (activeChests.isEmpty()) {
            return null;
        }
        
        Location playerLocation = player.getLocation();
        TreasureChest nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (TreasureChest chest : activeChests.values()) {

            if (!chest.getLocation().getWorld().equals(playerLocation.getWorld())) {
                continue;
            }
            
            double distance = chest.getLocation().distance(playerLocation);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = chest;
            }
        }
        
        return nearest;
    }


    public Map<UUID, TreasureChest> getActiveChests() {
        return activeChests;
    }


    public void removeAllChests() {
        for (UUID chestId : new ArrayList<>(activeChests.keySet())) {
            removeChest(chestId);
        }
        
        plugin.getLogger().info("Removed all treasure chests");
    }
}
