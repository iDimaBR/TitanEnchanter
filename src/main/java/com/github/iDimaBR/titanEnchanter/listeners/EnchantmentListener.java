package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class EnchantmentListener implements Listener {

    private final TitanEnchanter plugin;
    private final EnchantManager enchantManager;
    private final ConfigUtil config;

    public EnchantmentListener(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantManager();
        this.config = plugin.getConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());
        if (level == null) return;

        int[] offeredLevels = event.getExpLevelCostsOffered();
        for (int i = 0; i < offeredLevels.length; i++) {
            if (!enchantManager.isValidLevel(offeredLevels[i], level)) {
                offeredLevels[i] = -1;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());

        if (level == null) return;

        int enchantLevel = event.getExpLevelCost();
        if (!enchantManager.isValidLevel(enchantLevel, level)) {
            event.setCancelled(true);
            String message = config.getString("messages.invalid-level")
                .replace("{min}", String.valueOf(level.getMinLevel()))
                .replace("{max}", String.valueOf(level.getMaxLevel()));
            player.sendMessage(message);
            return;
        }

        if (!enchantManager.hasEnoughTickets(player, enchantLevel)) {
            event.setCancelled(true);
            String message = config.getString("messages.insufficient-tickets");
            player.sendMessage(message);
            return;
        }

        enchantManager.deductTickets(player, enchantLevel);

        String successMessage = config.getString("messages.enchant-success");
        player.sendMessage(successMessage);
    }
}