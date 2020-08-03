package de.tim.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class ReloadConfig implements CommandExecutor {

    private Main plugin;

    public ReloadConfig(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(cmd.getPermission())) {
            sender.sendMessage(Main.prefix + ChatColor.RED + " You do not have permission " + cmd.getPermission());
            return true;
        }
        ConfigurationSection config = plugin.getConfig();
        plugin.reloadConfig();
        Main.reloadClassMap(plugin);
        sender.sendMessage(Main.prefix + ChatColor.GREEN + "Plugin config has been reloaded!");
        return true;
    }
}
