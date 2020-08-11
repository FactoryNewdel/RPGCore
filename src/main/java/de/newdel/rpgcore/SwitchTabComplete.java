package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SwitchTabComplete implements TabCompleter {



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        } else if (args.length == 2) {
            list.addAll(SwitchCommand.getClassList());
        }
        return null;
    }
}
