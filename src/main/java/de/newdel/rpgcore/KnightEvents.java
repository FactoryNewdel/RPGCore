package de.newdel.rpgcore;

import de.newdel.rpgcore.KnightCommands.KnightAbility;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnightEvents implements Listener {

    private Main plugin;


    public KnightEvents(Main plugin) {
        this.plugin = plugin;
    }

    public static boolean isKnight(Player p) {
        System.out.println(BasicEvents.getPlayerClassMap());
        return BasicEvents.getPlayerClassMap().get(p.getName()).equals("Knight");
    }

    // Abilities

    @EventHandler
    public void onSwordDash(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof LivingEntity)
                || !KnightCommands.hasAbilityActivated(e.getPlayer(), KnightAbility.getByName("sworddash"))) return;
        LivingEntity entity = (LivingEntity) e.getRightClicked();
        ItemStack item = e.getPlayer().getItemInHand();
        entity.damage(getSwordAttackDmg(item.getType()) + 30);
        KnightCommands.endPlayerAbility(e.getPlayer(), KnightAbility.getByName("sworddash"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEasyCrit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (!isKnight(p)) return;
        if (!KnightCommands.hasAbilityActivated(p, KnightAbility.getByName("easycrit"))) return;
        if (isSword(p.getItemInHand())) {
            e.setDamage(e.getDamage() * 1.3);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStunBlow(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        Player target = (Player) e.getEntity();
        if (!isKnight(p)) return;
        if (!KnightCommands.hasAbilityActivated(p, KnightAbility.getByName("stunblow"))) return;
        if (!isSword(p.getItemInHand())) return;
        if (p.getVelocity().getY() > 0) return;
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 20, 1));
        KnightCommands.endPlayerAbility(p, KnightAbility.getByName("stunblow"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBleed(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        Player target = (Player) e.getEntity();
        if (!isKnight(p)) return;
        if (!KnightCommands.canUse(plugin, p, KnightAbility.BLEED)) return;
        if (!isSword(p.getItemInHand())) return;
        int random = (int) (Math.random() * 100 + 1);
        if (random <= 5) {
            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                target.setHealth(target.getHealth() - 1);
                target.getWorld().playEffect(target.getLocation(), Effect.MOBSPAWNER_FLAMES, 2004);
            }, 0L, 1 * 20L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getScheduler().cancelTask(taskId);
            }, 10 * 20L);
        }
    }

    // Basic stuff

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
        Player p = (Player) e.getWhoClicked();
        if (!isKnight(p)) return;
        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        String durabilityString = "Extra Durability Points: " + 1;
        if (clickedItem.getType() != Material.AIR && (cursorItem.getType() == Material.AIR || isArmor(cursorItem.getType()))) {
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore() && clickedItem.getItemMeta().getLore().contains(durabilityString)) {
                e.setCancelled(true);
                ItemMeta clickedMeta = clickedItem.getItemMeta();
                List<String> lores = clickedMeta.getLore();
                lores.remove(durabilityString);
                clickedMeta.setLore(lores);
                clickedItem.setItemMeta(clickedMeta);
                clickedItem.setDurability((short) (clickedItem.getDurability() + 1));
                e.setCurrentItem(new ItemStack(Material.AIR));
                p.getInventory().addItem(clickedItem);
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

    private int getSwordAttackDmg(Material material) {
        switch (material) {
            case WOOD_SWORD:
            case GOLD_SWORD: return 5;
            case STONE_SWORD: return 6;
            case IRON_SWORD: return 7;
            case DIAMOND_SWORD: return 8;
            default: return 1;
        }
    }

    private boolean isSword(ItemStack item) {
        return item.getType() == Material.WOOD_SWORD
                || item.getType() == Material.STONE_SWORD
                || item.getType() == Material.IRON_SWORD
                || item.getType() == Material.GOLD_SWORD
                || item.getType() == Material.DIAMOND_SWORD;
    }
}
