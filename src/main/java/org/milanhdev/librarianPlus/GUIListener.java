package org.milanhdev.librarianPlus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class GUIListener implements Listener {

    private final GUICommand guiCommand;

    public GUIListener(GUICommand guiCommand) {
        this.guiCommand = guiCommand;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player p)) return;
        if (event.getView().getTitle().equals(guiCommand.getGuiTitle())) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String enchantId = clicked.getItemMeta()
                    .getPersistentDataContainer()
                    .get(guiCommand.getSelectedKey(), PersistentDataType.STRING);

            if (enchantId == null) return;

            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantId));
            if (enchantment == null) return;

            GUICommand.EnchantInfo info = guiCommand.getEnchantmentInfoMap().get(enchantment);

            Set<Enchantment> selected = guiCommand.getSelectedEnchantments()
                    .computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());

            if (selected.contains(enchantment)) {
                selected.remove(enchantment);
                clicked.setType(Material.ENCHANTED_BOOK);
                p.sendMessage(ChatColor.RED + "Unselected: " + getDisplayName(enchantment));
            } else {
                selected.add(enchantment);
                clicked.setType(Material.LIME_STAINED_GLASS_PANE);
                p.sendMessage(ChatColor.GREEN + "Selected: " + getDisplayName(enchantment));
            }

            event.getInventory().setItem(event.getSlot(), clicked);
            p.playSound(p.getLocation(), Material.LIME_STAINED_GLASS_PANE.createBlockData().getSoundGroup().getPlaceSound(), 0.3f, 1.0f);
        }
    }

    private String defaultPrettyName(Enchantment enchant) {
        String[] parts = enchant.getKey().getKey().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase())
                    .append(" ");
        }
        return builder.toString().trim();
    }

    private String getDisplayName(Enchantment enchant) {
        GUICommand.EnchantInfo info = guiCommand.getEnchantmentInfoMap().get(enchant);
        if (info != null) {
            return info.displayName();
        } else {
            return defaultPrettyName(enchant);
        }
    }
}
