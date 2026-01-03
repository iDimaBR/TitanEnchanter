package com.github.iDimaBR.titanEnchanter;

import com.github.iDimaBR.titanEnchanter.listeners.EnchantmentListener;
import com.github.iDimaBR.titanEnchanter.listeners.InventoryClickListener;
import com.github.iDimaBR.titanEnchanter.listeners.InventoryCloseListener;
import com.github.iDimaBR.titanEnchanter.listeners.NPCClickListener;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.managers.GUIManager;
import com.github.iDimaBR.titanEnchanter.managers.PacketManager;
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
    private PacketManager packetManager;

    @Override
    public void onEnable() {
        instance = this;

        // Verificar se ProtocolLib está instalado
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib não encontrado! Este plugin requer ProtocolLib para funcionar.");
            getLogger().severe("Baixe em: https://www.spigotmc.org/resources/protocollib.1997/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.config = new ConfigUtil(this, "config.yml");

        this.enchantManager = new EnchantManager(this);
        this.guiManager = new GUIManager(this);
        this.packetManager = new PacketManager(this);

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