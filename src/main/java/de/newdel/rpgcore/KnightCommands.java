package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class KnightCommands implements CommandExecutor {

    private Plugin plugin;
    private static HashMap<String, ArrayList<KnightAbility>> abilityMap;
    private static HashMap<String, HashMap<KnightAbility, Long>> abilityCooldownMap;
    public enum KnightAbility {
        SWORDDASH,
        EASYCRIT,
        STUNBLOW,
        BLEED;
        public static KnightAbility getByName(String name) {
            if (name.equalsIgnoreCase("sworddash")) return SWORDDASH;
            else if (name.equalsIgnoreCase("easycrit")) return EASYCRIT;
            else if (name.equalsIgnoreCase("stunblow")) return STUNBLOW;
            else if (name.equalsIgnoreCase("bleed")) return BLEED;
            else throw new IllegalArgumentException("Invalid Ability: " + name);
        }
    }

    public KnightCommands(Plugin plugin) {
        this.plugin = plugin;
        abilityMap = new HashMap<>();
        abilityCooldownMap = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player)sender;
        KnightAbility ability = KnightAbility.getByName(cmd.getName());
        if (!canUse(plugin, p, ability)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You didn't unlock this ability yet");
            return true;
        } else if (!cooldownExpired(p, ability)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You can't use this ability right now");
            return true;
        }
        addPlayerAbility(p, ability);
        if (ability == KnightAbility.EASYCRIT) Bukkit.getScheduler().runTaskLater(plugin, () -> endPlayerAbility(p, ability), 20 * 5L);
        sender.sendMessage(Main.prefix + ChatColor.GREEN + ability.name() + " activated");
        return true;
    }

    public static boolean canUse(Plugin plugin, Player p, KnightAbility ability) {
        switch (ability) {
            case SWORDDASH: return plugin.getConfig().getInt("players." + p.getName() + ".Level") >= 5;
            case EASYCRIT: return plugin.getConfig().getInt("players." + p.getName() + ".Level") >= 10;
            case STUNBLOW: return plugin.getConfig().getInt("players." + p.getName() + ".Level") >= 20;
            case BLEED: return plugin.getConfig().getInt("players." + p.getName() + ".Level") >= 50;
            default: return false;
        }
    }

    public static boolean hasAbilityActivated(Player p, KnightAbility ability) {
        return KnightCommands.getAbilityMap().containsKey(p.getName())
                && KnightCommands.getAbilityMap().get(p.getName()).contains(ability);
    }

    public static void addPlayerAbility(Player p, KnightAbility ability) {
        if (!abilityMap.containsKey(p.getName())) {
            abilityMap.put(p.getName(), new ArrayList<>());
        }
        abilityMap.get(p.getName()).add(ability);
    }

    public static void removePlayerAbility(Player p, KnightAbility ability) {
        abilityMap.get(p.getName()).remove(ability);
    }

    public static void endPlayerAbility(Player p, KnightAbility ability) {
        if (!abilityMap.get(p.getName()).contains(ability)) return;
        removePlayerAbility(p, ability);
        if (!abilityCooldownMap.containsKey(p.getName())) {
            abilityCooldownMap.put(p.getName(), new HashMap<>());
        }
        abilityCooldownMap.get(p.getName()).put(ability, (System.currentTimeMillis() + 2 * 1000 * 60));
    }

    public static boolean cooldownExpired(Player p, KnightAbility ability) {
        if (!abilityCooldownMap.containsKey(p.getName())) return true;
        if (!abilityCooldownMap.get(p.getName()).containsKey(ability)) return true;
        if (new Date(abilityCooldownMap.get(p.getName()).get(ability)).before(new Date(System.currentTimeMillis()))) {
            abilityCooldownMap.get(p.getName()).remove(ability);
            return true;
        } else return false;
    }

    public static HashMap<String, ArrayList<KnightAbility>> getAbilityMap() {
        return abilityMap;
    }
}
