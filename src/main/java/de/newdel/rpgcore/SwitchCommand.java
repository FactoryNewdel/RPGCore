package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwitchCommand implements CommandExecutor {

    private Plugin plugin;
    private static List<String> classList = Arrays.asList("Archer", "Knight", "Mage", "Citizen");

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
            p.openInventory(BasicEvents.getChooseClassInventory(plugin));
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
