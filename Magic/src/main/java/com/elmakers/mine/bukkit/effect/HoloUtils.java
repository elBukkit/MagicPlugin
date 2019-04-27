package com.elmakers.mine.bukkit.effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.NMSUtils;

/**
 * With much thanks to zombiekiller753 and Goblom!
 */
public class HoloUtils extends NMSUtils
{
    private static int HORSE_AGE_OFFSET = -1700000; // Magic number for horse rendering glitch
    private static int Y_OFFSET = 55; // Offset to account for above

    @Nullable
    protected static Object createSkull(Location location) {
        Object skull = null;
        try {
            Object world = getHandle(location.getWorld());
            Constructor<?> skullConstructor = class_EntityWitherSkull.getConstructor(class_World);
            skull = skullConstructor.newInstance(world);
            Method setLocationMethod = skull.getClass().getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            setLocationMethod.invoke(skull, location.getX(), location.getY() + Y_OFFSET, location.getZ(), location.getPitch(), location.getYaw());
        } catch (Exception ex) {
            ex.printStackTrace();
            skull = null;
        }

        return skull;
    }

    @Nullable
    protected static Object createHorse(Location location, String text) {
        Object horse = null;
        try {
            Object world = getHandle(location.getWorld());
            Constructor<?> horseConstructor = class_EntityHorse.getConstructor(class_World);
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
            Object horsePacket = class_PacketSpawnLivingEntityConstructor.newInstance(horse);
            Object skullPacket = class_PacketSpawnEntityConstructor.newInstance(skull, WITHER_SKULL_TYPE);
            Constructor<?> packetAttachEntityConstructor = class_PacketPlayOutAttachEntity.getConstructor(Integer.TYPE, class_Entity, class_Entity);
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
            int horseId = (Integer)getHorseIdMethod.invoke(horse);
            int skullId = (Integer)getSkullIdMethod.invoke(skull);

            Constructor<?> packetDestroyEntityConstructor = class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
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
        return new Hologram(location, text);
    }
}
