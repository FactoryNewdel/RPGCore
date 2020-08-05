package de.newdel.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MageCommands implements CommandExecutor {

    private static Plugin plugin;
    private static HashMap<String, Spell> activeSpellMap = new HashMap<>();

    public enum Spell {
        PROJECTILE,
        FIREBALL,
        FREEZE,
        POISON,
        LIGHTNING,
        RETREAT;
        public static Spell getByName(String name) {
            if (name.equalsIgnoreCase("projectile")) return PROJECTILE;
            else if (name.equalsIgnoreCase("fireball")) return FIREBALL;
            else if (name.equalsIgnoreCase("freeze")) return FREEZE;
            else if (name.equalsIgnoreCase("poison")) return POISON;
            else if (name.equalsIgnoreCase("lightning")) return LIGHTNING;
            else if (name.equalsIgnoreCase("retreat")) return RETREAT;
            else throw new IllegalArgumentException("Invalid Spell: " + name);
        }
    }

    public MageCommands(Plugin mPlugin) {
        plugin = mPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player)sender;
        Spell spell = Spell.getByName(cmd.getName());
        if (!ownsSpell(p, spell)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You do not own this spell");
            return true;
        }
        setActiveSpell(p, spell);
        sender.sendMessage(Main.prefix + ChatColor.GREEN + spell.name() + " equipped");
        return true;
    }

    public static void setActiveSpell(Player p, Spell spell) {
        activeSpellMap.put(p.getName(), spell);
    }

    public static Spell getActiveSpell(Player p) {
        return activeSpellMap.get(p.getName());
    }

    private boolean ownsSpell(Player p, Spell spell) {
        return plugin.getConfig().getInt("players." + p.getName() + ".Spells." + spell) > 0;
    }

    public static void activateWandRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(BasicEvents.getWand());
        recipe.shape("aaa","aba","aaa");

        recipe.setIngredient('a', Material.GOLD_INGOT);
        recipe.setIngredient('b', Material.STICK);

        plugin.getServer().addRecipe(recipe);
    }
}
