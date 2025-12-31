package com.github.iDimaBR.titanEnchanter.managers;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import com.github.iDimaBR.titanEnchanter.models.EnchanterHolder;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final TitanEnchanter plugin;
    private final ConfigUtil config;

    public GUIManager(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void openMainMenu(Player player) {
        String title = config.getString("menu.title", "");
        int size = config.getInt("menu.size", 27);

        EnchanterHolder holder = new EnchanterHolder(null);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        for (EnchantLevel level : EnchantLevel.values()) {
            String key = level.getConfigKey();
            int slot = config.getInt("menu.level-ranges." + key + ".slot");
            inv.setItem(slot, createLevelRangeItem(level));
        }

        player.openInventory(inv);
    }

    private ItemStack createLevelRangeItem(EnchantLevel level) {
        String key = level.getConfigKey();
        String materialName = config.getString("menu.level-ranges." + key + ".material", "ENCHANTED_BOOK");

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.ENCHANTED_BOOK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String title = config.getString("menu.level-ranges." + key + ".title");
        meta.setDisplayName(title);

        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("menu.level-ranges." + key + ".lore")) {
            lore.add(line
                    .replace("{min}", String.valueOf(level.getMinLevel()))
                    .replace("{max}", String.valueOf(level.getMaxLevel())));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public EnchantLevel getLevelFromSlot(int slot) {
        for (EnchantLevel level : EnchantLevel.values()) {
            String key = level.getConfigKey();
            int configSlot = config.getInt("menu.level-ranges." + key + ".slot");
            if (slot == configSlot) {
                return level;
            }
        }
        return null;
    }

    public boolean isMainMenu(Inventory inventory) {
        return inventory.getHolder() instanceof EnchanterHolder;
    }
}