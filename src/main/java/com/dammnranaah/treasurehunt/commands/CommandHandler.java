package com.dammnranaah.treasurehunt.commands;

import com.dammnranaah.treasurehunt.TreasureHunt;
import com.dammnranaah.treasurehunt.models.TreasureChest;
import com.dammnranaah.treasurehunt.utils.LocationUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final TreasureHunt plugin;
    private final Map<UUID, Long> locateCooldowns;

    public CommandHandler(TreasureHunt plugin) {
        this.plugin = plugin;
        this.locateCooldowns = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                return handleStartCommand(sender);
            case "stop":
                return handleStopCommand(sender);
            case "reload":
                return handleReloadCommand(sender);
            case "locate":
                return handleLocateCommand(sender);
            case "help":
            default:
                sendHelpMessage(sender);
                return true;
        }
    }


    private boolean handleStartCommand(CommandSender sender) {
        if (!sender.hasPermission("treasurehunt.start")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        int count = plugin.getConfigManager().getChestCount();
        int spawned = plugin.getChestManager().spawnChests(count);

        if (spawned > 0) {
            sender.sendMessage(ChatColor.GREEN + "Spawned " + spawned + " treasure chests!");
            

            if (plugin.getConfigManager().isBroadcastSpawnEnabled()) {
                plugin.getServer().broadcastMessage(plugin.getConfigManager().getMessage("spawn"));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Could not spawn any chests. Check the console for details.");
        }

        return true;
    }


    private boolean handleStopCommand(CommandSender sender) {
        if (!sender.hasPermission("treasurehunt.stop")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        int removed = plugin.getChestManager().getActiveChests().size();
        plugin.getChestManager().removeAllChests();

        sender.sendMessage(ChatColor.GREEN + "Removed " + removed + " treasure chests!");
        

        if (plugin.getConfigManager().isBroadcastSpawnEnabled()) {
            plugin.getServer().broadcastMessage(plugin.getConfigManager().getMessage("chest-despawn"));
        }

        return true;
    }


    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("treasurehunt.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }


        plugin.getConfigManager().reloadConfig();
        

        plugin.getLootManager().reloadLootTables();
        

        if (plugin.getConfigManager().isAutoSpawnEnabled()) {
            plugin.stopSpawnTask();
            plugin.startSpawnTask();
        } else {
            plugin.stopSpawnTask();
        }

        sender.sendMessage(ChatColor.GREEN + "TreasureHunt configuration reloaded!");
        return true;
    }


    private boolean handleLocateCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("treasurehunt.locate")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }


        int cooldown = plugin.getConfigManager().getLocateCooldown();
        if (cooldown > 0) {
            long lastUse = locateCooldowns.getOrDefault(player.getUniqueId(), 0L);
            long timeLeft = (lastUse + cooldown * 1000L - System.currentTimeMillis()) / 1000L;

            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " seconds before using this command again.");
                return true;
            }
        }


        TreasureChest nearest = plugin.getChestManager().getNearestChest(player);

        if (nearest == null) {
            player.sendMessage(ChatColor.RED + "There are no treasure chests currently active.");
            return true;
        }


        double distance = player.getLocation().distance(nearest.getLocation());
        String direction = LocationUtils.getCardinalDirection(player.getLocation(), nearest.getLocation());
        String biome = nearest.getLocation().getBlock().getBiome().name().toLowerCase().replace("_", " ");

        player.sendMessage(ChatColor.GOLD + "The nearest treasure chest is " + 
                ChatColor.YELLOW + (int) distance + " blocks away " + 
                ChatColor.GOLD + "to the " + 
                ChatColor.YELLOW + direction + 
                ChatColor.GOLD + " in a " + 
                ChatColor.YELLOW + biome + 
                ChatColor.GOLD + ".");


        if (plugin.getConfigManager().isLocatePenaltyEnabled()) {

            if (plugin.getConfigManager().reduceLocateLootQuality()) {

            }


            if (plugin.getConfigManager().broadcastLocateUsage()) {
                plugin.getServer().broadcastMessage(
                        ChatColor.GOLD + "[TreasureHunt] " + 
                        ChatColor.YELLOW + player.getName() + 
                        ChatColor.GOLD + " is using a treasure locator!");
            }
        }


        locateCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        return true;
    }


    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== TreasureHunt Commands ===");
        
        if (sender.hasPermission("treasurehunt.start")) {
            sender.sendMessage(ChatColor.YELLOW + "/treasurehunt start" + ChatColor.GRAY + " - Spawn treasure chests");
        }
        
        if (sender.hasPermission("treasurehunt.stop")) {
            sender.sendMessage(ChatColor.YELLOW + "/treasurehunt stop" + ChatColor.GRAY + " - Remove all treasure chests");
        }
        
        if (sender.hasPermission("treasurehunt.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/treasurehunt reload" + ChatColor.GRAY + " - Reload the plugin configuration");
        }
        
        if (sender.hasPermission("treasurehunt.locate")) {
            sender.sendMessage(ChatColor.YELLOW + "/treasurehunt locate" + ChatColor.GRAY + " - Find the nearest treasure chest");
        }
        
        sender.sendMessage(ChatColor.YELLOW + "/treasurehunt help" + ChatColor.GRAY + " - Show this help message");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("treasurehunt.start")) {
                completions.add("start");
            }
            if (sender.hasPermission("treasurehunt.stop")) {
                completions.add("stop");
            }
            if (sender.hasPermission("treasurehunt.reload")) {
                completions.add("reload");
            }
            
            if (sender.hasPermission("treasurehunt.locate")) {
                completions.add("locate");
            }
            
            completions.add("help");
            
            return filterCompletions(completions, args[0]);
        }

        return completions;
    }


    private List<String> filterCompletions(List<String> completions, String current) {
        if (current.isEmpty()) {
            return completions;
        }

        current = current.toLowerCase();
        List<String> filtered = new ArrayList<>();

        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(current)) {
                filtered.add(completion);
            }
        }

        return filtered;
    }
}
