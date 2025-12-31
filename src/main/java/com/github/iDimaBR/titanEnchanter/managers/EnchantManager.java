package com.github.iDimaBR.titanEnchanter.managers;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import com.github.iDimaBR.titanEnchanter.models.EnchantMenuHolder;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantManager {

    private final TitanEnchanter plugin;
    private final ConfigUtil config;
    private final Map<UUID, EnchantLevel> playerLevels;

    public EnchantManager(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.playerLevels = new HashMap<>();
    }

    public void openEnchantingTable(Player player, EnchantLevel level) {
        playerLevels.put(player.getUniqueId(), level);
        player.closeInventory();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.openEnchanting(null, true);
                }
            }
        }.runTaskLater(plugin, 3L);
    }

    public EnchantLevel getPlayerLevel(UUID uuid) {
        return playerLevels.get(uuid);
    }

    public void removePlayerLevel(UUID uuid) {
        playerLevels.remove(uuid);
    }

    public boolean hasEnoughTickets(Player player, int requiredLevel) {
        // TODO: Integrate ticket API
        return player.getLevel() >= requiredLevel;
    }

    public void deductTickets(Player player, int amount) {
        // TODO: Integrate ticket API
        player.setLevel(player.getLevel() - amount);

        String message = config.getString("messages.tickets-deducted")
                .replace("{amount}", String.valueOf(amount));
        player.sendMessage(message);
    }

    public boolean isValidLevel(int level, EnchantLevel enchantLevel) {
        return level >= enchantLevel.getMinLevel() && level <= enchantLevel.getMaxLevel();
    }
}