package com.guythatlives.bedwarsg.shop;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory {

    private final String name;
    private final List<ShopItem> items;

    public ShopCategory(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public String getName() {
        return name;
    }

    public List<ShopItem> getItems() {
        return items;
    }
}
