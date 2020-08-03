package de.tim.rpgcore;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KnightEvents implements Listener {

    private Main plugin;


    public KnightEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        System.out.println(e.getDamage());
        System.out.println(e.getDamage() * 2 / ((10 / 8.0) + 1));
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (BasicEvents.getPlayerClassMap().get(p.getName()).equals("Knight")) {
                e.setDamage(e.getDamage() * 1.02);
            }
        }
    }


}
