package com.github.iDimaBR.titanEnchanter.models;

import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter @Setter
public class EnchantMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private EnchantLevel level;

    public EnchantMenuHolder(Inventory inventory, EnchantLevel level) {
        this.inventory = inventory;
        this.level = level;
    }
}