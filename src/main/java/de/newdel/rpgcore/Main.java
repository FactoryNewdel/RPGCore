package de.newdel.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public final class Main extends JavaPlugin {

    public static final String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "RPGCore" + ChatColor.GRAY + "]" + ChatColor.RESET;
    private static HashMap<String, Material> classMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        KnightCommands knightCommands = new KnightCommands(this);
        getCommand("sworddash").setExecutor(knightCommands);
        getCommand("easycrit").setExecutor(knightCommands);
        getCommand("stunblow").setExecutor(knightCommands);

        MageCommands mageCommands = new MageCommands(this);
        getCommand("projectile").setExecutor(mageCommands);
        getCommand("fireball").setExecutor(mageCommands);
        getCommand("freeze").setExecutor(mageCommands);
        getCommand("poison").setExecutor(mageCommands);
        getCommand("lightning").setExecutor(mageCommands);
        getCommand("retreat").setExecutor(mageCommands);

        getCommand("reloadrpgcore").setExecutor(new ReloadConfig(this));

        getServer().getPluginManager().registerEvents(new BasicEvents(this), this);
        getServer().getPluginManager().registerEvents(new KnightEvents(this), this);
        getServer().getPluginManager().registerEvents(new MageEvents(this), this);
        getServer().getPluginManager().registerEvents(new Backpack(), this);

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        reloadClassMap(this);
        Backpack.reloadBackpackRecipe(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static HashMap<String, Material> getClassMap() {
        return classMap;
    }

    public static void reloadClassMap(Plugin plugin) {
        classMap.clear();
        for (String mClass : plugin.getConfig().getConfigurationSection("classes").getKeys(false)) {
            classMap.put(mClass, Material.getMaterial(plugin.getConfig().getString("classes." + mClass + ".icon")));
        }
    }
}
