package com.dammnranaah.treasurehunt.models;

import java.util.UUID;

import org.bukkit.Location;

public class TreasureChest {

    private final UUID id;
    private final Location location;
    private final String tier;
    private final long spawnTime;
    private boolean looted;
    public TreasureChest(UUID id, Location location, String tier, long spawnTime) {
        this.id = id;
        this.location = location;
        this.tier = tier;
        this.spawnTime = spawnTime;
        this.looted = false;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getTier() {
        return tier;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public boolean isLooted() {
        return looted;
    }

    public void setLooted(boolean looted) {
        this.looted = looted;
    }
}
