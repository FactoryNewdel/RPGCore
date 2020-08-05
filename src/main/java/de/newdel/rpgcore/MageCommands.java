package de.newdel.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MageCommands implements CommandExecutor {

    private Plugin plugin;
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

    public MageCommands(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player)sender;
        Spell spell = Spell.getByName(cmd.getName());
        if (MageEvents.hasCooldown(p)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You change your spell yet");
            return true;
        }
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
        return plugin.getConfig().getStringList("players." + p.getName() + ".Spells").contains(spell.name());
    }
}
