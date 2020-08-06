package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ArcherEvents implements Listener {

    private Plugin plugin;

    public ArcherEvents(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isArcher(Player p) {
        return BasicEvents.getPlayerClassMap().get(p.getName()).equals("Archer");
    }

    // Basic Event

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!isArcher(p)) return;
        BasicEvents.addExp(plugin, p, 5);
        Projectile projectile = (Projectile) e.getProjectile();
        projectile.setCustomName("Shooter=" + p.getUniqueId().toString());

        int random = (int) (Math.random() * 100) + 1;
        int chance;
        if (plugin.getConfig().getInt("players." + p.getName() + ".Level") > 50) chance = 7;
        else chance = 3;
        if (plugin.getConfig().getInt("players." + p.getName() + ".Level") == 187) chance = 100;

        if (random > chance) return;

        Vector targetVector = null;
        int radiusX = 10;
        int radiusY = 10;
        int radiusZ = 10;
        float yaw = p.getLocation().getYaw();

        root:
        while (true) {
            for (Entity entity : projectile.getNearbyEntities(radiusX, radiusY, radiusZ)) {
                if (entity instanceof LivingEntity && !entity.getName().equals(p.getName())) {
                    targetVector = entity.getLocation().toVector();
                    break root;
                }
            }

            if (yaw > 225 && yaw <= 315) {
                //+X
                radiusX += 5;
            } else if (yaw > 315 || yaw <= 45) {
                //+Z
                radiusZ += 5;
            } else if (yaw > 45 && yaw <= 135) {
                //-X
                radiusX += 5;
            } else {
                //-Z
                radiusZ += 5;
            }

            if (radiusX > 50 || radiusZ > 50) return;
        }

        if (targetVector != null) projectile.setVelocity(targetVector.subtract(p.getLocation().toVector()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return;
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getEntity();
        if (arrow.getCustomName() == null || !arrow.getCustomName().startsWith("Shooter=")) return;
        Player p = Bukkit.getPlayer(UUID.fromString(arrow.getCustomName().split("=")[1]));
        int level = plugin.getConfig().getInt("players." + p.getName() + ".Level");
        if (level < 10) e.setDamage(e.getDamage() * 1.2);
        else if (level < 50) e.setDamage(e.getDamage() * 1.25);
        else e.setDamage(e.getDamage() * 1.3);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getSlotType() != InventoryType.SlotType.ARMOR) return;
        Player p = (Player) e.getWhoClicked();
        if (!isArcher(p)) return;
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        String durabilityString = "Decreased Durability Points: " + 2;
        if (clickedItem.getType() != Material.AIR && (cursorItem.getType() == Material.AIR || isArmor(cursorItem.getType()))) {
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore() && clickedItem.getItemMeta().getLore().contains(durabilityString)) {
                ItemMeta clickedMeta = e.getCurrentItem().getItemMeta();
                List<String> lores = clickedMeta.getLore();
                lores.remove(durabilityString);
                clickedMeta.setLore(lores);
                clickedItem.setItemMeta(clickedMeta);
                clickedItem.setDurability((short) (clickedItem.getDurability() - 2));
            }
        }
        if (isArmor(cursorItem.getType()) && plugin.getConfig().getInt("players." + p.getName() + "Level") < 20) {
            ItemMeta meta = cursorItem.getItemMeta();
            List<String> lores = meta.getLore();
            if (lores == null) lores = new ArrayList<>();
            if (lores.isEmpty() || !lores.contains(durabilityString)) {
                meta.setLore(Arrays.asList(durabilityString));
                cursorItem.setItemMeta(meta);
                cursorItem.setDurability((short) (cursorItem.getDurability() + 2));
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
