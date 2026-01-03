package com.github.iDimaBR.titanEnchanter.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PacketManager {

    private final TitanEnchanter plugin;
    private final Map<UUID, EnchantmentData[]> playerEnchantments;
    private final Map<String, Integer> enchantmentIds;
    private final ProtocolManager protocolManager;

    public PacketManager(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.playerEnchantments = new HashMap<>();
        this.enchantmentIds = initializeEnchantmentIds();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
    }

    private void registerPacketListener() {
        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.WINDOW_DATA) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.isCancelled()) return;

                        Player player = event.getPlayer();
                        EnchantmentData[] offers = playerEnchantments.get(player.getUniqueId());

                        if (offers == null) return;

                        PacketContainer packet = event.getPacket();
                        int windowId = packet.getIntegers().read(0);
                        int property = packet.getIntegers().read(1);
                        int value = packet.getIntegers().read(2);
                        if (property < 0 || property > 9) return;

                        if (value == -1 && property >= 4) {
                            playerEnchantments.remove(player.getUniqueId());
                            return;
                        }

                        if (property >= 4 && property <= 6) {
                            int slot = property - 4;
                            if (slot < offers.length) {
                                int enchantId = getEnchantmentId(offers[slot].enchantment);
                                packet.getIntegers().write(2, enchantId);
                            }
                        } else if (property >= 7 && property <= 9) {
                            int slot = property - 7;
                            if (slot < offers.length) {
                                packet.getIntegers().write(2, offers[slot].level);
                            }
                        } else if (property >= 0 && property <= 2) {
                            if (property < offers.length) {
                                packet.getIntegers().write(2, offers[property].cost);
                            }
                        }
                    }
                }
        );
    }

    public void savePlayerOffers(UUID playerId, EnchantmentData[] offers) {
        playerEnchantments.put(playerId, offers);
    }

    public void sendCustomOffers(Player player, EnchantmentData[] offers) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Enviar custos
                    for (int i = 0; i < 3; i++) {
                        sendWindowData(player, i, offers[i].cost);
                    }

                    // Enviar encantamentos
                    for (int i = 0; i < 3; i++) {
                        int enchantId = getEnchantmentId(offers[i].enchantment);
                        sendWindowData(player, 4 + i, enchantId);
                    }

                    // Enviar níveis
                    for (int i = 0; i < 3; i++) {
                        sendWindowData(player, 7 + i, offers[i].level);
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error sending packets: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    private void sendWindowData(Player player, int property, int value) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WINDOW_DATA);

        packet.getIntegers()
                .write(0, 0) // Window ID
                .write(1, property)
                .write(2, value);

        protocolManager.sendServerPacket(player, packet);
    }

    public EnchantmentData[] getPlayerOffers(UUID playerId) {
        return playerEnchantments.get(playerId);
    }

    public EnchantmentData getPlayerOffer(UUID playerId, int slot) {
        EnchantmentData[] offers = playerEnchantments.get(playerId);
        if (offers == null || slot < 0 || slot >= offers.length) {
            return null;
        }
        return offers[slot];
    }

    public void clearPlayerOffers(UUID playerId) {
        playerEnchantments.remove(playerId);
    }

    /**
     * Reseta visualmente a mesa de encantamento, limpando todas as ofertas
     */
    public void resetEnchantingTable(Player player) {
        try {
            // Enviar -1 para todas as properties para limpar a interface
            // Properties 0, 1, 2 (custos)
            for (int i = 0; i < 3; i++) {
                sendWindowData(player, i, 0);
            }

            // Properties 4, 5, 6 (IDs dos encantamentos)
            for (int i = 4; i < 7; i++) {
                sendWindowData(player, i, -1);
            }

            // Properties 7, 8, 9 (níveis)
            for (int i = 7; i < 10; i++) {
                sendWindowData(player, i, -1);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error on reset table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getEnchantmentId(Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        return enchantmentIds.getOrDefault(key, 0);
    }

    private Map<String, Integer> initializeEnchantmentIds() {
        Map<String, Integer> ids = new HashMap<>();
        ids.put("protection", 0);
        ids.put("fire_protection", 1);
        ids.put("feather_falling", 2);
        ids.put("blast_protection", 3);
        ids.put("projectile_protection", 4);
        ids.put("respiration", 5);
        ids.put("aqua_affinity", 6);
        ids.put("thorns", 7);
        ids.put("depth_strider", 8);
        ids.put("frost_walker", 9);
        ids.put("binding_curse", 10);
        ids.put("soul_speed", 11);
        ids.put("swift_sneak", 12);
        ids.put("sharpness", 13);
        ids.put("smite", 14);
        ids.put("bane_of_arthropods", 15);
        ids.put("knockback", 16);
        ids.put("fire_aspect", 17);
        ids.put("looting", 18);
        ids.put("sweeping", 19);
        ids.put("efficiency", 20);
        ids.put("silk_touch", 21);
        ids.put("unbreaking", 22);
        ids.put("fortune", 23);
        ids.put("power", 24);
        ids.put("punch", 25);
        ids.put("flame", 26);
        ids.put("infinity", 27);
        ids.put("luck_of_the_sea", 28);
        ids.put("lure", 29);
        ids.put("loyalty", 30);
        ids.put("impaling", 31);
        ids.put("riptide", 32);
        ids.put("channeling", 33);
        ids.put("multishot", 34);
        ids.put("quick_charge", 35);
        ids.put("piercing", 36);
        ids.put("mending", 37);
        ids.put("vanishing_curse", 38);

        return ids;
    }

    public static class EnchantmentData {
        public final Enchantment enchantment;
        public final int level;
        public final int cost;

        public EnchantmentData(Enchantment enchantment, int level, int cost) {
            this.enchantment = enchantment;
            this.level = level;
            this.cost = cost;
        }
    }
}