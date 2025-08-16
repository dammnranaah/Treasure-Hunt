package com.dammnranaah.treasurehunt.utils;

import org.bukkit.Location;

public class LocationUtils {
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
    
    
    public static String getAreaName(Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        Location spawn = location.getWorld().getSpawnLocation();
        String direction = getCardinalDirection(spawn, location);
        
        String biome = location.getBlock().getBiome().name()
                .toLowerCase()
                .replace("_", " ");
        
        return String.format("the %s %s", biome, direction);
    }
    
    
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
    
    
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        
        return loc1.getWorld().equals(loc2.getWorld()) &&
               loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    
    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || !loc1.getWorld().equals(loc2.getWorld())) {
            return -1;
        }
        
        return loc1.distance(loc2);
    }
}
