package org.milanhdev.librarianPlus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class GUICommand implements CommandExecutor {

    private final LibrarianPlus plugin;
    private final String GUI_TITLE = ChatColor.GRAY + "Select Enchantments";
    private final NamespacedKey selectedKey;
    private final Map<UUID, Set<Enchantment>> selectedEnchantments = new HashMap<>();
    public record EnchantInfo(String displayName, int maxLevel) {}

    public GUICommand(LibrarianPlus plugin) {
        this.plugin = plugin;
        this.selectedKey = new NamespacedKey(plugin, "selected_enchant");
    }

    private final Map<Enchantment, EnchantInfo> enchantmentInfoMap = Map.ofEntries(
            Map.entry(Enchantment.MENDING, new EnchantInfo("Mending", 1)),
            Map.entry(Enchantment.UNBREAKING, new EnchantInfo("Unbreaking", 3)),
            Map.entry(Enchantment.EFFICIENCY, new EnchantInfo("Efficiency", 5)),
            Map.entry(Enchantment.FORTUNE, new EnchantInfo("Fortune", 3)),
            Map.entry(Enchantment.LUCK_OF_THE_SEA, new EnchantInfo("Luck of the Sea", 3)),
            Map.entry(Enchantment.LURE, new EnchantInfo("Lure", 3)),
            Map.entry(Enchantment.SILK_TOUCH, new EnchantInfo("Silk Touch", 1)),
            Map.entry(Enchantment.AQUA_AFFINITY, new EnchantInfo("Aqua Affinity", 1)),
            Map.entry(Enchantment.BLAST_PROTECTION, new EnchantInfo("Blast Protection", 4)),
            Map.entry(Enchantment.DEPTH_STRIDER, new EnchantInfo("Depth Strider", 3)),
            Map.entry(Enchantment.FEATHER_FALLING, new EnchantInfo("Feather Falling", 4)),
            Map.entry(Enchantment.FIRE_PROTECTION, new EnchantInfo("Fire Protection", 4)),
            Map.entry(Enchantment.FROST_WALKER, new EnchantInfo("Frost Walker", 2)),
            Map.entry(Enchantment.PROJECTILE_PROTECTION, new EnchantInfo("Projectile Protection", 4)),
            Map.entry(Enchantment.PROTECTION, new EnchantInfo("Protection", 4)),
            Map.entry(Enchantment.RESPIRATION, new EnchantInfo("Respiration", 3)),
            Map.entry(Enchantment.THORNS, new EnchantInfo("Thorns", 3)),
            Map.entry(Enchantment.BANE_OF_ARTHROPODS, new EnchantInfo("Bane of Arthropods", 5)),
            Map.entry(Enchantment.FIRE_ASPECT, new EnchantInfo("Fire Aspect", 2)),
            Map.entry(Enchantment.KNOCKBACK, new EnchantInfo("Knockback", 2)),
            Map.entry(Enchantment.LOOTING, new EnchantInfo("Looting", 3)),
            Map.entry(Enchantment.SHARPNESS, new EnchantInfo("Sharpness", 5)),
            Map.entry(Enchantment.SMITE, new EnchantInfo("Smite", 5)),
            Map.entry(Enchantment.SWEEPING_EDGE, new EnchantInfo("Sweeping Edge", 3)),
            Map.entry(Enchantment.CHANNELING, new EnchantInfo("Channeling", 1)),
            Map.entry(Enchantment.FLAME, new EnchantInfo("Flame", 1)),
            Map.entry(Enchantment.IMPALING, new EnchantInfo("Impaling", 5)),
            Map.entry(Enchantment.INFINITY, new EnchantInfo("Infinity", 1)),
            Map.entry(Enchantment.LOYALTY, new EnchantInfo("Loyalty", 3)),
            Map.entry(Enchantment.MULTISHOT, new EnchantInfo("Multishot", 1)),
            Map.entry(Enchantment.PIERCING, new EnchantInfo("Piercing", 4)),
            Map.entry(Enchantment.POWER, new EnchantInfo("Power", 5)),
            Map.entry(Enchantment.PUNCH, new EnchantInfo("Punch", 2)),
            Map.entry(Enchantment.QUICK_CHARGE, new EnchantInfo("Quick Charge", 3)),
            Map.entry(Enchantment.RIPTIDE, new EnchantInfo("Riptide", 3)),
            Map.entry(Enchantment.BREACH, new EnchantInfo("Breach", 4)),
            Map.entry(Enchantment.DENSITY, new EnchantInfo("Density", 5)),
            Map.entry(Enchantment.LUNGE, new EnchantInfo("Lunge", 3))
    );

    private final List<Enchantment> enchantmentsList = List.of(
            Enchantment.MENDING, Enchantment.UNBREAKING, Enchantment.EFFICIENCY, Enchantment.FORTUNE,
            Enchantment.LUCK_OF_THE_SEA, Enchantment.LURE, Enchantment.SILK_TOUCH, Enchantment.AQUA_AFFINITY,
            Enchantment.BLAST_PROTECTION, Enchantment.DEPTH_STRIDER, Enchantment.FEATHER_FALLING,
            Enchantment.FIRE_PROTECTION, Enchantment.FROST_WALKER, Enchantment.PROJECTILE_PROTECTION,
            Enchantment.PROTECTION, Enchantment.RESPIRATION, Enchantment.THORNS,
            Enchantment.BANE_OF_ARTHROPODS, Enchantment.FIRE_ASPECT, Enchantment.KNOCKBACK, Enchantment.LOOTING,
            Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.SWEEPING_EDGE,
            Enchantment.CHANNELING, Enchantment.FLAME, Enchantment.IMPALING, Enchantment.INFINITY,
            Enchantment.LOYALTY, Enchantment.MULTISHOT, Enchantment.PIERCING, Enchantment.POWER,
            Enchantment.PUNCH, Enchantment.QUICK_CHARGE, Enchantment.RIPTIDE, Enchantment.BREACH,
            Enchantment.DENSITY, Enchantment.LUNGE
    );

    public Map<Enchantment, EnchantInfo> getEnchantmentInfoMap() {
        return enchantmentInfoMap;
    }

    public Map<UUID, Set<Enchantment>> getSelectedEnchantments() {
        return selectedEnchantments;
    }

    public NamespacedKey getSelectedKey() {
        return selectedKey;
    }

    public String getGuiTitle() {
        return GUI_TITLE;
    }

    public void openVillagerGUI(Player p) {
        Inventory gui = Bukkit.createInventory(p, 45, GUI_TITLE);

        for (Enchantment enchant : enchantmentsList) {
            boolean selected = selectedEnchantments
                    .getOrDefault(p.getUniqueId(), new HashSet<>()).contains(enchant);

            // Use correct record name here
            EnchantInfo info = enchantmentInfoMap.get(enchant);

            String displayName;
            if (info == null) {
                displayName = toPrettyName(enchant);
            } else {
                if (info.maxLevel() > 1) {
                    displayName = info.displayName() + " " + toRomanNumeral(info.maxLevel());
                } else {
                    displayName = info.displayName();
                }
            }

            gui.addItem(createEnchantItem(enchant, selected, displayName));
        }

        p.openInventory(gui);
    }

    private ItemStack createEnchantItem(Enchantment enchant, boolean selected, String displayName) {
        ItemStack item = new ItemStack(selected ? Material.LIME_STAINED_GLASS_PANE : Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + displayName);
        meta.getPersistentDataContainer().set(selectedKey, PersistentDataType.STRING, enchant.getKey().getKey());
        item.setItemMeta(meta);
        return item;
    }

    private String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    private String toPrettyName(Enchantment enchant) {
        EnchantInfo info = enchantmentInfoMap.get(enchant);
        if (info != null) {
            return info.displayName();
        } else {
            return defaultPrettyName(enchant);
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;
        openVillagerGUI(p);
        return true;
    }
}
