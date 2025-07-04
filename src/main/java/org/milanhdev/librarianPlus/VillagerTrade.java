package org.milanhdev.librarianPlus;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VillagerTrade implements Listener {

    private final LibrarianPlus plugin;
    private final GUICommand guiCommand;

    public VillagerTrade(LibrarianPlus plugin, GUICommand guiCommand) {
        this.plugin = plugin;
        this.guiCommand = guiCommand;
    }

    private final Map<Location, UUID> lecternPlacers = new HashMap<>();

    @EventHandler
    public void onLecternPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.LECTERN) {
            lecternPlacers.put(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onLecternBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.LECTERN) {
            lecternPlacers.remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        if (event.getProfession() == Villager.Profession.LIBRARIAN) {
            Villager villager = event.getEntity();
            Location villagerLoc = villager.getLocation();

            UUID playerUUID = null;
            double closestDistanceSquared = 16.0;

            for (Map.Entry<Location, UUID> entry : lecternPlacers.entrySet()) {
                double distanceSquared = entry.getKey().distanceSquared(villagerLoc);
                if (distanceSquared < closestDistanceSquared) {
                    playerUUID = entry.getValue();
                    closestDistanceSquared = distanceSquared;
                }
            }

            if (playerUUID == null) return;

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                checkVillagerTrades(villager, player);
            }, 4L);
        }
    }

    private void checkVillagerTrades(Villager villager, Player p) {
        Set<Enchantment> selected = guiCommand.getSelectedEnchantments().getOrDefault(p.getUniqueId(), Set.of());

        if (selected.isEmpty()) return;

        for (MerchantRecipe recipe : villager.getRecipes()) {
            ItemStack result = recipe.getResult();
            if (result.getType() == Material.ENCHANTED_BOOK && result.hasItemMeta()) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
                for (Map.Entry<Enchantment, Integer> enchant : meta.getStoredEnchants().entrySet()) {
                    if (selected.contains(enchant.getKey())) {
                        GUICommand.EnchantInfo info = guiCommand.getEnchantmentInfoMap().get(enchant.getKey());
                        int maxSelectedLevel = (info != null) ? info.maxLevel() : 1;
                        int offeredLevel = enchant.getValue();

                        if (offeredLevel >= maxSelectedLevel) {
                            String formattedName = formatEnchantmentName(enchant.getKey());
                            String levelDisplay = (maxSelectedLevel > 1) ? " " + toRomanNumeral(offeredLevel) : "";
                            p.sendMessage(ChatColor.GREEN + "Found enchantment: " + ChatColor.LIGHT_PURPLE + formattedName + levelDisplay);
                            villager.getWorld().playSound(villager.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.2f, 1.0f);
                            return;
                        }
                    }
                }
            }
        }

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.4f, 1.0f);
    }

    private String formatEnchantmentName(Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        String[] words = key.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1).toLowerCase());
                }
                formatted.append(" ");
            }
        }

        return formatted.toString().trim();
    }

    private String toRomanNumeral(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number); // fallback for levels > 5
        }
    }
}
