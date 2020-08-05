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

public class MageBackpack implements Listener {

    private static ShapedRecipe backpackRecipe;
    private static String backpackString = "Spell Backpack";
    private static String backpackInventoryString = ChatColor.GOLD + "Spell Backpack";
    private static Plugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!MageEvents.isMage(e.getPlayer())) return;
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

        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("classes.Mage.spells");
        ArrayList<String> list = new ArrayList<>();
        for (String key : cs.getKeys(false)) {
            if (plugin.getConfig().getInt("players." + p.getName() + ".Spells." + key.toUpperCase()) > 0) {
                list.add(key);
            }
        }
        int size = cs.getKeys(false).size();
        int[] slots;
        switch (size) {
            case 1: slots = new int[]{4};             break;
            case 2: slots = new int[]{2,6};           break;
            case 3: slots = new int[]{0,4,8};         break;
            case 4: slots = new int[]{1,3,5,7};       break;
            case 5: slots = new int[]{0,2,4,6,8};     break;
            case 6: slots = new int[]{0,1,3,5,7,8};   break;
            case 7: slots = new int[]{1,2,3,4,5,6,7}; break;
            default: throw new RuntimeException();
        }
        int i = 0;

        for (String key : list) {
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
        recipe.setIngredient('q', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.0")));
        recipe.setIngredient('w', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.1")));
        recipe.setIngredient('e', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.2")));
        recipe.setIngredient('r', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.10")));
        recipe.setIngredient('t', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.11")));
        recipe.setIngredient('z', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.12")));
        recipe.setIngredient('u', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.20")));
        recipe.setIngredient('i', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.21")));
        recipe.setIngredient('o', Material.getMaterial(mPlugin.getConfig().getString("SpellBackpackRecipe.22")));
        mPlugin.getServer().addRecipe(recipe);
        backpackRecipe = recipe;
    }
}
