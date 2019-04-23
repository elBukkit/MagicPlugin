package com.elmakers.mine.bukkit.effect;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class EffectUtils extends NMSUtils {
    public static void spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power) {
        spawnFireworkEffect(server, location, effect, power, null, 2, 1);
    }

    public static void spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, boolean silent) {
        spawnFireworkEffect(server, location, effect, power, null, 2, 1, silent);
    }

    @Nullable
    public static Entity spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown) {
        return spawnFireworkEffect(server, location, effect, power, direction, expectedLifespan, ticksFlown, false);
    }

    @Nullable
    public static Entity spawnFireworkEffect(Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        Entity entity = null;
        try {
            Material fireworkMaterial = DefaultMaterials.getFirework();
            if (fireworkMaterial == null) {
                return null;
            }
            Object world = getHandle(location.getWorld());
            ItemStack itemStack = new ItemStack(fireworkMaterial);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = getHandle(makeReal(itemStack));
            final Object fireworkHandle = class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);
            CompatibilityUtils.setSilent(fireworkHandle, silent);

            if (direction != null) {
                if (class_Entity_motField != null) {
                    Object vec = class_Vec3D_constructor.newInstance(direction.getX(), direction.getY(), direction.getZ());
                    class_Entity_motField.set(fireworkHandle, vec);
                } else if (class_Entity_motXField != null) {
                    class_Entity_motXField.set(fireworkHandle, direction.getX());
                    class_Entity_motYField.set(fireworkHandle, direction.getY());
                    class_Entity_motZField.set(fireworkHandle, direction.getZ());
                }
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

                Constructor<?> packetDestroyEntityConstructor = class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
                Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {(Integer)fireworkId});

                Collection<? extends Player> players = server.getOnlinePlayers();
                sendPacket(server, location, players, fireworkPacket);
                sendPacket(server, location, players, metadataPacket);
                sendPacket(server, location, players, statusPacket);
                sendPacket(server, location, players, destroyPacket);
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
        return getFireworkEffect(context, color1, color2, fireworkType, flicker, trail, true);
    }

    public static FireworkEffect getFireworkEffect(CastContext context, Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail, boolean useWandColor) {
        Mage mage = context.getMage();
        Random random = context.getRandom();
        Color wandColor = mage.getEffectColor();
        if (wandColor != null && useWandColor) {
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
