package com.elmakers.mine.bukkit.effect;

import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * With much thanks to zombiekiller753 and Goblom!
 */
public class HoloUtils extends NMSUtils
{
    private static int WITHER_SKULL_TYPE = 66;
    private static int HORSE_AGE_OFFSET = -1700000; // Magic number for horse rendering glitch
    private static int Y_OFFSET = 55; // Offset to account for above

    protected static boolean enabled = true;
    protected static Class<?> class_EntityHorse;
    protected static Class<?> class_EntityWitherSkull;
    protected static Class<?> class_PacketPlayOutAttachEntity;
    protected static Class<?> class_PacketPlayOutEntityDestroy;
    protected static Class<?> class_PacketPlayOutSpawnEntity;
    protected static Class<?> class_PacketPlayOutSpawnEntityLiving;

    static
    {
        try {
            class_EntityHorse = fixBukkitClass("net.minecraft.server.EntityHorse");
            class_EntityWitherSkull = fixBukkitClass("net.minecraft.server.EntityWitherSkull");
            class_PacketPlayOutAttachEntity = fixBukkitClass("net.minecraft.server.PacketPlayOutAttachEntity");
            class_PacketPlayOutEntityDestroy = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityDestroy");
            class_PacketPlayOutSpawnEntity = fixBukkitClass("net.minecraft.server.PacketPlayOutSpawnEntity");
            class_PacketPlayOutSpawnEntityLiving = fixBukkitClass("net.minecraft.server.PacketPlayOutSpawnEntityLiving");
        }
        catch (Throwable ex) {
            enabled = false;
            ex.printStackTrace();
        }
    }

    protected static Object createSkull(Location location)
    {
        Object skull = null;
        try {
            Object world = getHandle(location.getWorld());
            Constructor skullConstructor = class_EntityWitherSkull.getConstructor(class_World);
            skull = skullConstructor.newInstance(world);
            Method setLocationMethod = skull.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            setLocationMethod.invoke(skull, location.getX(), location.getY() + Y_OFFSET, location.getZ(), location.getPitch(), location.getYaw());
        } catch (Exception ex) {
            ex.printStackTrace();
            skull = null;
        }

        return skull;
    }

    protected static Object createHorse(Location location, String text)
    {
        Object horse = null;
        try {
            Object world = getHandle(location.getWorld());
            Constructor horseConstructor = class_EntityHorse.getConstructor(class_World);
            horse = horseConstructor.newInstance(world);

            Method setAgeMethod = horse.getClass().getMethod("setAge", Integer.TYPE);
            setAgeMethod.invoke(horse, HORSE_AGE_OFFSET);

            Method setCustomNameVisibleMethod = horse.getClass().getMethod("setCustomNameVisible", Boolean.TYPE);
            setCustomNameVisibleMethod.invoke(horse, true);

            Method setLocationMethod = horse.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            setLocationMethod.invoke(horse, location.getX(), location.getY() + Y_OFFSET, location.getZ(), location.getPitch(), location.getYaw());

            Method setCustomNameMethod = horse.getClass().getMethod("setCustomName", String.class);
            setCustomNameMethod.invoke(horse, text);

        } catch (Exception ex) {
            ex.printStackTrace();
            horse = null;
        }

        return horse;
    }

    protected static boolean teleport(Location location, Object skull, Object horse)
    {
        try {
            Method setLocationMethod = horse.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            setLocationMethod.invoke(horse, location.getX(), location.getY() + Y_OFFSET, location.getZ(), location.getPitch(), location.getYaw());
            setLocationMethod = skull.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            setLocationMethod.invoke(skull, location.getX(), location.getY() + Y_OFFSET, location.getZ(), location.getPitch(), location.getYaw());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    protected static boolean rename(String text, Object horse)
    {
        try {
            Method setCustomNameMethod = horse.getClass().getMethod("setCustomName", String.class);
            setCustomNameMethod.invoke(horse, text);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    protected static boolean sendToPlayer(Player player, Object skull, Object horse)
    {
        try {
            Constructor packetSpawnLivingEntityConstructor = class_PacketPlayOutSpawnEntityLiving.getConstructor(class_EntityLiving);
            Object horsePacket = packetSpawnLivingEntityConstructor.newInstance(horse);
            Constructor packetSpawnEntityConstructor = class_PacketPlayOutSpawnEntity.getConstructor(class_Entity, Integer.TYPE);
            Object skullPacket = packetSpawnEntityConstructor.newInstance(skull, WITHER_SKULL_TYPE);
            Constructor packetAttachEntityConstructor = class_PacketPlayOutAttachEntity.getConstructor(Integer.TYPE, class_Entity, class_Entity);
            Object attachPacket = packetAttachEntityConstructor.newInstance(0, horse, skull);

            sendPacket(player, horsePacket);
            sendPacket(player, skullPacket);
            sendPacket(player, attachPacket);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    protected static boolean removeFromPlayer(Player player, Object skull, Object horse)
    {
        try {
            Method getHorseIdMethod = horse.getClass().getMethod("getId");
            Method getSkullIdMethod = skull.getClass().getMethod("getId");
            int horseId = (int)(Integer)getHorseIdMethod.invoke(horse);
            int skullId = (int)(Integer)getSkullIdMethod.invoke(skull);

            Constructor packetDestroyEntityConstructor = class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
            Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {horseId, skullId});

            sendPacket(player, destroyPacket);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static Hologram createHoloText(Location location, String text)
    {
        if (!enabled) return null;

        return new Hologram(location, text);
    }
}