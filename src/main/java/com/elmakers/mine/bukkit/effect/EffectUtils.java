package com.elmakers.mine.bukkit.effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.plugin.Plugin;

public class EffectUtils extends NMSUtils {
    private static int FIREWORK_TYPE = 76;
    private static byte FIREWORK_STATUS_DETONATE = 17;

    private static int FIREWORK_RANGE = 64;

    protected static Class<?> class_EntityFirework;
    protected static Class<?> class_PacketPlayOutSpawnEntity;
    protected static Class<?> class_PacketPlayOutEntityStatus;
    protected static Class<?> class_PacketPlayOutEntityDestroy;

    static
    {
        try {
            class_EntityFirework = fixBukkitClass("net.minecraft.server.EntityFireworks");
            class_PacketPlayOutSpawnEntity = fixBukkitClass("net.minecraft.server.PacketPlayOutSpawnEntity");
            class_PacketPlayOutEntityStatus = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityStatus");
            class_PacketPlayOutEntityDestroy = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityDestroy");
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static Collection<Player> getPlayers(Location center, double range) {
        Collection<Player> players = new ArrayList<Player>();
        double rangeSquared = range * range;
        Collection<Player> worldPlayers = center.getWorld().getPlayers();
        for (Player player : worldPlayers) {
            if (player.getLocation().distanceSquared(center) <= rangeSquared) {
                players.add(player);
            }
        }
        return players;
    }

    public static void spawnFireworkEffectOld(Plugin plugin, Location location, FireworkEffect effect, int power) {
        World world = location.getWorld();

        try {
            // Initialize the Firework object
            final Firework firework = world.spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            firework.setFireworkMeta(meta);

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    firework.detonate();

                    // Remove the firework, we only needed it for the effect
                    firework.remove();
                    Bukkit.getLogger().info("Detonated");
                }
            }, 10L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void spawnFireworkEffect(Location location, FireworkEffect effect, int power) {
        try {
            Object world = getHandle(location.getWorld());
            Constructor fireworkConstructor = class_EntityFirework.getConstructor(class_World, Double.TYPE, Double.TYPE, Double.TYPE, class_ItemStack);
            ItemStack itemStack = new ItemStack(Material.FIREWORK);

            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = getHandle(makeReal(itemStack));
            final Object fireworkHandle = fireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);

            Field class_Firework_ticksFlownField = class_EntityFirework.getDeclaredField("ticksFlown");
            class_Firework_ticksFlownField.setAccessible(true);
            Field class_Firework_expectedLifespanField = class_EntityFirework.getDeclaredField("expectedLifespan");
            class_Firework_expectedLifespanField.setAccessible(true);

            class_Firework_ticksFlownField.set(fireworkHandle, 1);
            class_Firework_expectedLifespanField.set(fireworkHandle, 2);

            Method addEntity = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);
            addEntity.invoke(world, fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void spawnFireworkEffectPackets(Plugin plugin, Location location, FireworkEffect effect, int power) {
        final Collection<Player> players = getPlayers(location, FIREWORK_RANGE);
        if (players.size() == 0) {
            return;
        }
        try {
            Object world = getHandle(location.getWorld());
            Constructor fireworkConstructor = class_EntityFirework.getConstructor(class_World, Double.TYPE, Double.TYPE, Double.TYPE, class_ItemStack);
            ItemStack itemStack = new ItemStack(Material.FIREWORK);

            FireworkMeta meta = (FireworkMeta)itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = getHandle(makeReal(itemStack));
            Bukkit.getLogger().info("ITEM: " + item + ", tag: " + getTag(item));
            final Object firework = fireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);

            Field class_Firework_ticksFlownField = class_EntityFirework.getDeclaredField("ticksFlown");
            class_Firework_ticksFlownField.setAccessible(true);
            Field class_Firework_expectedLifespanField = class_EntityFirework.getDeclaredField("expectedLifespan");
            class_Firework_expectedLifespanField.setAccessible(true);

            class_Firework_ticksFlownField.set(firework, (Integer)class_Firework_expectedLifespanField.get(firework) + 1);

            Constructor packetSpawnEntityConstructor = class_PacketPlayOutSpawnEntity.getConstructor(class_Entity, Integer.TYPE);
            Object fireworkPacket = packetSpawnEntityConstructor.newInstance(firework, FIREWORK_TYPE);

            Constructor packetEntityStatusConstructor = class_PacketPlayOutEntityStatus.getConstructor(class_Entity, Byte.TYPE);
            final Object statusPacket = packetEntityStatusConstructor.newInstance(firework, FIREWORK_STATUS_DETONATE);

            Method getIdMethod = firework.getClass().getMethod("getId");
            int fireworkId = (int)(Integer)getIdMethod.invoke(firework);

            Constructor packetDestroyEntityConstructor = class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
            final Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {fireworkId});

            Firework bukkitFirework = (Firework)getBukkitEntity(firework);
            Bukkit.getLogger().info("Firework meta: " + bukkitFirework.getFireworkMeta());

            for (Player player : players) {
                sendPacket(player, fireworkPacket);
                Bukkit.getLogger().info("Launched to " + player.getName());
            }

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Player player : players) {
                        try {
                            sendPacket(player, statusPacket);
                            Bukkit.getLogger().info("Detonated to " + player.getName());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }, 1L);
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Player player : players) {
                        try {
                            sendPacket(player, destroyPacket);
                            Bukkit.getLogger().info("Removed from " + player.getName());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }, 2L);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
