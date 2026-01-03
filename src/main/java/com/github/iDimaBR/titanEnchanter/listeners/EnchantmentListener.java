package com.github.iDimaBR.titanEnchanter.listeners;

import com.github.iDimaBR.titanEnchanter.TitanEnchanter;
import com.github.iDimaBR.titanEnchanter.managers.EnchantManager;
import com.github.iDimaBR.titanEnchanter.managers.PacketManager;
import com.github.iDimaBR.titanEnchanter.models.EnchantLevel;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantmentListener implements Listener {

    private final TitanEnchanter plugin;
    private final EnchantManager enchantManager;
    private final PacketManager packetManager;
    private final Random random;

    public EnchantmentListener(TitanEnchanter plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantManager();
        this.packetManager = plugin.getPacketManager();
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());

        if (level == null) return;

        int[] costs = level.getOfferedCosts();
        ItemStack item = event.getItem();

        PacketManager.EnchantmentData[] customOffers = new PacketManager.EnchantmentData[3];
        EnchantmentOffer[] vanillaOffers = event.getOffers();

        for (int i = 0; i < 3; i++) {
            List<Enchantment> possibleEnchants = getPossibleEnchantments(item);

            if (possibleEnchants.isEmpty()) {
                if (vanillaOffers[i] != null) {
                    customOffers[i] = new PacketManager.EnchantmentData(
                            vanillaOffers[i].getEnchantment(),
                            vanillaOffers[i].getEnchantmentLevel(),
                            costs[i]
                    );
                } else {
                    customOffers[i] = new PacketManager.EnchantmentData(
                            Enchantment.PROTECTION_ENVIRONMENTAL,
                            1,
                            costs[i]
                    );
                }
            } else {
                Enchantment enchant = possibleEnchants.get(random.nextInt(possibleEnchants.size()));
                int enchantLevel = getRandomEnchantLevel(enchant, level.getMinLevel(), level.getMaxLevel());

                customOffers[i] = new PacketManager.EnchantmentData(
                        enchant,
                        enchantLevel,
                        costs[i]
                );
            }
        }

        // Salvar as ofertas ANTES de aplicar
        packetManager.savePlayerOffers(player.getUniqueId(), customOffers);

        // Aplicar as ofertas
        applyOffers(event, customOffers);

        // Enviar pacotes customizados
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            packetManager.sendCustomOffers(player, customOffers);
        }, 1L);
    }

    /**
     * Aplica as ofertas no evento vanilla
     */
    private void applyOffers(PrepareItemEnchantEvent event, PacketManager.EnchantmentData[] offers) {
        EnchantmentOffer[] vanillaOffers = event.getOffers();
        for (int i = 0; i < 3; i++) {
            vanillaOffers[i] = new EnchantmentOffer(
                    offers[i].enchantment,
                    offers[i].level,
                    offers[i].cost
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        EnchantLevel level = enchantManager.getPlayerLevel(player.getUniqueId());

        if (level == null) return;
        event.setCancelled(true);

        // Obter a oferta específica que o jogador selecionou
        int whichButton = event.whichButton();
        PacketManager.EnchantmentData offer = packetManager.getPlayerOffer(
                player.getUniqueId(),
                whichButton
        );

        if (offer == null) {
            player.closeInventory();
            return;
        }

        // Verificar se o jogador tem tickets suficientes
        if (!enchantManager.hasEnoughTickets(player, offer.cost)) {
            String message = plugin.getConfig().getString("messages.insufficient-tickets");
            player.sendMessage(message);
            player.closeInventory();
            return;
        }

        // Obter o item do inventário de encantamento
        ItemStack item = event.getItem();

        // Aplicar o encantamento principal
        item.addUnsafeEnchantment(offer.enchantment, offer.level);

        // Gerar encantamentos bônus (como no vanilla)
        List<Enchantment> bonusEnchantments = generateBonusEnchantments(item, offer.enchantment, offer.cost);
        for (Enchantment bonus : bonusEnchantments) {
            // Bônus têm níveis mais baixos (máximo baseado no custo)
            int maxBonusLevel = Math.min(bonus.getMaxLevel(), getBonusMaxLevel(offer.cost));
            int bonusLevel = 1 + random.nextInt(maxBonusLevel);
            item.addUnsafeEnchantment(bonus, bonusLevel);
        }

        // Deduzir os níveis de experiência do jogador
        int currentLevel = player.getLevel();
        player.setLevel(Math.max(0, currentLevel - offer.cost));

        // Deduzir os tickets
        enchantManager.deductTickets(player, offer.cost);

        // Limpar as ofertas e resetar a mesa visualmente
        packetManager.clearPlayerOffers(player.getUniqueId());

        // Enviar pacotes de reset para limpar a interface
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            packetManager.resetEnchantingTable(player);
        }, 1L);
    }

    /**
     * Gera encantamentos bônus baseado no custo (como no vanilla)
     */
    private List<Enchantment> generateBonusEnchantments(ItemStack item, Enchantment mainEnchant, int cost) {
        List<Enchantment> bonusEnchants = new ArrayList<>();

        // Chance de encantamentos bônus aumenta com o custo
        // Custo 25-30: ~40-60% de chance de ter 1-2 encantamentos bônus
        double bonusChance = (cost / 30.0) * 0.6; // 0% a 25 cost → 60% a 30 cost

        if (random.nextDouble() > bonusChance) {
            return bonusEnchants; // Sem bônus
        }

        // Obter encantamentos possíveis (já filtra encantamentos de tesouro)
        List<Enchantment> possibleBonus = getPossibleEnchantments(item);

        // Remover o encantamento principal e conflitantes
        possibleBonus.removeIf(e ->
                e.equals(mainEnchant) ||
                        e.conflictsWith(mainEnchant) ||
                        item.getEnchantments().containsKey(e)
        );

        if (possibleBonus.isEmpty()) {
            return bonusEnchants;
        }

        // Adicionar 1-2 encantamentos bônus
        int maxBonus = cost >= 27 ? 2 : 1; // Custos mais altos podem ter 2 bônus
        int bonusCount = random.nextInt(maxBonus) + 1;

        for (int i = 0; i < bonusCount && !possibleBonus.isEmpty(); i++) {
            Enchantment bonus = possibleBonus.remove(random.nextInt(possibleBonus.size()));

            // Verificar conflitos com encantamentos já adicionados
            boolean hasConflict = false;
            for (Enchantment existing : bonusEnchants) {
                if (bonus.conflictsWith(existing)) {
                    hasConflict = true;
                    break;
                }
            }

            if (!hasConflict) {
                bonusEnchants.add(bonus);
                // Remover encantamentos conflitantes da lista
                possibleBonus.removeIf(e -> e.conflictsWith(bonus));
            }
        }

        return bonusEnchants;
    }

    private List<Enchantment> getPossibleEnchantments(ItemStack item) {
        List<Enchantment> possible = new ArrayList<>();

        for (Enchantment enchant : Enchantment.values()) {
            // Filtrar encantamentos de tesouro (não podem vir de mesa de encantamento)
            if (isTreasureEnchantment(enchant)) {
                continue;
            }

            if (enchant.canEnchantItem(item)) {
                boolean hasConflict = false;
                for (Enchantment existing : item.getEnchantments().keySet()) {
                    if (enchant.conflictsWith(existing)) {
                        hasConflict = true;
                        break;
                    }
                }

                if (!hasConflict) {
                    possible.add(enchant);
                }
            }
        }

        return possible;
    }

    /**
     * Verifica se é um encantamento de tesouro (não disponível em mesa de encantamento)
     */
    private boolean isTreasureEnchantment(Enchantment enchant) {
        String key = enchant.getKey().getKey();

        // Encantamentos de tesouro que NÃO podem vir de mesa de encantamento
        return key.equals("mending") ||                    // Remendo
                key.equals("frost_walker") ||               // Passos Gélidos
                key.equals("binding_curse") ||              // Maldição da Ligação
                key.equals("vanishing_curse") ||            // Maldição do Desaparecimento
                key.equals("soul_speed") ||                 // Velocidade da Alma
                key.equals("swift_sneak");                  // Esgueirar Veloz
    }

    private int getRandomEnchantLevel(Enchantment enchant, int minPower, int maxPower) {
        int maxLevel = enchant.getMaxLevel();

        if (maxLevel == 1) {
            return 1;
        }

        // Calcular o nível máximo permitido baseado no poder da mesa
        // Fórmula similar ao vanilla:
        // Nível 1-6: máximo nível 1-2
        // Nível 7-12: máximo nível 2-3
        // Nível 13-18: máximo nível 3-4
        // Nível 19-24: máximo nível 4-5
        // Nível 25-30: máximo nível 5 (nível máximo do encantamento)

        int allowedMaxLevel;
        if (maxPower <= 6) {
            allowedMaxLevel = Math.min(2, maxLevel);
        } else if (maxPower <= 12) {
            allowedMaxLevel = Math.min(3, maxLevel);
        } else if (maxPower <= 18) {
            allowedMaxLevel = Math.min(4, maxLevel);
        } else if (maxPower <= 24) {
            allowedMaxLevel = Math.min(5, maxLevel);
        } else {
            allowedMaxLevel = maxLevel; // 25-30: qualquer nível
        }

        // Chance maior de níveis altos quanto maior o poder
        double powerRatio = (maxPower - minPower + 1) / 30.0;

        // Gerar nível entre 1 e o máximo permitido
        if (allowedMaxLevel == 1) {
            return 1;
        }

        int level = 1 + random.nextInt(allowedMaxLevel);

        // Com alto poder, aumentar chance de pegar o nível máximo
        if (powerRatio > 0.75 && level < allowedMaxLevel && random.nextDouble() < 0.4) {
            level = allowedMaxLevel;
        }

        return level;
    }

    /**
     * Retorna o nível máximo permitido para encantamentos bônus baseado no custo
     */
    private int getBonusMaxLevel(int cost) {
        if (cost <= 10) return 1;
        if (cost <= 20) return 2;
        return 3; // Máximo nível 3 para bônus
    }
}