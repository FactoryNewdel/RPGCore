package de.newdel.rpgcore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;


public class HoloAPI {

    private List<String> lines;
    private Location loc;
    private static final double ABS = 0.23D;
    private static String path;
    private static String version;
    Class<?> armorStand;
    Class<?> worldClass;
    Class<?> nmsEntity;
    Class<?> craftWorld;
    Class<?> packetClass;
    Class<?> entityLivingClass;


    static {
        path = Bukkit.getServer().getClass().getPackage().getName();
        version = path.substring(path.lastIndexOf(".")+1, path.length());
    }

    public HoloAPI(Location loc, List<String> lines) {
        this.lines = lines;
        this.loc = loc;
        try {
            armorStand = Class.forName("net.minecraft.server." + version + ".EntityArmorStand");
        worldClass = Class.forName("net.minecraft.server." + version + ".World");
            nmsEntity = Class.forName("net.minecraft.server." + version + ".Entity");
            craftWorld = Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
            packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutSpawnEntityLiving");
            entityLivingClass = Class.forName("net.minecraft.server." + version + ".EntityLiving");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Boolean successfully
    public boolean display(Player p) {
        Location displayLoc = loc.clone().add(0, (ABS * lines.size()) - 1.97D, 0);
        for (int i = 0; i < lines.size(); i++) {
            Object packet = this.getPacket(this.loc.getWorld(), displayLoc.getX(), displayLoc.getY(), displayLoc.getZ(), this.lines.get(i));
            if (packet == null) return false;
            this.sendPacket(p, packet);
            displayLoc.add(0, -ABS, 0);
        }

        return true;
    }

    public Object getPacket(World w, double x, double y, double z, String text) {
        try {
            Constructor<?> cww = armorStand.getConstructor(worldClass);
            Object craftWorldObj = craftWorld.cast(w);
            Method getHandleMethod = craftWorldObj.getClass().getMethod("getHandle");
            Object entityObject = cww.newInstance(getHandleMethod.invoke(craftWorldObj));
            Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
            setCustomName.invoke(entityObject, text);
            Method setCustomNameVisible = nmsEntity.getMethod("setCustomNameVisible", boolean.class);
            setCustomNameVisible.invoke(entityObject, true);
            Method setGravity = entityObject.getClass().getMethod("setGravity", boolean.class);
            setGravity.invoke(entityObject, false);
            Method setLocation = entityObject.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
            setLocation.invoke(entityObject, x, y, z, 0.0F, 0.0F);
            Method setInvisible = entityObject.getClass().getMethod("setInvisible", boolean.class);
            setInvisible.invoke(entityObject, true);
            Constructor<?> cw = packetClass.getConstructor(entityLivingClass);
            return cw.newInstance(entityObject);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendPacket(Player p, Object packet) {
        String path = Bukkit.getServer().getClass().getPackage().getName();
        String version = path.substring(path.lastIndexOf(".") + 1, path.length());
        try {
            Method getHandle = p.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(p);
            Object pConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".Packet");
            Method sendMethod = pConnection.getClass().getMethod("sendPacket", packetClass);
            sendMethod.invoke(pConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
