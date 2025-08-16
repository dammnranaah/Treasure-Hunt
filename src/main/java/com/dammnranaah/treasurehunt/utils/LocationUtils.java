package com.dammnranaah.treasurehunt.utils;

import org.bukkit.Location;

/**
 * Utility class for location-related operations
 */
public class LocationUtils {

    /**
     * Format a location as a string
     * @param location Location to format
     * @return Formatted location string
     */
    public static String formatLocation(Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        return String.format("%s [%d, %d, %d]", 
                location.getWorld().getName(), 
                location.getBlockX(), 
                location.getBlockY(), 
                location.getBlockZ());
    }
    
    /**
     * Get a general area name based on coordinates
     * @param location Location to get area name for
     * @return Area name
     */
    public static String getAreaName(Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        // Get cardinal direction from spawn
        Location spawn = location.getWorld().getSpawnLocation();
        String direction = getCardinalDirection(spawn, location);
        
        // Get biome name
        String biome = location.getBlock().getBiome().name()
                .toLowerCase()
                .replace("_", " ");
        
        // Format the area name
        return String.format("the %s %s", biome, direction);
    }
    
    /**
     * Get the cardinal direction from one location to another
     * @param from Starting location
     * @param to Ending location
     * @return Cardinal direction
     */
    public static String getCardinalDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        if (Math.abs(dx) > Math.abs(dz) * 2) {
            return dx > 0 ? "east" : "west";
        } else if (Math.abs(dz) > Math.abs(dx) * 2) {
            return dz > 0 ? "south" : "north";
        } else {
            if (dx > 0 && dz > 0) {
                return "southeast";
            } else if (dx > 0) {
                return "northeast";
            } else if (dz > 0) {
                return "southwest";
            } else {
                return "northwest";
            }
        }
    }
    
    /**
     * Check if two locations refer to the same block
     * @param loc1 First location
     * @param loc2 Second location
     * @return true if the locations refer to the same block
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        
        return loc1.getWorld().equals(loc2.getWorld()) &&
               loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    /**
     * Calculate the distance between two locations
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in blocks, or -1 if the locations are in different worlds
     */
    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || !loc1.getWorld().equals(loc2.getWorld())) {
            return -1;
        }
        
        return loc1.distance(loc2);
    }
}
