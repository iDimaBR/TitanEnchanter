package com.github.iDimaBR.titanEnchanter;

import com.github.iDimaBR.titanEnchanter.listeners.EnchantmentListener;
import com.github.iDimaBR.titanEnchanter.listeners.InventoryClickListener;
import com.github.iDimaBR.titanEnchanter.listeners.InventoryCloseListener;
import com.github.iDimaBR.titanEnchanter.listeners.NPCClickListener;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.managers.GUIManager;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TitanEnchanter extends JavaPlugin {

    @Getter
    private static TitanEnchanter instance;
    private ConfigUtil config;
    private EnchantManager enchantManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        this.config = new ConfigUtil(this, "config.yml");

        this.enchantManager = new EnchantManager(this);
        this.guiManager = new GUIManager(this);

        getServer().getPluginManager().registerEvents(new NPCClickListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantmentListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);

        getLogger().info("TitanEnchanter has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TitanEnchanter has been disabled!");
    }
}