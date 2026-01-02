package com.guythatlives.bedwarsg.shop;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopManager {

    private final BedwarsG plugin;
    private final Map<String, ShopCategory> categories;

    public ShopManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.categories = new HashMap<>();
        initializeShop();
    }

    private void initializeShop() {
        // Blocks category
        ShopCategory blocks = new ShopCategory("Blocks");
        blocks.addItem(new ShopItem(Material.WHITE_WOOL, "Wool", 4, Material.IRON_INGOT, 1));
        blocks.addItem(new ShopItem(Material.TERRACOTTA, "Hardened Clay", 12, Material.IRON_INGOT, 1));
        blocks.addItem(new ShopItem(Material.OAK_PLANKS, "Wood", 4, Material.GOLD_INGOT, 1));
        blocks.addItem(new ShopItem(Material.END_STONE, "End Stone", 12, Material.IRON_INGOT, 1));
        blocks.addItem(new ShopItem(Material.OBSIDIAN, "Obsidian", 4, Material.EMERALD, 1));
        categories.put("blocks", blocks);

        // Weapons category
        ShopCategory weapons = new ShopCategory("Weapons");
        weapons.addItem(new ShopItem(Material.STONE_SWORD, "Stone Sword", 1, Material.IRON_INGOT, 10));
        weapons.addItem(new ShopItem(Material.IRON_SWORD, "Iron Sword", 1, Material.GOLD_INGOT, 7));
        weapons.addItem(new ShopItem(Material.DIAMOND_SWORD, "Diamond Sword", 1, Material.EMERALD, 4));
        weapons.addItem(new ShopItem(Material.STICK, "Knockback Stick", 1, Material.GOLD_INGOT, 5));
        categories.put("weapons", weapons);

        // Armor category
        ShopCategory armor = new ShopCategory("Armor");
        armor.addItem(new ShopItem(Material.CHAINMAIL_BOOTS, "Chainmail Armor", 1, Material.IRON_INGOT, 40));
        armor.addItem(new ShopItem(Material.IRON_BOOTS, "Iron Armor", 1, Material.GOLD_INGOT, 12));
        armor.addItem(new ShopItem(Material.DIAMOND_BOOTS, "Diamond Armor", 1, Material.EMERALD, 6));
        categories.put("armor", armor);

        // Tools category
        ShopCategory tools = new ShopCategory("Tools");
        tools.addItem(new ShopItem(Material.WOODEN_PICKAXE, "Wood Pickaxe", 1, Material.IRON_INGOT, 10));
        tools.addItem(new ShopItem(Material.STONE_PICKAXE, "Stone Pickaxe", 1, Material.IRON_INGOT, 10));
        tools.addItem(new ShopItem(Material.IRON_PICKAXE, "Iron Pickaxe", 1, Material.GOLD_INGOT, 3));
        tools.addItem(new ShopItem(Material.DIAMOND_PICKAXE, "Diamond Pickaxe", 1, Material.GOLD_INGOT, 6));
        tools.addItem(new ShopItem(Material.WOODEN_AXE, "Wood Axe", 1, Material.IRON_INGOT, 10));
        tools.addItem(new ShopItem(Material.STONE_AXE, "Stone Axe", 1, Material.IRON_INGOT, 10));
        tools.addItem(new ShopItem(Material.SHEARS, "Shears", 1, Material.IRON_INGOT, 20));
        categories.put("tools", tools);

        // Food category
        ShopCategory food = new ShopCategory("Food");
        food.addItem(new ShopItem(Material.APPLE, "Apple", 1, Material.IRON_INGOT, 4));
        food.addItem(new ShopItem(Material.GOLDEN_APPLE, "Golden Apple", 1, Material.GOLD_INGOT, 3));
        categories.put("food", food);

        // Special category
        ShopCategory special = new ShopCategory("Special");
        special.addItem(new ShopItem(Material.TNT, "TNT", 1, Material.GOLD_INGOT, 4));
        special.addItem(new ShopItem(Material.ENDER_PEARL, "Ender Pearl", 1, Material.EMERALD, 4));
        special.addItem(new ShopItem(Material.WATER_BUCKET, "Water Bucket", 1, Material.GOLD_INGOT, 6));
        special.addItem(new ShopItem(Material.ARROW, "Arrow", 8, Material.GOLD_INGOT, 2));
        special.addItem(new ShopItem(Material.BOW, "Bow", 1, Material.GOLD_INGOT, 12));
        categories.put("special", special);
    }

    public void openShop(Player player, String categoryName) {
        ShopCategory category = categories.get(categoryName.toLowerCase());
        if (category == null) {
            openMainShop(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, category.getName() + " Shop");

        int slot = 0;
        for (ShopItem item : category.getItems()) {
            ItemStack stack = new ItemStack(item.getMaterial(), item.getAmount());
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName("§a" + item.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Cost: §e" + item.getCost() + " " + formatMaterial(item.getCurrency()));
            lore.add("");
            lore.add("§eClick to purchase!");
            meta.setLore(lore);

            stack.setItemMeta(meta);
            inv.setItem(slot++, stack);
        }

        player.openInventory(inv);
    }

    public void openMainShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Item Shop");

        // Add category items
        inv.setItem(10, createCategoryItem(Material.WHITE_WOOL, "§aBlocks", "blocks"));
        inv.setItem(12, createCategoryItem(Material.STONE_SWORD, "§aWeapons", "weapons"));
        inv.setItem(14, createCategoryItem(Material.CHAINMAIL_CHESTPLATE, "§aArmor", "armor"));
        inv.setItem(16, createCategoryItem(Material.WOODEN_PICKAXE, "§aTools", "tools"));
        inv.setItem(28, createCategoryItem(Material.GOLDEN_APPLE, "§aFood", "food"));
        inv.setItem(30, createCategoryItem(Material.TNT, "§aSpecial Items", "special"));

        player.openInventory(inv);
    }

    private ItemStack createCategoryItem(Material material, String name, String category) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("§7Click to browse " + category);
        meta.setLore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    public boolean purchaseItem(Player player, ShopItem item) {
        int cost = item.getCost();
        Material currency = item.getCurrency();

        // Count currency in inventory
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == currency) {
                total += stack.getAmount();
            }
        }

        if (total < cost) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency", formatMaterial(currency));
            String message = plugin.getConfigManager().getMessage("shop.insufficient-funds", placeholders);
            player.sendMessage(message);
            return false;
        }

        // Remove currency
        int remaining = cost;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack != null && stack.getType() == currency) {
                int amount = stack.getAmount();
                if (amount <= remaining) {
                    remaining -= amount;
                    player.getInventory().setItem(i, null);
                } else {
                    stack.setAmount(amount - remaining);
                    remaining = 0;
                }

                if (remaining == 0) {
                    break;
                }
            }
        }

        // Give item
        ItemStack purchasedItem = new ItemStack(item.getMaterial(), item.getAmount());
        player.getInventory().addItem(purchasedItem);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item.getDisplayName());
        placeholders.put("cost", String.valueOf(cost));
        placeholders.put("currency", formatMaterial(currency));
        String message = plugin.getConfigManager().getMessage("shop.purchased", placeholders);
        player.sendMessage(message);

        return true;
    }

    private String formatMaterial(Material material) {
        switch (material) {
            case IRON_INGOT:
                return "Iron";
            case GOLD_INGOT:
                return "Gold";
            case DIAMOND:
                return "Diamond";
            case EMERALD:
                return "Emerald";
            default:
                return material.name();
        }
    }

    public ShopCategory getCategory(String name) {
        return categories.get(name.toLowerCase());
    }
}
