package de.newdel.rpgcore;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;

public class WandRecipe {

    private static ShapedRecipe backpackRecipe;

    public static void reloadWandRecipe(Plugin mPlugin) {
        if (backpackRecipe != null) {
            Iterator<Recipe> it = mPlugin.getServer().recipeIterator();
            while (it.hasNext()) {
                if (backpackRecipe.equals(it.next())) {
                    it.remove();
                    break;
                }
            }
        }

        ShapedRecipe recipe = new ShapedRecipe(BasicEvents.getWand());
        recipe.shape("  g", " s ", "g  ");
        recipe.setIngredient('g', Material.GOLD_NUGGET);
        recipe.setIngredient('s', Material.STICK);
        mPlugin.getServer().addRecipe(recipe);
        backpackRecipe = recipe;
    }
}
