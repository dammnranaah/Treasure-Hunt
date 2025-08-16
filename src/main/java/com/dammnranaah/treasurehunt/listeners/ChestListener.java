package com.dammnranaah.treasurehunt.listeners;

import com.dammnranaah.treasurehunt.TreasureHunt;
import com.dammnranaah.treasurehunt.models.TreasureChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;


public class ChestListener implements Listener {

    private final TreasureHunt plugin;

    public ChestListener(TreasureHunt plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle chest open events
     * @param event InventoryOpenEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            Block block = chest.getBlock();
            TreasureChest treasureChest = plugin.getChestManager().getChestAt(block.getLocation());

            if (treasureChest != null && !treasureChest.isLooted()) {
                // Mark the chest as looted
                treasureChest.setLooted(true);

                // Broadcast chest found message if enabled
                if (plugin.getConfigManager().isBroadcastSpawnEnabled()) {
                    String message = plugin.getConfigManager().getMessage("chest-found")
                            .replace("%player%", player.getName());
                    Bukkit.broadcastMessage(message);
                }

                // Log the event
                plugin.getLogger().info(player.getName() + " found a treasure chest at " + 
                        block.getLocation().getWorld().getName() + " " + 
                        block.getLocation().getBlockX() + " " + 
                        block.getLocation().getBlockY() + " " + 
                        block.getLocation().getBlockZ());
            }
        }
    }

    /**
     * Handle chest close events
     * @param event InventoryCloseEvent
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            Block block = chest.getBlock();
            TreasureChest treasureChest = plugin.getChestManager().getChestAt(block.getLocation());

            if (treasureChest != null && treasureChest.isLooted()) {
                // Check if the chest is empty
                boolean isEmpty = true;
                for (int i = 0; i < event.getInventory().getSize(); i++) {
                    if (event.getInventory().getItem(i) != null) {
                        isEmpty = false;
                        break;
                    }
                }

                // Remove the chest if it's empty
                if (isEmpty) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getChestManager().removeChest(treasureChest.getId());
                    }, 20L); // Remove after 1 second
                }
            }
        }
    }

    /**
     * Prevent breaking treasure chests
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.CHEST) {
            TreasureChest treasureChest = plugin.getChestManager().getChestAt(block.getLocation());

            if (treasureChest != null) {
                // Only allow breaking if the player has admin permission
                if (!event.getPlayer().hasPermission("treasurehunt.admin")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot break treasure chests!");
                } else {
                    // Admin is breaking the chest, remove it from the manager
                    plugin.getChestManager().removeChest(treasureChest.getId());
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Treasure chest removed.");
                }
            }
        }
    }

    /**
     * Handle player interact events for chest protection
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block != null && block.getType() == Material.CHEST) {
            TreasureChest treasureChest = plugin.getChestManager().getChestAt(block.getLocation());

            if (treasureChest != null) {
                // Allow interaction for all players (no need to cancel the event)
                // This is just a hook for potential future features
            }
        }
    }
}
