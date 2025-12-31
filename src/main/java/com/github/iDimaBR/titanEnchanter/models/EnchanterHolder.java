package com.github.iDimaBR.titanEnchanter.menus;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter
public class EnchanterHolder implements InventoryHolder {

    private final Inventory inventory;

    public EnchanterHolder(Inventory inventory) {
        this.inventory = inventory;
    }
}