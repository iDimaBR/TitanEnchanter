package com.github.iDimaBR.titanEnchanter.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter @Setter
public class EnchanterHolder implements InventoryHolder {

    private Inventory inventory;

    public EnchanterHolder(Inventory inventory) {
        this.inventory = inventory;
    }
}