package de.newdel.rpgcore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;

public class ArcherCommands implements CommandExecutor {

    private Plugin plugin;
    private HashMap<String, Long> rainList = new HashMap<>();

    public ArcherCommands(Plugin mPlugin) {
        plugin = mPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players!");
            return true;
        }
        Player p = (Player)sender;
        if (!ArcherEvents.isArcher(p)) {
            p.sendMessage(Main.prefix + ChatColor.RED + "Only for Archers");
            return true;
        }
        if (plugin.getConfig().getInt("players." + p.getName() + Main.getClassMap().get(p.getName()) + ".Level") < 5) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You do not know this skill");
            return true;
        }
        if (rainList.containsKey(p.getName()) && new Date(System.currentTimeMillis()).before(new Date(rainList.get(p.getName())))) {
            p.sendMessage(Main.prefix + ChatColor.RED + "You can't use this skill again yet");
            return true;
        }
        int toRemove = 10;
        if (p.getInventory().contains(Material.ARROW, 10)) {
            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && item.getType() == Material.ARROW && (!item.hasItemMeta() || !item.getItemMeta().hasLore())) {
                    if (item.getAmount() > toRemove) {
                        item.setAmount(item.getAmount() - toRemove);
                        break;
                    } else if (item.getAmount() == toRemove) {
                        p.getInventory().remove(item);
                    } else {
                        toRemove -= item.getAmount();
                        p.getInventory().remove(item);
                        item.setType(Material.AIR);
                    }
                }
            }
        } else {
            p.sendMessage(Main.prefix + ChatColor.RED + "You do not have enough arrows (10) for this spell");
            return true;
        }

        boolean onFire = plugin.getConfig().getInt("players." + p.getName() + Main.getClassMap().get(p.getName()) + ".Level") >= 10;
        for (int i = 0; i < 10; i++) {
            Arrow arrow = p.launchProjectile(Arrow.class);
            if (onFire) arrow.setFireTicks(Integer.MAX_VALUE);
        }

        rainList.put(p.getName(), System.currentTimeMillis() + (1000 * 10));
        sender.sendMessage(Main.prefix + ChatColor.GREEN + "Arrowrain activated");
        return true;
    }
}
