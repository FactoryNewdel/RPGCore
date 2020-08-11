package de.newdel.rpgcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Arrays;

public class InventoryLoader {

    private static String root;

    enum Classes {
        Knight,
        Mage,
        Archer,
        Citizen;

        public static Classes getByName(String s) {
            if (s.equalsIgnoreCase("knight")) return Knight;
            else if (s.equalsIgnoreCase("mage")) return Mage;
            else if (s.equalsIgnoreCase("archer")) return Archer;
            else if (s.equalsIgnoreCase("citizen")) return Citizen;
            else throw new RuntimeException("Wrong className: " + s);
        }
    }

    static {
        root = Bukkit.getWorldContainer().getAbsolutePath();
        root = root.substring(0, root.length() - 2);
    }

    public static void saveInventory(Player p, Classes playerClass, PlayerInventory inv) {
        File directory = new File(root + "\\plugins\\RPGCore\\Inventories");
        if (!directory.exists()) directory.mkdir();
        File file = new File(root + "\\plugins\\RPGCore\\Inventories\\" + p.getUniqueId().toString() + "_" + playerClass.name() + ".yml");
        try {
            if (file.exists()) file.delete();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(fos);
            for (ItemStack item : inv.getArmorContents()) {
                boos.writeObject(item);
            }
            for (ItemStack item : inv.getContents()) {
                boos.writeObject(item);
            }
            boos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadInventory(Player p, Classes playerClass) {
        File file = new File(root + "\\plugins\\RPGCore\\Inventories\\" + p.getUniqueId().toString() + "_" + playerClass.name() + ".yml");
        if (!file.exists()) throw new RuntimeException("Inventory file does not exist: " + file.getAbsolutePath());
        try {
            FileInputStream fis = new FileInputStream(file);
            BukkitObjectInputStream ois = new BukkitObjectInputStream(fis);
            PlayerInventory inv = p.getInventory();
            int i = 0;
            ItemStack[] armorContents = new ItemStack[4];
            ItemStack[] invContents = new ItemStack[36];
            try {
                while (true) {
                    ItemStack item = (ItemStack) ois.readObject();
                    if (i >= 4) invContents[i - 4] = item;
                    else armorContents[i] = item;
                    i++;
                }
            } catch (EOFException e) {
                ois.close();
                fis.close();
            }
            inv.setArmorContents(armorContents);
            inv.setContents(invContents);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean inventoryExists(Player p, Classes playerClass) {
        File file = new File(root + "\\plugins\\RPGCore\\Inventories\\" + p.getUniqueId().toString() + "_" + playerClass.name() + ".yml");
        return file.exists();
    }
}
