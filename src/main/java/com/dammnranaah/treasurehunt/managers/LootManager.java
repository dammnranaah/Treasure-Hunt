package com.dammnranaah.treasurehunt.managers;

import com.dammnranaah.treasurehunt.TreasureHunt;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager {

    private final TreasureHunt plugin;
    private final Map<String, List<LootItem>> lootTables;

    public LootManager(TreasureHunt plugin) {
        this.plugin = plugin;
        this.lootTables = new HashMap<>();
        loadLootTables();
    }


    private void loadLootTables() {
        ConfigurationSection lootConfig = plugin.getConfig().getConfigurationSection("loot.items");
        if (lootConfig == null) {
            plugin.getLogger().warning("No loot tables found in config");
            return;
        }


        for (String tier : lootConfig.getKeys(false)) {
            List<LootItem> items = new ArrayList<>();
            ConfigurationSection tierSection = lootConfig.getConfigurationSection(tier);
            
            if (tierSection != null) {
                for (String key : tierSection.getKeys(false)) {
                    ConfigurationSection itemSection = tierSection.getConfigurationSection(key);
                    
                    if (itemSection != null) {
                        try {
                            String materialName = itemSection.getString("item");
                            Material material = Material.valueOf(materialName);
                            
                            String amountRange = itemSection.getString("amount", "1");
                            int minAmount = 1;
                            int maxAmount = 1;
                            
                            if (amountRange.contains("-")) {
                                String[] parts = amountRange.split("-");
                                minAmount = Integer.parseInt(parts[0]);
                                maxAmount = Integer.parseInt(parts[1]);
                            } else {
                                minAmount = maxAmount = Integer.parseInt(amountRange);
                            }
                            
                            int chance = itemSection.getInt("chance", 100);
                            
                            LootItem lootItem = new LootItem(material, minAmount, maxAmount, chance);
                            

                            ConfigurationSection enchantmentsSection = itemSection.getConfigurationSection("enchantments");
                            if (enchantmentsSection != null) {
                                for (String enchKey : enchantmentsSection.getKeys(false)) {
                                    ConfigurationSection enchSection = enchantmentsSection.getConfigurationSection(enchKey);
                                    
                                    if (enchSection != null) {
                                        String enchantmentName = enchSection.getString("type");
                                        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
                                        
                                        if (enchantment != null) {
                                            String levelRange = enchSection.getString("level", "1");
                                            int minLevel = 1;
                                            int maxLevel = 1;
                                            
                                            if (levelRange.contains("-")) {
                                                String[] parts = levelRange.split("-");
                                                minLevel = Integer.parseInt(parts[0]);
                                                maxLevel = Integer.parseInt(parts[1]);
                                            } else {
                                                minLevel = maxLevel = Integer.parseInt(levelRange);
                                            }
                                            
                                            int enchChance = enchSection.getInt("chance", 100);
                                            
                                            lootItem.addEnchantment(enchantment, minLevel, maxLevel, enchChance);
                                        }
                                    }
                                }
                            }
                            
                            items.add(lootItem);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid item in loot table: " + e.getMessage());
                        }
                    }
                }
            }
            
            lootTables.put(tier, items);
            plugin.getLogger().info("Loaded " + items.size() + " items for loot tier: " + tier);
        }
    }


    public void fillChest(Inventory inventory, String tier) {

        inventory.clear();
        

        List<LootItem> lootTable = lootTables.get(tier.toLowerCase());
        if (lootTable == null || lootTable.isEmpty()) {
            plugin.getLogger().warning("No loot table found for tier: " + tier);
            return;
        }
        

        int minItems = plugin.getConfig().getInt("loot.tiers." + tier + ".min-items", 2);
        int maxItems = plugin.getConfig().getInt("loot.tiers." + tier + ".max-items", 5);
        

        int itemCount = ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);
        

        for (int i = 0; i < itemCount; i++) {
            ItemStack item = getRandomLoot(lootTable);
            if (item != null) {

                int slot;
                do {
                    slot = ThreadLocalRandom.current().nextInt(inventory.getSize());
                } while (inventory.getItem(slot) != null);
                
                inventory.setItem(slot, item);
            }
        }
        

        if (plugin.getConfig().getBoolean("loot.use-vanilla-loot", true)) {
            addVanillaLoot(inventory, tier);
        }
    }


    private ItemStack getRandomLoot(List<LootItem> lootTable) {

        int totalWeight = 0;
        for (LootItem item : lootTable) {
            totalWeight += item.getChance();
        }
        

        if (totalWeight <= 0) {
            return null;
        }
        

        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;
        
        for (LootItem lootItem : lootTable) {
            currentWeight += lootItem.getChance();
            if (random < currentWeight) {
                return lootItem.createItemStack();
            }
        }
        
        return null;
    }


    private void addVanillaLoot(Inventory inventory, String tier) {
        LootTable lootTable = null;
        

        switch (tier.toLowerCase()) {
            case "common":
                lootTable = getRandomLootTable(Arrays.asList(
                        LootTables.SIMPLE_DUNGEON,
                        LootTables.VILLAGE_TEMPLE,
                        LootTables.SPAWN_BONUS_CHEST
                ));
                break;
            case "uncommon":
                lootTable = getRandomLootTable(Arrays.asList(
                        LootTables.DESERT_PYRAMID,
                        LootTables.JUNGLE_TEMPLE,
                        LootTables.ABANDONED_MINESHAFT
                ));
                break;
            case "rare":
                lootTable = getRandomLootTable(Arrays.asList(
                        LootTables.STRONGHOLD_LIBRARY,
                        LootTables.STRONGHOLD_CROSSING,
                        LootTables.NETHER_BRIDGE
                ));
                break;
            case "epic":
                lootTable = getRandomLootTable(Arrays.asList(
                        LootTables.END_CITY_TREASURE,
                        LootTables.WOODLAND_MANSION,
                        LootTables.BURIED_TREASURE
                ));
                break;
        }
        

        if (lootTable != null) {

            ItemStack placeholder = new ItemStack(Material.BOOK);
            ItemMeta meta = placeholder.getItemMeta();
            meta.setDisplayName("Treasure");
            placeholder.setItemMeta(meta);
            

            int slot;
            do {
                slot = ThreadLocalRandom.current().nextInt(inventory.getSize());
            } while (inventory.getItem(slot) != null);
            
            inventory.setItem(slot, placeholder);
        }
    }
    

    private LootTable getRandomLootTable(List<LootTables> lootTables) {
        if (lootTables.isEmpty()) {
            return null;
        }
        
        LootTables randomTable = lootTables.get(ThreadLocalRandom.current().nextInt(lootTables.size()));
        return randomTable.getLootTable();
    }
    

    public void reloadLootTables() {
        lootTables.clear();
        loadLootTables();
    }
    

    private class LootItem {
        private final Material material;
        private final int minAmount;
        private final int maxAmount;
        private final int chance;
        private final List<EnchantmentInfo> enchantments;
        
        public LootItem(Material material, int minAmount, int maxAmount, int chance) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.chance = chance;
            this.enchantments = new ArrayList<>();
        }
        
        public void addEnchantment(Enchantment enchantment, int minLevel, int maxLevel, int chance) {
            enchantments.add(new EnchantmentInfo(enchantment, minLevel, maxLevel, chance));
        }
        
        public int getChance() {
            return chance;
        }
        
        public ItemStack createItemStack() {
            int amount = ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
            ItemStack item = new ItemStack(material, amount);
            

            if (!enchantments.isEmpty() && material != Material.AIR) {
                for (EnchantmentInfo enchInfo : enchantments) {

                    if (ThreadLocalRandom.current().nextInt(100) < enchInfo.getChance()) {
                        int level = ThreadLocalRandom.current().nextInt(
                                enchInfo.getMinLevel(), 
                                enchInfo.getMaxLevel() + 1);
                        

                        if (material == Material.ENCHANTED_BOOK) {
                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                            meta.addStoredEnchant(enchInfo.getEnchantment(), level, true);
                            item.setItemMeta(meta);
                        } else {
                            item.addUnsafeEnchantment(enchInfo.getEnchantment(), level);
                        }
                    }
                }
            }
            
            return item;
        }
    }
    

    private class EnchantmentInfo {
        private final Enchantment enchantment;
        private final int minLevel;
        private final int maxLevel;
        private final int chance;
        
        public EnchantmentInfo(Enchantment enchantment, int minLevel, int maxLevel, int chance) {
            this.enchantment = enchantment;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.chance = chance;
        }
        
        public Enchantment getEnchantment() {
            return enchantment;
        }
        
        public int getMinLevel() {
            return minLevel;
        }
        
        public int getMaxLevel() {
            return maxLevel;
        }
        
        public int getChance() {
            return chance;
        }
    }
}
