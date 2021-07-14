package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.magic.Mage;

import de.slikey.effectlib.util.MathUtils;

public class SourceLocation {
    private LocationType locationType;
    private boolean orientToTarget;
    private boolean isSource;

    public enum LocationType {
        CAST,
        EYES,
        HEAD,
        FEET,
        WAND,
        BODY,
        HIT,
        BLOCK,
        BLOCK_CENTER
    }

    @Nullable
    public static SourceLocation tryCreate(Object locationType, boolean isSource) {
        if (locationType instanceof String) {
            return tryCreate((String)locationType, isSource);
        }
        return null;
    }

    @Nullable
    public static SourceLocation tryCreate(String locationTypeString, boolean isSource) {
        if (locationTypeString != null && !locationTypeString.isEmpty()) {
            try {
                LocationType locationType = LocationType.valueOf(locationTypeString.toUpperCase());
                return new SourceLocation(locationType, isSource);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    protected SourceLocation(LocationType locationType, boolean isSource) {
        this.locationType = locationType;
        this.isSource = isSource;
    }

    public SourceLocation(ConfigurationSection configuration) {
        this(configuration, "source_location", true);
    }

    public SourceLocation(String locationTypeString, boolean isSource) {
        this.isSource = isSource;
        if (!locationTypeString.isEmpty()) {
            try {
                locationType = LocationType.valueOf(locationTypeString.toUpperCase());
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().warning("Invalid location type specified in source_location parameter: " + locationTypeString);
            }
        }
    }

    public SourceLocation(ConfigurationSection configuration, String sourceKey, boolean isSource) {
        this(configuration.getString(sourceKey, ""), isSource);

        if (locationType == null) {
            // This is here for backwards-compatibility
            if (configuration.getBoolean("use_block_location", false)) {
                locationType = LocationType.BLOCK;
            } else if (configuration.getBoolean("use_eye_location", false)) {
                locationType = LocationType.EYES;
            } else if (configuration.getBoolean("use_cast_location", false)) {
                locationType = LocationType.CAST;
            } else {
                // Default for source locations is wand, for target locations is hit
                if (isSource) {
                    if (configuration.getBoolean("use_hit_location", false)) {
                        locationType = LocationType.HIT;
                    } else if (configuration.getBoolean("use_wand_location", true)) {
                        locationType = LocationType.WAND;
                    } else if (configuration.getBoolean("use_eye_location", true)) {
                        locationType = LocationType.EYES;
                    }
                } else {
                    if (configuration.getBoolean("use_wand_location", false)) {
                        locationType = LocationType.WAND;
                    } else if (configuration.getBoolean("use_hit_location", true)) {
                        locationType = LocationType.HIT;
                    } else if (configuration.getBoolean("use_eye_location", true)) {
                        locationType = LocationType.EYES;
                    }
                }
            }

            // Fall back to feet if nothing else was specified
            if (locationType == null) {
                locationType = LocationType.FEET;
            }
        }

        orientToTarget = configuration.getBoolean("use_target_location", configuration.getBoolean("orient", isSource));
        // This is a special-case here for CustomProjectile
        if (configuration.getBoolean("reorient", false)) {
            orientToTarget = false;
        }
    }

    @Nullable
    public Block getBlock(EffectContext context) {
        Location location = getLocation(context);
        return location == null ? null : location.getBlock();
    }

    @Nullable
    public Location getLocation(EffectContext context) {
        Mage mage;
        Location eyeLocation;
        Location feetLocation;
        Location blockLocation;
        if (isSource) {
            mage = context instanceof MageContext ? ((MageContext)context).getMage() : null;
            eyeLocation = context.getEyeLocation();
            feetLocation = context.getLocation();
            blockLocation = context.getLocation();
        } else {
            blockLocation = context.getTargetLocation();
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null) {
                mage = null;
                feetLocation = context.getTargetLocation();
                eyeLocation = context.getTargetLocation();
            } else {
                mage = context.getController().getRegisteredMage(targetEntity);
                feetLocation = targetEntity.getLocation();
                eyeLocation = targetEntity instanceof LivingEntity ? ((LivingEntity)targetEntity).getEyeLocation() : targetEntity.getLocation();
            }
        }
        if (mage == null && (locationType == LocationType.CAST || locationType == LocationType.WAND)) {
            locationType = LocationType.EYES;
        }

        Location location = null;
        switch (locationType) {
            case CAST:
                if (isSource)  {
                    location = context.getCastLocation();
                } else {
                    location = mage.getCastLocation();
                }
                break;
            case HEAD:
            case EYES:
                location = eyeLocation;
                break;
            case FEET:
                location = feetLocation;
                break;
            case WAND:
                if (isSource)  {
                    location = context.getWandLocation();
                } else {
                    location = mage.getWandLocation();
                }
                break;
            case BODY:
                if (eyeLocation != null && feetLocation != null) {
                    location = eyeLocation.clone().add(feetLocation).multiply(0.5);
                }
                break;
            case HIT:
                location = context.getTargetLocation();
                break;
            case BLOCK:
                if (blockLocation != null) {
                    location = blockLocation.getBlock().getLocation();
                }
                break;
            case BLOCK_CENTER:
                if (blockLocation != null) {
                    location = blockLocation.getBlock().getLocation().add(0.5, 0.5, 0.5);
                }
                break;
        }
        if (location == null) {
            location = feetLocation;
        }
        Location targetLocation = isSource ? context.getTargetLocation() : context.getLocation();
        if (orientToTarget && targetLocation != null && location != null) {
            Vector direction = targetLocation.toVector().subtract(location.toVector()).normalize();
            if (MathUtils.isFinite(direction.getX()) && MathUtils.isFinite(direction.getY()) && MathUtils.isFinite(direction.getZ())) {
                location.setDirection(direction);
            }
        }
        return location;
    }

    public boolean shouldUseWandLocation() {
        return locationType == LocationType.WAND;
    }

    public boolean shouldUseCastLocation() {
        return locationType == LocationType.CAST;
    }

    public boolean shouldUseEyeLocation() {
        return locationType == LocationType.EYES || locationType == LocationType.HEAD;
    }

    public boolean shouldUseHitLocation() {
        return locationType == LocationType.HIT;
    }

    public boolean shouldUseBlockLocation() {
        return locationType == LocationType.BLOCK;
    }
}
