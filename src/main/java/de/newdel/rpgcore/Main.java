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
    private static ShapedRecipe backpackRecipe;

    @Override
    public void onEnable() {
        // Plugin startup logic
        KnightCommands knightCommands = new KnightCommands(this);
        getCommand("sworddash").setExecutor(knightCommands);
        getCommand("easycrit").setExecutor(knightCommands);
        getCommand("stunblow").setExecutor(knightCommands);

        getCommand("reloadrpgcore").setExecutor(new ReloadConfig(this));

        getServer().getPluginManager().registerEvents(new BasicEvents(this), this);
        getServer().getPluginManager().registerEvents(new KnightEvents(this), this);

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        reloadClassMap(this);
        reloadBackpackRecipe(this);
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

    public static void reloadBackpackRecipe(Plugin plugin) {
        if (backpackRecipe != null) {
            Iterator<Recipe> it = plugin.getServer().recipeIterator();
            while (it.hasNext()) {
                if (backpackRecipe.equals(it.next())) {
                    it.remove();
                    break;
                }
            }
        }

        ItemStack result = new ItemStack(Material.CHEST);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setLore(Arrays.asList("Ability Backpack"));
        result.setItemMeta(resultMeta);

        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape("qwe","rtz","uio");

        recipe.setIngredient('q', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.0")));
        recipe.setIngredient('w', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.1")));
        recipe.setIngredient('e', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.2")));
        recipe.setIngredient('r', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.10")));
        recipe.setIngredient('t', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.11")));
        recipe.setIngredient('z', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.12")));
        recipe.setIngredient('u', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.20")));
        recipe.setIngredient('i', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.21")));
        recipe.setIngredient('o', Material.getMaterial(plugin.getConfig().getString("BackpackRecipe.22")));
        plugin.getServer().addRecipe(recipe);
        backpackRecipe = recipe;
    }
}
