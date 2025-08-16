package com.dammnranaah.treasurehunt.models;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Represents a treasure chest in the world
 */
public class TreasureChest {

    private final UUID id;
    private final Location location;
    private final String tier;
    private final long spawnTime;
    private boolean looted;

    /**
     * Create a new treasure chest
     * @param id Unique identifier
     * @param location Location of the chest
     * @param tier Tier of the chest (common, uncommon, rare, epic)
     * @param spawnTime Time when the chest was spawned
     */
    public TreasureChest(UUID id, Location location, String tier, long spawnTime) {
        this.id = id;
        this.location = location;
        this.tier = tier;
        this.spawnTime = spawnTime;
        this.looted = false;
    }

    /**
     * Get the unique identifier of the chest
     * @return Chest ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the location of the chest
     * @return Chest location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the tier of the chest
     * @return Chest tier
     */
    public String getTier() {
        return tier;
    }

    /**
     * Get the time when the chest was spawned
     * @return Spawn time in milliseconds
     */
    public long getSpawnTime() {
        return spawnTime;
    }

    /**
     * Check if the chest has been looted
     * @return true if the chest has been looted
     */
    public boolean isLooted() {
        return looted;
    }

    /**
     * Set the looted status of the chest
     * @param looted New looted status
     */
    public void setLooted(boolean looted) {
        this.looted = looted;
    }
}
