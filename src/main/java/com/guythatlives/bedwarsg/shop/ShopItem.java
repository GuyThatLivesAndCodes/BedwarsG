package com.guythatlives.bedwarsg.shop;

import org.bukkit.Material;

public class ShopItem {

    private final Material material;
    private final String displayName;
    private final int amount;
    private final Material currency;
    private final int cost;

    public ShopItem(Material material, String displayName, int amount, Material currency, int cost) {
        this.material = material;
        this.displayName = displayName;
        this.amount = amount;
        this.currency = currency;
        this.cost = cost;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAmount() {
        return amount;
    }

    public Material getCurrency() {
        return currency;
    }

    public int getCost() {
        return cost;
    }
}
