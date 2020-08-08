package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ArrowrainRecipe implements Listener {

    private static ShapedRecipe backpackRecipe;
    private static String backpackString = "Arrowrain";
    private static Plugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!ArcherEvents.isArcher(e.getPlayer())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack hand = e.getItem();
        if (hand == null || hand.getType() != Material.ARROW) return;
        if (!hand.hasItemMeta() || !hand.getItemMeta().hasLore() || !hand.getItemMeta().getLore().contains(backpackString)) return;
        e.setCancelled(true);
        Bukkit.getServer().dispatchCommand(e.getPlayer(), "arrowrain");
    }

    public static void reloadArrowrainRecipe(Plugin mPlugin) {
        if (plugin == null) plugin = mPlugin;
        if (backpackRecipe != null) {
            Iterator<Recipe> it = mPlugin.getServer().recipeIterator();
            while (it.hasNext()) {
                if (backpackRecipe.equals(it.next())) {
                    it.remove();
                    break;
                }
            }
        }

        ItemStack result = new ItemStack(Material.ARROW);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setLore(Arrays.asList(backpackString));
        result.setItemMeta(resultMeta);

        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape("qwe","rtz","uio");
        recipe.setIngredient('q', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.0")));
        recipe.setIngredient('w', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.1")));
        recipe.setIngredient('e', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.2")));
        recipe.setIngredient('r', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.10")));
        recipe.setIngredient('t', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.11")));
        recipe.setIngredient('z', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.12")));
        recipe.setIngredient('u', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.20")));
        recipe.setIngredient('i', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.21")));
        recipe.setIngredient('o', Material.getMaterial(mPlugin.getConfig().getString("ArrowRainRecipe.22")));
        mPlugin.getServer().addRecipe(recipe);
        backpackRecipe = recipe;
    }
}
