package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class EnchantmentListener implements Listener {

    private final EnchantManager enchantManager;
    private final ConfigUtil config;

    public EnchantmentListener(TitanEnchanter plugin) {
        this.enchantManager = plugin.getEnchantManager();
        this.config = plugin.getConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());
        if (level == null) return;

        int[] costs = level.getOfferedCosts();
        int[] offeredLevels = event.getExpLevelCostsOffered();
        for (int i = 0; i < offeredLevels.length && i < 3; i++) {
            offeredLevels[i] = costs[i];
        }

        EnchantmentOffer[] offers = event.getOffers();
        if (offers != null) {
            for (int i = 0; i < offers.length && i < 3; i++) {
                EnchantmentOffer offer = offers[i];
                if (offer != null) {
                    offer.setCost(costs[i]);
                }
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
            if(config.getBoolean("messages.invalid-level.enabled")) {
                String message = config.getString("messages.invalid-level.text")
                        .replace("{min}", String.valueOf(level.getMinLevel()))
                        .replace("{max}", String.valueOf(level.getMaxLevel()));
                player.sendMessage(message);
            }
            return;
        }

        if (!enchantManager.hasEnoughTickets(player, enchantLevel)) {
            event.setCancelled(true);
            player.sendMessage(config.getString("messages.insufficient-tickets"));
            return;
        }

        enchantManager.deductTickets(player, enchantLevel);
        player.sendMessage(config.getString("messages.enchant-success"));
    }
}
