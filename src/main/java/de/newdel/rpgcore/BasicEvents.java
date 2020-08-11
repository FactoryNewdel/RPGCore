package de.newdel.rpgcore;

import de.newdel.rpgcore.MageCommands.Spell;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BasicEvents implements Listener {

    private Main plugin;
    private static ArrayList<String> chooseClassList = new ArrayList<>();
    private static HashMap<String, String> playerClassMap = new HashMap<>();
    private static String invChooseClass = ChatColor.GOLD + "Choose Class";

    public BasicEvents(Main plugin) {
        this.plugin = plugin;
    }

    public static boolean isCitizen(Player p) {
        return BasicEvents.getPlayerClassMap().get(p.getName()).equals("Citizen");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("RPGCore.use")) return;
        if (plugin.getConfig().contains("players." + p.getName())) {
            playerClassMap.put(p.getName(), plugin.getConfig().getString("players." + p.getName() + ".ActiveClass"));
            if (MageEvents.isMage(p)) {
                MageCommands.setActiveSpell(p, Spell.PROJECTILE);
            } else if (ArcherEvents.isArcher(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            } else if (isCitizen(p)) return;
            Bukkit.getScheduler().runTaskLater(plugin, () -> setScoreboard(p), 20 * 1L);
            return;
        }
        chooseClassList.add(p.getName());
        Bukkit.getScheduler().runTaskLater(plugin, () -> p.openInventory(getChooseClassInventory(plugin)), 20 * 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerClassMap.remove(e.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (chooseClassList.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (chooseClassList.contains(e.getPlayer().getName())) Bukkit.getScheduler().runTaskLater(plugin, () -> e.getPlayer().openInventory(e.getInventory()), 10L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(invChooseClass)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String className = item.getItemMeta().getDisplayName();
        Player p = (Player) e.getWhoClicked();
        plugin.getConfig().set("players." + p.getName() + ".ActiveClass", className);
        plugin.saveConfig();
        playerClassMap.put(p.getName(), plugin.getConfig().getString("players." + p.getName() + ".ActiveClass"));
        chooseClassList.remove(e.getWhoClicked().getName());
        p.closeInventory();
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (plugin.getConfig().contains("players." + p.getName() + "." + className)) {
            if (InventoryLoader.inventoryExists(p, InventoryLoader.Classes.getByName(className))) InventoryLoader.loadInventory(p, InventoryLoader.Classes.getByName(className));
            p.kickPlayer("Your class has been changed. Please reconnect");
            return;
        }
        if (!className.equals("Citizen")) setNewScoreboard(p, className);
        if (className.equals("Mage")) {
            setMageConfig(plugin, p);
        } else if (className.equals("Archer")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        }
    }

    public static Inventory getChooseClassInventory(Plugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 9, invChooseClass);

        int i = 1;
        for (String mClass : plugin.getConfig().getConfigurationSection("classes").getKeys(false)) {
            ItemStack item = new ItemStack(Main.getClassMap().get(mClass));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(mClass);
            item.setItemMeta(meta);
            inv.setItem(i, item);
            i += 2;
        }
        return inv;
    }

    private void setNewScoreboard(Player p, String className) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("class", "dummy");

        //Setting where to display the scoreboard/objective (either SIDEBAR, PLAYER_LIST or BELOW_NAME)
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        //Setting the display name of the scoreboard/objective
        objective.setDisplayName(ChatColor.GOLD + className);

        Score level = objective.getScore(ChatColor.GREEN + "Level:" + ChatColor.WHITE + " 1");
        level.setScore(2);
        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".Level", 1);

        Score curExp = objective.getScore(ChatColor.GREEN + "EXP:" + ChatColor.WHITE + " 0 exp");
        curExp.setScore(1);
        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".CurEXP", 0);

        Score nextExp = objective.getScore(ChatColor.GREEN + "EXP for lvl up:" + ChatColor.WHITE + " 5 exp");
        nextExp.setScore(0);
        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".NextEXP", 5);

        p.setScoreboard(board);
        plugin.saveConfig();
    }

    private void setScoreboard(Player p) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("class", "dummy");

        //Setting where to display the scoreboard/objective (either SIDEBAR, PLAYER_LIST or BELOW_NAME)
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        //Setting the display name of the scoreboard/objective
        objective.setDisplayName(ChatColor.GOLD + plugin.getConfig().getString("players." + p.getName() + ".ActiveClass"));

        Score level = objective.getScore(ChatColor.GREEN + "Level:" + ChatColor.WHITE + " " + plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".Level"));
        level.setScore(2);

        Score curExp = objective.getScore(ChatColor.GREEN + "EXP:" + ChatColor.WHITE + " " + plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".CurEXP") + " exp");
        curExp.setScore(1);

        Score nextExp = objective.getScore(ChatColor.GREEN + "EXP for lvl up:" + ChatColor.WHITE + " " + plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".NextEXP") + " exp");
        nextExp.setScore(0);

        p.setScoreboard(board);
    }

    public static void addExp(Plugin plugin, Player p, int exp) {
        Scoreboard board = p.getScoreboard();
        Objective objective = board.getObjective("class");
        int levelInt = plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".Level");
        int curExpInt = plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".CurEXP");
        int nextExpInt = plugin.getConfig().getInt("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".NextEXP");
        board.resetScores(ChatColor.GREEN + "Level:" + ChatColor.WHITE + " " + levelInt);
        board.resetScores(ChatColor.GREEN + "EXP:" + ChatColor.WHITE + " " + curExpInt + " exp");
        board.resetScores(ChatColor.GREEN + "EXP for lvl up:" + ChatColor.WHITE + " " + nextExpInt + " exp");

        curExpInt += exp;

        while (curExpInt >= nextExpInt) {
            curExpInt = curExpInt - nextExpInt;
            levelInt++;
            nextExpInt = levelInt * 5;
        }

        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".Level", levelInt);
        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".CurEXP", curExpInt);
        plugin.getConfig().set("players." + p.getName() + "." + playerClassMap.get(p.getName()) + ".NextEXP", nextExpInt);
        plugin.saveConfig();

        Score levelScore = objective.getScore(ChatColor.GREEN + "Level:" + ChatColor.WHITE + " " + levelInt);
        Score curExpScore = objective.getScore(ChatColor.GREEN + "EXP:" + ChatColor.WHITE + " " + curExpInt + " exp");
        Score nextExpScore = objective.getScore(ChatColor.GREEN + "EXP for lvl up:" + ChatColor.WHITE + " " + nextExpInt + " exp");
        levelScore.setScore(2);
        curExpScore.setScore(1);
        nextExpScore.setScore(0);
    }

    public static HashMap<String, String> getPlayerClassMap() {
        return playerClassMap;
    }

    public static ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta wandMeta = wand.getItemMeta();
        wandMeta.setDisplayName("Wand");
        wandMeta.setLore(Arrays.asList("Mage's Wand"));
        wand.setItemMeta(wandMeta);
        return wand;
    }

    public static void setMageConfig(Plugin plugin, Player p) {
        p.getInventory().addItem(getWand());
        MageCommands.setActiveSpell(p, MageCommands.Spell.PROJECTILE);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.PROJECTILE.name(), 1);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.FIREBALL.name(), 0);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.FREEZE.name(), 0);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.POISON.name(), 0);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.LIGHTNING.name(), 0);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.RETREAT.name(), 0);
        plugin.getConfig().set("players." + p.getName() + ".Mage.Spells." + Spell.INVSTEAL.name(), 0);
        plugin.saveConfig();
    }

    public static void cancelPlayer(Player p) {
        chooseClassList.add(p.getName());
    }
}
