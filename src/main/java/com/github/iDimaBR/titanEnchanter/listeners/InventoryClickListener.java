package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.managers.GUIManager;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class InventoryClickListener implements Listener {

    private final TitanEnchanter plugin;
    private final GUIManager guiManager;
    private final EnchantManager enchantManager;

    public InventoryClickListener(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.enchantManager = plugin.getEnchantManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        final Player player = (Player) event.getWhoClicked();
        if (!guiManager.isMainMenu(event.getInventory())) return;

        final Inventory clickedInventory = event.getClickedInventory();
        if(clickedInventory == null) return;
        if(clickedInventory.getType() == InventoryType.PLAYER) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        EnchantLevel level = guiManager.getLevelFromSlot(event.getRawSlot());
        enchantManager.openEnchantingTable(player, level);
    }
}