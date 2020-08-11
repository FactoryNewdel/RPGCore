package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class ResetCommand implements CommandExecutor {

    private Plugin plugin;

    public ResetCommand(Plugin plugin) {
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
        Player target;
        if (cmd.getName().equalsIgnoreCase("reset")) {
            target = p;
        } else if (cmd.getName().equalsIgnoreCase("areset")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(Main.prefix + ChatColor.RED + "Player " + args[0] + " is not online");
                return true;
            }
        } else throw new RuntimeException("Wrong command! " + cmd);
        String className = BasicEvents.getPlayerClassMap().get(p.getName());
        plugin.getConfig().set("players." + target.getName() + "." + className + ".Level", 1);
        plugin.getConfig().set("players." + target.getName() + "." + className + ".CurEXP", 0);
        plugin.getConfig().set("players." + target.getName() + "." + className + ".NextEXP", 5);
        if (className.equalsIgnoreCase("mage")) BasicEvents.setMageConfig(plugin, p);
        plugin.saveConfig();
        if (p != target) p.sendMessage(Main.prefix + ChatColor.GREEN + "Active Class of player " + target.getName() + " has been reset");
        target.kickPlayer("Your class has been reset. Please reconnect");
        return true;
    }
}
