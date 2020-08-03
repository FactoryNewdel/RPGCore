package de.tim.rpgcore;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnightEvents implements Listener {

    private Main plugin;


    public KnightEvents(Main plugin) {
        this.plugin = plugin;
    }

    private boolean isKnight(Player p) {
        return BasicEvents.getPlayerClassMap().get(p.getName()).equals("Knight");
    }


    @EventHandler(ignoreCancelled = true)
    public void onKnightDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (isKnight(p)) {
            e.setDamage(e.getDamage() * 1.02);
        }

        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        if (livingEntity.getHealth() - e.getDamage() > 0) return;
        if (livingEntity instanceof Player) {
            BasicEvents.addExp(plugin, p, 20);
        } else if (livingEntity instanceof Monster) {
            if (livingEntity instanceof EnderDragon || livingEntity instanceof Wither) {
                BasicEvents.addExp(plugin, p, 100);
            } else {
                BasicEvents.addExp(plugin, p, 10);
            }
        } else {
            BasicEvents.addExp(plugin, p, 5);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (!isKnight((Player) e.getWhoClicked())) return;
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        String durabilityString = "Extra Durability Points: " + 1;
        if (clickedItem.getType() != Material.AIR && (cursorItem.getType() == Material.AIR || isArmor(cursorItem.getType()))) {
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore() && clickedItem.getItemMeta().getLore().contains(durabilityString)) {
                ItemMeta clickedMeta = e.getCurrentItem().getItemMeta();
                List<String> lores = clickedMeta.getLore();
                lores.remove(durabilityString);
                clickedMeta.setLore(lores);
                clickedItem.setItemMeta(clickedMeta);
                clickedItem.setDurability((short) (clickedItem.getDurability() + 1));
            }
        }
        if (isArmor(cursorItem.getType())) {
            ItemMeta meta = cursorItem.getItemMeta();
            List<String> lores = meta.getLore();
            if (lores == null) lores = new ArrayList<>();
            if (lores.isEmpty() || !lores.contains(durabilityString)) {
                meta.setLore(Arrays.asList(durabilityString));
                cursorItem.setItemMeta(meta);
                cursorItem.setDurability((short) (cursorItem.getDurability() - 1));
            }
        }

    }

    private boolean isArmor(Material material) {
        ItemStack armor = new ItemStack(material);
        try {
            armor.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            return true;
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }
}
