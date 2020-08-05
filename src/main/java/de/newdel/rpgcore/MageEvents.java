package de.newdel.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import de.newdel.rpgcore.MageCommands.Spell;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MageEvents implements Listener {

    private Plugin plugin;
    private static HashMap<String, Long> cooldownMap = new HashMap<>();


    public MageEvents(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isMage(Player p) {
        return BasicEvents.getPlayerClassMap().get(p.getName()).equals("Mage");
    }

    //Basic Event

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            e.setCancelled(true);
            return;
        }
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!isMage(p)) return;
        int random = (int) (Math.random() * 100 + 1);
        if (random <= 2) e.setCancelled(true);
    }

    //Use Spell Book

    @EventHandler
    public void onPlayerUseSpellBook(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        if (!isMage(p)) {
            e.getPlayer().sendMessage(Main.prefix + ChatColor.RED + "Only mages can use spells from the cursed land");
            return;
        }
        ItemStack spellBook = e.getItem();
        if (spellBook == null || spellBook.getType() != Material.ENCHANTED_BOOK
            || !spellBook.hasItemMeta() || !spellBook.getItemMeta().hasLore()) return;
        Spell spell = null;
        for (String lore : spellBook.getItemMeta().getLore()) {
            if (lore.startsWith("_") && lore.endsWith("_")) {
                spell = Spell.getByName(lore.split("_")[1]);
                break;
            }
        }
        List<String> list = plugin.getConfig().getStringList("players." + p.getName() + ".Spells");
        if (list.contains(spell.name())) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You already know this spell");
            return;
        }
        list.add(spell.name());
        plugin.getConfig().set("players." + p.getName() + ".Spells", list);
        plugin.saveConfig();
        p.sendMessage(Main.prefix + ChatColor.GREEN + "Successfully learned " + spell.name());
    }


    //Shoot

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isMage(e.getPlayer())) return;
        Player p = e.getPlayer();
        ItemStack wand = e.getItem();
        if (wand == null || !wand.equals(BasicEvents.getWand())) return;
        if (hasCooldown(p)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You have to wait before doing this again");
            return;
        }
        Spell activeSpell = MageCommands.getActiveSpell(p);
        Projectile projectile;
        switch (activeSpell) {
            case PROJECTILE: {
                projectile = p.launchProjectile(Arrow.class);
                setCooldown(p, 1);
                break;
            }
            case FIREBALL: {
                Fireball fireball = p.launchProjectile(Fireball.class);
                fireball.setIsIncendiary(false);
                fireball.setCustomName("FireballSpell");
                fireball.setVelocity(fireball.getVelocity().multiply(5));
                setCooldown(p, 5);
                return;
            }
            case FREEZE:     projectile = p.launchProjectile(Snowball.class);    break;
            case POISON:     projectile = p.launchProjectile(WitherSkull.class);    break;
            case LIGHTNING:  projectile = p.launchProjectile(FishHook.class);    break;
            case RETREAT:    projectile = p.launchProjectile(ThrownPotion.class);    break;
            default: throw new RuntimeException("Invalid Projectile");
        }
        projectile.setVelocity(projectile.getVelocity().multiply(3));
    }

    //Fireball

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Fireball && e.getDamager().getCustomName() != null && e.getDamager().getCustomName().equals("FireballSpell")) e.setDamage(2);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntity() == null || e.getEntityType() != EntityType.FIREBALL || e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().equals("FireballSpell")) return;
        e.setCancelled(true);
        e.getLocation().getWorld().createExplosion(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ(), 5, false, true);
    }

    //Spells in Dungeons

    @EventHandler(ignoreCancelled = true)
    public void onChunkPopulate(ChunkPopulateEvent e) {
        BlockState[] tileEntities = e.getChunk().getTileEntities();
        for (BlockState state : tileEntities) {
            if (!(state instanceof Chest)) continue;
            Chest chest = (Chest) state;
            int random = (int) (Math.random() * 100 + 1);
            if (random <= plugin.getConfig().getInt("SpellLootChance")) {
                random = (int) (Math.random() * 5 + 1);
                ItemStack spell = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta spellMeta = spell.getItemMeta();
                spellMeta.setDisplayName(ChatColor.GOLD + "Spell Book");
                switch (random) {
                    case 1: spellMeta.setLore(Arrays.asList("_Fireball_")); break;
                    case 2: spellMeta.setLore(Arrays.asList("_Freeze_")); break;
                    case 3: spellMeta.setLore(Arrays.asList("_Poison_")); break;
                    case 4: spellMeta.setLore(Arrays.asList("_Lightning_")); break;
                    case 5: spellMeta.setLore(Arrays.asList("_Retreat_")); break;
                    default: throw new RuntimeException("Invalid random");
                }
                spell.setItemMeta(spellMeta);
                chest.getBlockInventory().addItem(spell);
                System.out.println(chest.getLocation());
            }
        }
    }

    public static void setCooldown(Player p, int seconds) {
        cooldownMap.put(p.getName(), System.currentTimeMillis() + 1000 * seconds);
    }

    public static boolean hasCooldown(Player p) {
        if (!cooldownMap.containsKey(p.getName())) return false;
        if (new Date(cooldownMap.get(p.getName())).before(new Date(System.currentTimeMillis()))) {
            cooldownMap.remove(p.getName());
            return false;
        } else return true;
    }
}
