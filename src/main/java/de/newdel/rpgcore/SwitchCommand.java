package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SwitchCommand implements CommandExecutor {

    private Plugin plugin;
    private static List<String> classList = Arrays.asList("Archer", "Knight", "Mage", "Citizen");
    private HashMap<String, Long> cooldownMap = new HashMap<>();

    public SwitchCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player)sender;
        if (!p.hasPermission(cmd.getPermission())) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You do not have permission " + cmd.getPermission());
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("switch")) {
            if (cooldownMap.containsKey(p.getName())) {
                if (new Date().before(new Date(cooldownMap.get(p.getName())))) {
                    p.sendMessage(Main.prefix + ChatColor.RED + "You have to wait before you can do this again");
                    return true;
                } else {
                    cooldownMap.remove(p.getName());
                }
            }
            InventoryLoader.saveInventory(p, InventoryLoader.Classes.getByName(plugin.getConfig().getString("players." + p.getName() + ".ActiveClass")), p.getInventory());
            p.getInventory().clear();
            p.getInventory().setArmorContents(new ItemStack[4]);
            if (ArcherEvents.isArcher(p)) p.removePotionEffect(PotionEffectType.SPEED);
            BasicEvents.cancelPlayer(p);
            p.openInventory(BasicEvents.getChooseClassInventory(plugin));
            cooldownMap.put(p.getName(), System.currentTimeMillis() + 1000 * 60 * 5);
        } else if (cmd.getName().equalsIgnoreCase("aswitch")) {
            if (args.length != 2) return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(Main.prefix + ChatColor.RED + "Player " + args[0] + " is currently not online");
                return true;
            }
            if (!classList.contains(args[1])) {
                p.sendMessage(Main.prefix + ChatColor.RED + "Wrong classname: " + args[1] + ". Valid classes:\n" + classList.toString());
                return true;
            }
            plugin.getConfig().set("players." + target.getName() + ".ActiveClass", args[1]);
            plugin.saveConfig();
            p.sendMessage(Main.prefix + ChatColor.GREEN + "Changed class of " + target.getName() + " to " + args[1]);
            target.kickPlayer("Your class has been changed by an admin. Please reconnect");
        } else throw new RuntimeException("Wrong command! " + cmd);
        return true;
    }

    public static List<String> getClassList() {
        return classList;
    }
}
