package com.elmakers.mine.bukkit.effect;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.elmakers.mine.bukkit.utility.NMSUtils;

public class EffectUtils extends NMSUtils {

    public static void spawnFireworkEffect(Location location, FireworkEffect effect, int power) {
        try {
            Object world = getHandle(location.getWorld());
            ItemStack itemStack = new ItemStack(Material.FIREWORK);

            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = getHandle(makeReal(itemStack));
            final Object fireworkHandle = class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);

            class_Firework_ticksFlownField.set(fireworkHandle, 2);
            class_Firework_expectedLifespanField.set(fireworkHandle, 1);

            class_World_addEntityMethod.invoke(world, fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
