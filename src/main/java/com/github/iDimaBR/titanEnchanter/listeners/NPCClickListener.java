package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.utils.ConfigUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class NPCClickListener implements Listener {

    private final TitanEnchanter plugin;
    private final ConfigUtil config;

    public NPCClickListener(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();
        int npcId = npc.getId();

        List<Integer> configuredNPCs = config.getIntegerList("npc-ids");
        if (configuredNPCs.contains(npcId)) {
            event.setCancelled(true);
            plugin.getGuiManager().openMainMenu(player);
        }
    }
}