package com.elmakers.mine.bukkit.utility;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

/**
 * A generic place to put compatibility-based utilities.
 * 
 * These are generally here when there is a new method added
 * to the Bukkti API we'd like to use, but aren't quite
 * ready to give up backwards compatibility.
 * 
 * The easy solution to this problem is to shamelessly copy
 * Bukkit's code in here, mark it as deprecated and then
 * switch everything over once the new Bukkit method is in an
 * official release.
 */
public class CompatibilityUtils {

    /**
     * This is shamelessly copied from org.bukkit.Location.setDirection.
     *
     * It's only here for 1.6 backwards compatibility.
     *
     * This will be removed once there is an RB for 1.7 or 1.8.
     *
     * @param location The Location to set the direction of
     * @param vector the vector to use for the new direction
     * @return Location the resultant Location (same as location)
     */
    @Deprecated
    public static Location setDirection(Location location, Vector vector) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();

        if (x == 0 && z == 0) {
            location.setPitch(vector.getY() > 0 ? -90 : 90);
            return location;
        }

        double theta = Math.atan2(-x, z);
        location.setYaw((float) Math.toDegrees((theta + _2PI) % _2PI));

        double x2 = NumberConversions.square(x);
        double z2 = NumberConversions.square(z);
        double xz = Math.sqrt(x2 + z2);
        location.setPitch((float) Math.toDegrees(Math.atan(-vector.getY() / xz)));

        return location;
    }

    public static void applyPotionEffects(LivingEntity entity, Collection<PotionEffect> effects) {
        for (PotionEffect effect: effects) {
            applyPotionEffect(entity, effect);
        }
    }

    public static void applyPotionEffect(LivingEntity entity, PotionEffect effect) {
        // Avoid nerfing existing effects
        boolean applyEffect = true;
        Collection<PotionEffect> currentEffects = entity.getActivePotionEffects();
        for (PotionEffect currentEffect : currentEffects) {
            if (currentEffect.getType().equals(effect.getType())) {
                if (currentEffect.getAmplifier() > effect.getAmplifier()) {
                    applyEffect = false;
                    break;
                }
            }
        }
        if (applyEffect) {
            entity.addPotionEffect(effect, true);
        }
    }

    public static boolean hasMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return false;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = NMSUtils.getNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = NMSUtils.getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        Object dataRoot = NMSUtils.getNode(pluginsRoot, key);

        return dataRoot != null && NMSUtils.containsNode(dataRoot, plugin.getName());
    }

    public static String getMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return null;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = NMSUtils.getNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = NMSUtils.getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        Object dataRoot = NMSUtils.getNode(pluginsRoot, key);
        if (dataRoot == null) return null;
        return NMSUtils.getMeta(dataRoot, plugin.getName());
    }

    public static boolean setMetadata(ItemStack itemStack, Plugin plugin, String key, String value) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return false;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = NMSUtils.createNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = NMSUtils.createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        Object dataRoot = NMSUtils.createNode(pluginsRoot, key);
        if (dataRoot == null) return false;
        NMSUtils.setMeta(dataRoot, plugin.getName(), value);
        return true;
    }

    public static boolean removeMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return false;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = NMSUtils.createNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = NMSUtils.createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        Object dataRoot = NMSUtils.createNode(pluginsRoot, key);
        if (dataRoot == null) return false;
        NMSUtils.removeMeta(dataRoot, plugin.getName());
        return true;
    }

    public static Object getMetadataNode(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return null;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = NMSUtils.getNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = NMSUtils.getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        Object dataRoot = NMSUtils.getNode(pluginsRoot, key);
        if (dataRoot == null) return null;

        return NMSUtils.getNode(dataRoot, plugin.getName());
    }

    public static Object createMetadataNode(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = InventoryUtils.getHandle(itemStack);
        if (handle == null) return null;
        Object tag = NMSUtils.getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = NMSUtils.createNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = NMSUtils.createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        Object dataRoot = NMSUtils.createNode(pluginsRoot, key);
        if (dataRoot == null) return null;

        return NMSUtils.createNode(dataRoot, plugin.getName());
    }
}
