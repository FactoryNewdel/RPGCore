package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import de.newdel.rpgcore.MageCommands.Spell;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class MageEvents implements Listener {

    private Plugin plugin;
    private static HashMap<String, HashMap<Spell, Long>> cooldownMap = new HashMap<>();
    private final List<String> spells = Arrays.asList(Spell.PROJECTILE.name(), Spell.FIREBALL.name(), Spell.FREEZE.name(), Spell.LIGHTNING.name(), Spell.POISON.name(), Spell.RETREAT.name());
    private Player lastLightningShooter = null;
    private ArrayList<String> retreatList = new ArrayList<>();

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
        if (random <= getInvincibilityLevel(p)) e.setCancelled(true);
    }

    private int getInvincibilityLevel(Player p) {
        int level = plugin.getConfig().getInt("players." + p.getName() + ".Level");
        if (level > 50) return 15;
        else if (level > 10) return 7;
        else if (level > 5) return 5;
        else return 2;
    }

    //Use Spell Book

    @EventHandler
    public void onPlayerUseSpellBook(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack spellBook = e.getItem();
        if (!isSpellbook(spellBook)) return;
        if (!isMage(p)) {
            e.getPlayer().sendMessage(Main.prefix + ChatColor.RED + "Only mages can use spells from the cursed land");
            return;
        }
        Spell spell = null;
        for (String lore : spellBook.getItemMeta().getLore()) {
            if (lore.startsWith("_") && lore.endsWith("_")) {
                spell = Spell.getByName(lore.split("_")[1]);
                break;
            }
        }
        if (spell == null) return;
        int level = plugin.getConfig().getInt("players." + p.getName() + ".Spells." + spell.name());
        if (level > 10) {
            p.sendMessage(Main.prefix + ChatColor.RED + spell.name() + " is already on max level");
            return;
        }

        int cost = level + 1;
        int amount;

        try {
            amount = Integer.parseInt(spellBook.getItemMeta().getLore().get(1));
        } catch (NullPointerException exception) {
            amount = 1;
        }

        if (amount < cost) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You need " + (cost - spellBook.getAmount()) + " more books to level up this spell");
            return;
        } else if (amount > cost) {
            ItemMeta spellBookMeta = spellBook.getItemMeta();
            List<String> lores = spellBookMeta.getLore();
            lores.set(1, String.valueOf(Integer.parseInt(lores.get(1)) - cost));
            spellBookMeta.setLore(lores);
            spellBook.setItemMeta(spellBookMeta);
        } else e.getPlayer().getInventory().remove(spellBook);

        if (level == 0) {
            plugin.getConfig().set("players." + p.getName() + ".Spells." + spell.name(), 1);
            p.sendMessage(Main.prefix + ChatColor.GREEN + "Successfully learned " + spell.name());
        } else {
            plugin.getConfig().set("players." + p.getName() + ".Spells." + spell.name(), ++level);
            p.sendMessage(Main.prefix + ChatColor.GREEN + spell.name() + " upgraded to level " + level);
        }

        plugin.saveConfig();
    }


    //Shoot

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isMage(e.getPlayer())) return;
        Player p = e.getPlayer();
        if (retreatList.contains(p.getName())) return;
        ItemStack wand = e.getItem();
        if (wand == null || !wand.equals(BasicEvents.getWand())) return;
        Spell activeSpell = MageCommands.getActiveSpell(p);

        if (hasCooldown(p, activeSpell)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You have to wait before doing this again");
            return;
        }

        int level = plugin.getConfig().getInt("players." + p.getName() + ".Spells." + activeSpell.name());
        BasicEvents.addExp(plugin, p, level == 1 ? 1 : level / 2);

        Projectile projectile;
        switch (activeSpell) {
            case PROJECTILE: {
                for (int i = 0; i < plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.PROJECTILE.name()); i++) {
                    projectile = p.launchProjectile(Arrow.class);
                    projectile.setVelocity(projectile.getVelocity().multiply(3));
                }
                setCooldown(p, Spell.PROJECTILE, 1);
                return;
            }
            case FIREBALL: {
                Fireball fireball = p.launchProjectile(Fireball.class);
                fireball.setIsIncendiary(false);
                fireball.setCustomName("FireballSpell " + plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.FIREBALL.name()));
                fireball.setVelocity(fireball.getVelocity().multiply(5));
                setCooldown(p, Spell.FIREBALL, 5);
                return;
            }
            case FREEZE: {
                projectile = p.launchProjectile(Snowball.class);
                projectile.setCustomName("FreezeSpell " + plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.FREEZE.name()));
                setCooldown(p, Spell.FREEZE, 5);
                break;
            }
            case POISON:
                projectile = p.launchProjectile(WitherSkull.class);
                projectile.setCustomName("PoisonSpell " + plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.POISON.name()));
                setCooldown(p, Spell.POISON, 30);
                break;
            case LIGHTNING: {
                Set<Material> set = new HashSet<>();
                set.add(Material.AIR);
                lastLightningShooter = p;
                p.getWorld().strikeLightningEffect(p.getTargetBlock(set, 100).getLocation());
                setCooldown(p, Spell.LIGHTNING, 60);
                return;
            }
            case RETREAT:
                retreatList.add(p.getName());
                int playerLevel = plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.RETREAT.name());
                int spellLevel;
                if (playerLevel <= 3) {
                    spellLevel = 1;
                } else if (playerLevel <= 6) {
                    spellLevel = 2;
                } else if (playerLevel <= 9) {
                    spellLevel = 3;
                } else spellLevel = 4;

                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, spellLevel));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.hidePlayer(p);
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    retreatList.remove(p.getName());
                    p.removePotionEffect(PotionEffectType.SPEED);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.showPlayer(p);
                    }
                }, spellLevel * 5 * 20L);

                setCooldown(p, Spell.RETREAT, 10 * spellLevel);
                return;
            case INVSTEAL: {
                setCooldown(p, Spell.INVSTEAL, 15);
                return;
            }
            default:
                throw new RuntimeException("Invalid Projectile");
        }
        projectile.setVelocity(projectile.getVelocity().multiply(3));
    }

    //Fireball

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Fireball && e.getDamager().getCustomName() != null && e.getDamager().getCustomName().startsWith("FireballSpell"))
            e.setDamage(2);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof Fireball) || e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().startsWith("FireballSpell"))
            return;
        e.setCancelled(true);
        e.getLocation().getWorld().createExplosion(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ(), Float.parseFloat(e.getEntity().getCustomName().split("\\s+")[1]), false, true);
    }

    //Freeze

    @EventHandler
    public void onFreezeHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball) || e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().startsWith("FreezeSpell")) return;
        Location hit = e.getEntity().getLocation();
        double xb = hit.getBlockX();
        double zb = hit.getBlockZ();
        hit.setY(hit.getY() + 1);
        while (hit.getWorld().getBlockAt(hit).getType() == Material.WATER
                || hit.getWorld().getBlockAt(hit).getType() == Material.STATIONARY_WATER) {
            hit.setY(hit.getY() + 1);
        }
        double yb = hit.getBlockY() - 1;
        int level = Integer.parseInt(e.getEntity().getCustomName().split("\\s+")[1]);
        int radius = 1;
        int amount = (int) (Math.random() * (7 + level - 1) + (5 + level - 1));
        int icedOut = 0;
        root:
        while (radius <= 11) {
            for (double minX = xb - radius; minX <= xb + radius; minX++) {
                for (double minZ = zb - radius; minZ <= zb + radius; minZ++) {
                    if (Math.sqrt(Math.pow((minX - xb), 2) + Math.pow((minZ - zb), 2)) <= radius) {
                        Block block = hit.getWorld().getBlockAt((int) minX, (int) yb, (int) minZ);
                        if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                            block.setType(Material.ICE);
                            icedOut++;
                        }
                        if (icedOut >= amount) break root;
                    }
                }
            }
            radius += 2;
        }
    }

    // Poison

    @EventHandler
    public void onPoison(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof WitherSkull) || e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().startsWith("PoisonSpell")) return;
        if (!(e.getEntity().getNearbyEntities(1,1,1).get(0) instanceof Player)) return;
        Player p = (Player) e.getEntity().getNearbyEntities(1,1,1).get(0);
        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 5, plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.POISON.name()) / 2));
    }

    // Lightning

    @EventHandler
    public void onLightning(LightningStrikeEvent e) {
        if (!e.getLightning().isEffect()) return;
        for (Entity entity : e.getLightning().getNearbyEntities(3, 3, 3)) {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                if (p.equals(lastLightningShooter)) continue;
                p.setHealth(p.getHealth() - plugin.getConfig().getInt("players." + lastLightningShooter.getName() + ".Spells." + Spell.LIGHTNING.name()));
                p.damage(0);
            }
        }
    }

    // Retreat

    @EventHandler(ignoreCancelled = true)
    public void onRetreat(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (retreatList.contains(p.getName())) e.setCancelled(true);
    }

    // Invsteal

    @EventHandler
    public void onInvsteal(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (!isMage(p) || MageCommands.getActiveSpell(p) != Spell.INVSTEAL) return;
        if (retreatList.contains(p.getName())) return;
        ItemStack wand = p.getItemInHand();
        if (wand == null || !wand.equals(BasicEvents.getWand())) return;
        if (hasCooldown(p, Spell.INVSTEAL)) return;
        if (!(e.getRightClicked() instanceof Player)) return;
        Player target = (Player) e.getRightClicked();
        ItemStack[] items = target.getInventory().getContents();
        ArrayList<ItemStack> inventoryItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null) inventoryItems.add(item);
        }
        if (inventoryItems.size() == 0) return;
        int random = (int) (Math.random() * 100) + 1;
        int level = plugin.getConfig().getInt("players." + p.getName() + ".Spells." + Spell.INVSTEAL.name());
        if (random <= level * 10) {
            random = (int) (Math.random() * inventoryItems.size());
            ItemStack steal = inventoryItems.get(random);
            target.getInventory().remove(steal);
            p.getInventory().addItem(steal);
            p.sendMessage(Main.prefix + ChatColor.GREEN + "You stole an item from your enemy");
        }
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
                    case 1:
                        spellMeta.setLore(Arrays.asList("_Fireball_"));
                        break;
                    case 2:
                        spellMeta.setLore(Arrays.asList("_Freeze_"));
                        break;
                    case 3:
                        spellMeta.setLore(Arrays.asList("_Poison_"));
                        break;
                    case 4:
                        spellMeta.setLore(Arrays.asList("_Lightning_"));
                        break;
                    case 5:
                        spellMeta.setLore(Arrays.asList("_Retreat_"));
                        break;
                    default:
                        throw new RuntimeException("Invalid random");
                }
                spell.setItemMeta(spellMeta);
                chest.getBlockInventory().addItem(spell);
            }
        }
    }

    //Stack Spell Books

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        ItemStack drop = e.getItemDrop().getItemStack();
        ItemMeta dropMeta = drop.getItemMeta();
        if (!isSpellbook(drop)) return;
        for (Entity entity : e.getItemDrop().getNearbyEntities(3, 3, 3)) {
            if (!(entity instanceof Item)) continue;
            ItemStack item = ((Item) entity).getItemStack();
            if (!isSpellbook(item)) continue;
            int level = 1;
            if (item.getItemMeta().getLore().size() != 1) {
                level = Integer.parseInt(item.getItemMeta().getLore().get(1));
            }
            entity.remove();
            List<String> lores = dropMeta.getLore();
            if (lores.size() == 1) {
                lores.add(++level + "");
            } else {
                lores.set(1, String.valueOf(Integer.parseInt(lores.get(1)) + level));
            }
            dropMeta.setLore(lores);
            drop.setItemMeta(dropMeta);
        }
    }

    private boolean isSpellbook(ItemStack book) {
        return book != null && book.getType() == Material.ENCHANTED_BOOK && book.hasItemMeta() && book.getItemMeta().hasLore()
                && spells.contains(book.getItemMeta().getLore().get(0).split("_")[1].toUpperCase());
    }

    public static void setCooldown(Player p, Spell spell, int seconds) {
        if (!cooldownMap.containsKey(p.getName())) cooldownMap.put(p.getName(), new HashMap<>());
        cooldownMap.get(p.getName()).put(spell, System.currentTimeMillis() + 1000 * seconds);
    }

    public static boolean hasCooldown(Player p, Spell spell) {
        if (!cooldownMap.containsKey(p.getName())) return false;
        if (!cooldownMap.get(p.getName()).containsKey(spell)) return false;
        if (new Date(cooldownMap.get(p.getName()).get(spell)).before(new Date(System.currentTimeMillis()))) {
            cooldownMap.remove(p.getName());
            return false;
        } else return true;
    }
}
