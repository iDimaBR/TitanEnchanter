package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryCloseListener implements Listener {

    private final EnchantManager enchantManager;

    public InventoryCloseListener(TitanEnchanter plugin) {
        this.enchantManager = plugin.getEnchantManager();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();

        if (event.getView().getType() == InventoryType.ENCHANTING) {
            enchantManager.removePlayerLevel(player.getUniqueId());
        }
    }
}