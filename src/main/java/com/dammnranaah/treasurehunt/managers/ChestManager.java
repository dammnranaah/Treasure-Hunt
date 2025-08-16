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

    /**
     * Load chests from storage
     */
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

    /**
     * Save chests to storage
     */
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

    /**
     * Spawn treasure chests in the world
     * @param count Number of chests to spawn
     * @return Number of chests successfully spawned
     */
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

    /**
     * Find a suitable location for a chest
     * @param world World to search in
     * @return Suitable location or null if none found
     */
    private Location findSuitableLocation(World world) {
        int minDistance = plugin.getConfigManager().getMinDistance();
        int maxDistance = plugin.getConfigManager().getMaxDistance();
        int minY = plugin.getConfigManager().getMinY();
        int maxY = plugin.getConfigManager().getMaxY();
        boolean requireSolidGround = plugin.getConfigManager().requireSolidGround();
        
        Location spawnPoint = world.getSpawnLocation();
        
        // Try up to 50 times to find a suitable location
        for (int attempt = 0; attempt < 50; attempt++) {
            // Generate random coordinates within the specified range
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = minDistance + ThreadLocalRandom.current().nextDouble() * (maxDistance - minDistance);
            
            int x = (int) (spawnPoint.getX() + distance * Math.cos(angle));
            int z = (int) (spawnPoint.getZ() + distance * Math.sin(angle));
            
            // Find a suitable Y coordinate
            int y = findSuitableY(world, x, z, minY, maxY, requireSolidGround);
            
            if (y != -1) {
                Location location = new Location(world, x, y, z);
                
                // Check if the location is suitable for a chest
                if (isSuitableForChest(location)) {
                    return location;
                }
            }
        }
        
        plugin.getLogger().warning("Could not find a suitable location for chest in world: " + world.getName());
        return null;
    }

    /**
     * Find a suitable Y coordinate for a chest
     * @param world World to search in
     * @param x X coordinate
     * @param z Z coordinate
     * @param minY Minimum Y coordinate
     * @param maxY Maximum Y coordinate
     * @param requireSolidGround Whether the chest must be placed on solid ground
     * @return Suitable Y coordinate or -1 if none found
     */
    private int findSuitableY(World world, int x, int z, int minY, int maxY, boolean requireSolidGround) {
        if (requireSolidGround) {
            // Start from the top and work down to find solid ground
            for (int y = maxY; y >= minY; y--) {
                Block block = world.getBlockAt(x, y, z);
                Block blockAbove = world.getBlockAt(x, y + 1, z);
                
                if (block.getType().isSolid() && blockAbove.getType().isAir()) {
                    return y + 1; // Place chest on top of solid block
                }
            }
        } else {
            // Just pick a random Y within the range
            return minY + ThreadLocalRandom.current().nextInt(maxY - minY + 1);
        }
        
        return -1;
    }

    /**
     * Check if a location is suitable for a chest
     * @param location Location to check
     * @return true if the location is suitable
     */
    private boolean isSuitableForChest(Location location) {
        Block block = location.getBlock();
        
        // Check if the block is air and can be replaced
        if (!block.getType().isAir()) {
            return false;
        }
        
        // Check if there's enough space for a chest
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block nearbyBlock = location.clone().add(x, 0, z).getBlock();
                if (nearbyBlock.getType() == Material.CHEST) {
                    return false; // Don't place chests too close to each other
                }
            }
        }
        
        return true;
    }

    /**
     * Determine the tier of a chest based on configured probabilities
     * @return Tier name
     */
    private String determineTier() {
        // Default tiers if not configured
        Map<String, Integer> tierChances = new HashMap<>();
        tierChances.put("common", 60);
        tierChances.put("uncommon", 30);
        tierChances.put("rare", 8);
        tierChances.put("epic", 2);
        
        // Get tier chances from config
        if (plugin.getConfig().contains("loot.tiers")) {
            for (String tier : plugin.getConfig().getConfigurationSection("loot.tiers").getKeys(false)) {
                int chance = plugin.getConfig().getInt("loot.tiers." + tier + ".chance", 0);
                if (chance > 0) {
                    tierChances.put(tier, chance);
                }
            }
        }
        
        // Calculate total chance
        int totalChance = tierChances.values().stream().mapToInt(Integer::intValue).sum();
        
        // Generate a random number
        int random = ThreadLocalRandom.current().nextInt(totalChance);
        
        // Determine tier based on random number
        int currentSum = 0;
        for (Map.Entry<String, Integer> entry : tierChances.entrySet()) {
            currentSum += entry.getValue();
            if (random < currentSum) {
                return entry.getKey();
            }
        }
        
        // Default to common if something goes wrong
        return "common";
    }

    /**
     * Spawn a chest at the given location
     * @param location Location to spawn the chest
     * @param tier Tier of the chest
     * @return UUID of the spawned chest
     */
    public UUID spawnChest(Location location, String tier) {
        Block block = location.getBlock();
        block.setType(Material.CHEST);
        
        UUID chestId = UUID.randomUUID();
        TreasureChest treasureChest = new TreasureChest(chestId, location, tier, System.currentTimeMillis());
        activeChests.put(chestId, treasureChest);
        
        // Fill the chest with loot
        Chest chest = (Chest) block.getState();
        plugin.getLootManager().fillChest(chest.getInventory(), tier);
        
        // Schedule despawn if enabled
        if (plugin.getConfigManager().isDespawnEnabled()) {
            int despawnTime = plugin.getConfigManager().getDespawnTime();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (activeChests.containsKey(chestId)) {
                    removeChest(chestId);
                    plugin.getLogger().info("Treasure chest despawned due to timeout");
                    
                    // Broadcast despawn message if configured
                    if (plugin.getConfigManager().isBroadcastSpawnEnabled()) {
                        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("chest-despawn"));
                    }
                }
            }, despawnTime * 20L * 60L); // Convert minutes to ticks
        }
        
        plugin.getLogger().info("Spawned a " + tier + " treasure chest at " + LocationUtils.formatLocation(location));
        return chestId;
    }

    /**
     * Remove a chest from the world
     * @param chestId UUID of the chest to remove
     */
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

    /**
     * Get a chest by its location
     * @param location Location to check
     * @return TreasureChest at the location or null if none found
     */
    public TreasureChest getChestAt(Location location) {
        for (TreasureChest chest : activeChests.values()) {
            if (LocationUtils.isSameBlock(chest.getLocation(), location)) {
                return chest;
            }
        }
        return null;
    }

    /**
     * Get the nearest chest to a player
     * @param player Player to check
     * @return Nearest TreasureChest or null if none found
     */
    public TreasureChest getNearestChest(Player player) {
        if (activeChests.isEmpty()) {
            return null;
        }
        
        Location playerLocation = player.getLocation();
        TreasureChest nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (TreasureChest chest : activeChests.values()) {
            // Skip chests in different worlds
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

    /**
     * Get all active chests
     * @return Map of active chests
     */
    public Map<UUID, TreasureChest> getActiveChests() {
        return activeChests;
    }

    /**
     * Remove all active chests
     */
    public void removeAllChests() {
        for (UUID chestId : new ArrayList<>(activeChests.keySet())) {
            removeChest(chestId);
        }
        
        plugin.getLogger().info("Removed all treasure chests");
    }
}
