package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import de.newdel.rpgcore.MageCommands.Spell;

public class ClassCommand implements CommandExecutor {

    private Plugin plugin;

    public ClassCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission(cmd.getPermission())) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You do not have permission " + cmd.getPermission());
            return true;
        }
        Location loc = p.getLocation();
        List<String> lines = new ArrayList<>();

        String className = plugin.getConfig().getString("players." + p.getName() + ".ActiveClass");
        int level = plugin.getConfig().getInt("players." + p.getName() + "." + className + ".Level");
        lines.add(ChatColor.GOLD + className);
        lines.add(ChatColor.GREEN + "Level: " + ChatColor.RED + level);
        if (className.equals("Knight")) {
            lines.add(ChatColor.YELLOW + "Basics");
            lines.add("+2% dmg with sword");
            lines.add("+1 durability on armor");
            if (level > 5) {
                lines.add(ChatColor.YELLOW + "Abilities (2m cooldown)");
                lines.add("Sword Dash -> Right click player to deal dmg with strength 10");
            }
            if (level > 10) lines.add("Easy Crit -> Crit (+30% dmg) with every hit for 5 sec");
            if (level > 20) lines.add("Stun Blow -> On next crit on another player, target gets 10 sec Blindness");
            if (level > 50)
                lines.add("Bleed -> Chance of 5% to activate bleed effect on target (-0.5 hearts / sec for 10 sec");
            lines.add(ChatColor.YELLOW + "Leveling by Killing");
            lines.add("Ender Dragon/Wither: 100 xp");
            lines.add("Monster: 10 xp");
            lines.add("Animal: 5 xp");
            lines.add("Player: 20 xp");
        } else if (className.equals("Mage")) {
            lines.add(ChatColor.YELLOW + "Basics");
            lines.add("Chance of 2/5/7/15% on level 1/5/10/50 to prevent damage on hit");
            lines.add(ChatColor.YELLOW + "Spells");
            lines.add("All spells can be leveled up by using Spell Books");
            lines.add("Spell Books can be found in Dungeons and");
            lines.add("leveled up by dropping them together");
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.PROJECTILE.name()) > 0) {
                lines.add(ChatColor.GREEN + "Projectile (1 sec): " + ChatColor.RED + "Just a normal arrow");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "More arrows");
            }
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.FIREBALL.name()) > 0) {
                lines.add(ChatColor.GREEN + "Fireball (5 sec): " + ChatColor.RED + "Shoots a fireball that causes a big explosion but not much damage");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "Bigger explosion");
            }
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.FREEZE.name()) > 0) {
                lines.add(ChatColor.GREEN + "Freeze (5 sec): " + ChatColor.RED + "Freezes a few blocks of water in a small radius");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "Freezes more blocks");
            }
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.POISON.name()) > 0) {
                lines.add(ChatColor.GREEN + "Poison (30 sec): " + ChatColor.RED + "Right click a player to give him poison for a few seconds");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "Stronger poison");
            }
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.LIGHTNING.name()) > 0) {
                lines.add(ChatColor.GREEN + "Lightning (60 sec): " + ChatColor.RED + "Summons a lightning where the player is looking at");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "More damage");
            }
            if (plugin.getConfig().getInt("players." + p.getName() + ".Mage.Spells." + Spell.RETREAT.name()) > 0) {
                lines.add(ChatColor.GREEN + "Retreat (10-40 sec): " + ChatColor.RED + "Player gets invisible and speed 3 effect for a few seconds but can't hit a player");
                lines.add(ChatColor.GREEN + "Level up: " + ChatColor.RED + "Longer effect but longer cooldown");
            }
            if (level > 20)
                lines.add(ChatColor.RED + "Invsteal (15 sec, 1 lvl): " + ChatColor.GREEN + "Steal an item from the target's inventory (10% chance)");
            lines.add(ChatColor.YELLOW + "Leveling");
            lines.add("Get spellLevel / 2 ep for every spell you cast (min 1)");
        } else if (className.equals("Archer")) {
            lines.add(ChatColor.YELLOW + "Basics");
            lines.add("+20/25/30% damage with bow on level 1/10/50");
            lines.add("3/7% chance of automatically hitting the nearest entity when shooting");
            lines.add("Speed 1");
            if (level < 20) lines.add("Armor has -2 durability on level < 20");
            lines.add(ChatColor.YELLOW + "Spell");
            lines.add("Arrowrain (10 sec):");
            if (level < 5) lines.add("Unlock at level 5");
            if (level > 10) lines.add("Fire arrows");
            else lines.add("Arrows are on fire on lvl 10");
            lines.add("Shoots 10 arrows at once when they are in your inv");
            lines.add(ChatColor.YELLOW + "Leveling");
            lines.add("+5 ep for every hit on an entity");
        } else if (className.equals("Citizen")) {
            lines.add(ChatColor.YELLOW + "Abilities");
            lines.add("Yes");
        } else throw new RuntimeException("Invalid className: " + className);

        HoloAPI holo = new HoloAPI(loc, lines);
        if (!holo.display(p)) {
            p.sendMessage("An error occured while sending you the hologram. Pls try again");
        } else {
            p.sendMessage(Main.prefix + ChatColor.GREEN + "Info Hologram enabled. It will get disabled after 30 sec");
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> holo.destroy(p), 30 * 20L);
        return true;
    }
}
