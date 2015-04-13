package com.elmakers.mine.bukkit.effect;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Random;

public class EffectUtils extends NMSUtils {
    public static void spawnFireworkEffect(Location location, FireworkEffect effect, int power) {
        spawnFireworkEffect(location, effect, power, null, 2, 1);
    }

    public static Entity spawnFireworkEffect(Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown) {
        Entity entity = null;
        try {
            Object world = getHandle(location.getWorld());
            ItemStack itemStack = new ItemStack(Material.FIREWORK);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = getHandle(makeReal(itemStack));
            final Object fireworkHandle = class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);

            if (direction != null) {
                class_Entity_motXField.set(fireworkHandle, direction.getX());
                class_Entity_motYField.set(fireworkHandle, direction.getY());
                class_Entity_motZField.set(fireworkHandle, direction.getZ());
            }

            if (ticksFlown != null) {
                class_Firework_ticksFlownField.set(fireworkHandle, ticksFlown);
            }
            if (expectedLifespan != null) {
                class_Firework_expectedLifespanField.set(fireworkHandle, expectedLifespan);
            }

            if (direction == null)
            {
                Object fireworkPacket = class_PacketSpawnEntityConstructor.newInstance(fireworkHandle, FIREWORK_TYPE);
                Object fireworkId = class_Entity_getIdMethod.invoke(fireworkHandle);
                Object watcher = class_Entity_getDataWatcherMethod.invoke(fireworkHandle);
                Object metadataPacket = class_PacketPlayOutEntityMetadata_Constructor.newInstance(fireworkId, watcher, true);
                Object statusPacket = class_PacketPlayOutEntityStatus_Constructor.newInstance(fireworkHandle, (byte)17);

                Constructor packetDestroyEntityConstructor = class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
                Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {(Integer)fireworkId});

                Collection<Player> players = location.getWorld().getPlayers();

                sendPacket(location, players, fireworkPacket);
                sendPacket(location, players, metadataPacket);
                sendPacket(location, players, statusPacket);
                sendPacket(location, players, destroyPacket);
                return null;
            }

            class_World_addEntityMethod.invoke(world, fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity = (Entity)class_Entity_getBukkitEntityMethod.invoke(fireworkHandle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public static FireworkEffect getFireworkEffect(CastContext context, Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
        Mage mage = context.getMage();
        Random random = context.getRandom();
        Color wandColor = mage == null ? null : mage.getEffectColor();
        if (wandColor != null) {
            color1 = wandColor;
            color2 = wandColor.mixColors(color1, Color.WHITE);
        } else {
            if (color1 == null) {
                color1 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
            if (color2 == null) {
                color2 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
        }
        if (fireworkType == null) {
            fireworkType = org.bukkit.FireworkEffect.Type.values()[random.nextInt(org.bukkit.FireworkEffect.Type.values().length)];
        }
        if (flicker == null) {
            flicker = random.nextBoolean();
        }
        if (trail == null) {
            trail = random.nextBoolean();
        }

        return FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
    }

}
