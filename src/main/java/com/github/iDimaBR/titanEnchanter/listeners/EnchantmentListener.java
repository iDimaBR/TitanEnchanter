package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantmentListener implements Listener {

    private final EnchantManager enchantManager;
    private final ConfigUtil config;
    private final Random random;

    public EnchantmentListener(TitanEnchanter plugin) {
        this.enchantManager = plugin.getEnchantManager();
        this.config = plugin.getConfig();
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());
        if (level == null) return;

        event.setCancelled(false);

        int[] costs = level.getOfferedCosts();
        int[] offeredLevels = event.getExpLevelCostsOffered();

        for (int i = 0; i < 3; i++) {
            offeredLevels[i] = costs[i];
        }

        EnchantmentOffer[] offers = event.getOffers();
        List<Enchantment> validEnchants = getValidEnchantments(event.getItem());
        for (int i = 0; i < 3; i++) {
            if (validEnchants.isEmpty()) break;

            Enchantment enchantment = validEnchants.get(random.nextInt(validEnchants.size()));
            int enchantLevel = calculateEnchantmentLevel(enchantment, costs[i]);
            if (offers[i] == null) {
                offers[i] = new EnchantmentOffer(enchantment, enchantLevel, costs[i]);
            } else {
                offers[i].setEnchantment(enchantment);
                offers[i].setEnchantmentLevel(enchantLevel);
                offers[i].setCost(costs[i]);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());
        if (level == null) return;

        int enchantLevel = event.whichButton() + 1;
        int[] costs = level.getOfferedCosts();
        int actualCost = costs[enchantLevel - 1];

        if (!enchantManager.isValidLevel(actualCost, level)) {
            event.setCancelled(true);
            if(config.getBoolean("messages.invalid-level.enabled")) {
                String message = config.getString("messages.invalid-level.text")
                        .replace("{min}", String.valueOf(level.getMinLevel()))
                        .replace("{max}", String.valueOf(level.getMaxLevel()));
                player.sendMessage(message);
            }
            return;
        }

        if (!enchantManager.hasEnoughTickets(player, actualCost)) {
            event.setCancelled(true);
            player.sendMessage(config.getString("messages.insufficient-tickets"));
            return;
        }

        enchantManager.deductTickets(player, actualCost);
        player.sendMessage(config.getString("messages.enchant-success"));
    }

    private List<Enchantment> getValidEnchantments(org.bukkit.inventory.ItemStack item) {
        List<Enchantment> valid = new ArrayList<>();

        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment.canEnchantItem(item)) {
                valid.add(enchantment);
            }
        }

        return valid;
    }

    private int calculateEnchantmentLevel(Enchantment enchantment, int cost) {
        int maxLevel = enchantment.getMaxLevel();

        if (cost >= 25) {
            return maxLevel;
        } else if (cost >= 19) {
            return Math.max(1, maxLevel - random.nextInt(2));
        } else if (cost >= 13) {
            return Math.max(1, maxLevel / 2 + random.nextInt(maxLevel / 2 + 1));
        } else if (cost >= 7) {
            return Math.max(1, random.nextInt(maxLevel) + 1);
        } else {
            return Math.max(1, random.nextInt(Math.min(2, maxLevel)) + 1);
        }
    }
}