package com.elmakers.mine.bukkit.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HitboxUtils extends CompatibilityUtils {
    private static final Map<EntityType, BoundingBox> hitboxes = new HashMap<>();
    private static final Map<EntityType, Double> headSizes = new HashMap<>();
    private static double hitboxScale = 1.0;
    private static double hitboxScaleY = 1.0;
    private static double hitboxSneakScaleY = 0.75;
    private static BoundingBox defaultHitbox;

    public static BoundingBox getHitbox(Block block)
    {
        int blockX = block.getX();
        int blockY = block.getY();
        int blockZ = block.getZ();

        // We make these slightly small to ensure the coordinates stay within the block!
        return new BoundingBox(blockX + 0.001, blockX + 0.999, blockY + 0.001, blockY + 0.999, blockZ + 0.001, blockZ + 0.999);
    }

    @Nullable
    public static BoundingBox getHitbox(Entity entity) {
        if (entity == null)
        {
            return null;
        }
        BoundingBox hitbox = hitboxes.get(entity.getType());
        if (hitbox != null)
        {
            return hitbox.center(entity.getLocation().toVector());
        }

        if (class_Entity_getBoundingBox != null) {
            try {
                Object entityHandle = getHandle(entity);
                Object aabb = class_Entity_getBoundingBox.invoke(entityHandle);
                if (aabb == null) {
                    return defaultHitbox.center(entity.getLocation().toVector());
                }

                double scaleY = hitboxScaleY;
                if (entity instanceof Player && ((Player)entity).isSneaking()) {
                    scaleY = hitboxSneakScaleY;
                }
                return new BoundingBox(
                        class_AxisAlignedBB_minXField.getDouble(aabb),
                        class_AxisAlignedBB_maxXField.getDouble(aabb),
                        class_AxisAlignedBB_minYField.getDouble(aabb),
                        class_AxisAlignedBB_maxYField.getDouble(aabb),
                        class_AxisAlignedBB_minZField.getDouble(aabb),
                        class_AxisAlignedBB_maxZField.getDouble(aabb)
                ).scaleFromBase(hitboxScale, scaleY);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return defaultHitbox.center(entity.getLocation().toVector());
    }

    public static void setHitboxScale(double scale) {
        hitboxScale = scale;
    }

    public static void setHitboxScaleY(double scaleY) {
        hitboxScaleY = scaleY;
    }

    public static void setHitboxSneakScaleY(double scaleY) {
        hitboxSneakScaleY = scaleY;
    }

    public static boolean isHeadshot(Entity target, Location hitLocation) {
        if (target == null) return false;
        Double headSize = headSizes.get(target.getType());
        if (headSize == null) return false;
        Location eyeLocation = null;
        if (target instanceof LivingEntity) {
            eyeLocation = ((LivingEntity)target).getEyeLocation();
        } else {
            eyeLocation = target.getLocation();
        }
        if (!eyeLocation.getWorld().equals(hitLocation.getWorld())) return false;
        double distance = Math.abs(hitLocation.getY() - eyeLocation.getY());
        return distance <= headSize;
    }

    public static void configureHeadSizes(ConfigurationSection config) {
        headSizes.clear();
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                double size = config.getDouble(key);
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                if (size > 0)
                {
                    headSizes.put(entityType, size);
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void configureHitboxes(ConfigurationSection config) {
        hitboxes.clear();
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                Vector bounds = ConfigurationUtils.getVector(config, key);
                String upperKey = key.toUpperCase();
                double halfX = bounds.getX() / 2;
                double halfZ = bounds.getZ() / 2;
                BoundingBox bb = new BoundingBox(-halfX, halfX, 0, bounds.getY(), -halfZ, halfZ).scaleFromBase(hitboxScale, hitboxScaleY);
                if (upperKey.equals("DEFAULT")) {
                    defaultHitbox = bb;
                    continue;
                }
                EntityType entityType = EntityType.valueOf(upperKey);
                hitboxes.put(entityType, bb);
            } catch (Exception ignore) {
            }
        }
    }
}
