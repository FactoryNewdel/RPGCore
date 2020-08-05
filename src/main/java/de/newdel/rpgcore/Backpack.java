package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Iterator;

public class Backpack implements Listener {

    private static ShapedRecipe backpackRecipe;
    private static String backpackString = "Ability Backpack";
    private static String backpackInventoryString = ChatColor.GOLD + "Ability Backpack";
    private static Plugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!KnightEvents.isKnight(e.getPlayer())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack hand = e.getItem();
        if (hand == null || hand.getType() != Material.CHEST) return;
        if (!hand.hasItemMeta() || !hand.getItemMeta().hasLore() || !hand.getItemMeta().getLore().contains(backpackString)) return;
        e.setCancelled(true);
        e.getPlayer().openInventory(getAbilityInventory(e.getPlayer()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(backpackInventoryString)) return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        Bukkit.getServer().dispatchCommand(e.getWhoClicked(), item.getItemMeta().getDisplayName());
        e.getWhoClicked().closeInventory();
    }

    private Inventory getAbilityInventory(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, backpackInventoryString);

        String playerClass = plugin.getConfig().getString("players." + p.getName() + ".Class");

        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("classes." + playerClass + ".abilities");
        int size = cs.getKeys(false).size();
        int[] slots;
        switch (size) {
            case 1: slots = new int[]{4};       break;
            case 2: slots = new int[]{2,6};     break;
            case 3: slots = new int[]{0,4,8};   break;
            case 4: slots = new int[]{1,3,5,7}; break;
            default: throw new RuntimeException();
        }
        int i = 0;

        for (String key : cs.getKeys(false)) {
            ItemStack item = new ItemStack(Material.getMaterial(cs.getString(key)));
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(key.toUpperCase());
            item.setItemMeta(itemMeta);
            inv.setItem(slots[i++], item);
        }

        return inv;
    }

    public static void reloadBackpackRecipe(Plugin mPlugin) {
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

        ItemStack result = new ItemStack(Material.CHEST);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setLore(Arrays.asList(backpackString));
        result.setItemMeta(resultMeta);

        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape("qwe","rtz","uio");

        recipe.setIngredient('q', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.0")));
        recipe.setIngredient('w', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.1")));
        recipe.setIngredient('e', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.2")));
        recipe.setIngredient('r', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.10")));
        recipe.setIngredient('t', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.11")));
        recipe.setIngredient('z', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.12")));
        recipe.setIngredient('u', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.20")));
        recipe.setIngredient('i', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.21")));
        recipe.setIngredient('o', Material.getMaterial(mPlugin.getConfig().getString("BackpackRecipe.22")));
        mPlugin.getServer().addRecipe(recipe);
        backpackRecipe = recipe;
    }
}
