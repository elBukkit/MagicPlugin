package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import de.slikey.effectlib.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class SourceLocation {
    private LocationType locationType;
    private boolean orientToTarget;
    private boolean isSource;

    private enum LocationType {
        CAST,
        EYES,
        FEET,
        WAND,
        BODY,
        HIT,
        BLOCK
    }

    public SourceLocation(ConfigurationSection configuration) {
        this(configuration, "source_location", true);
    }

    public SourceLocation(ConfigurationSection configuration, String sourceKey, boolean isSource) {
        this.isSource = isSource;
        // The new format overrides any of the old ones
        String locationTypeString = configuration.getString(sourceKey, "");
        if (!locationTypeString.isEmpty()) {
            try {
                locationType = LocationType.valueOf(locationTypeString.toUpperCase());
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().warning("Invalid location type specified in source_location parameter: " + locationTypeString);
            }
        }

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
                    }
                } else {
                    if (configuration.getBoolean("use_wand_location", false)) {
                        locationType = LocationType.WAND;
                    } else if (configuration.getBoolean("use_hit_location", true)) {
                        locationType = LocationType.HIT;
                    }
                }
            }

            // Fall back to feet if nothing else was specified
            if (locationType == null) {
                locationType = LocationType.FEET;
            }
        }

        orientToTarget = configuration.getBoolean("use_target_location", true);
        // This is a special-case here for CustomProjectile
        if (!isSource || configuration.getBoolean("reorient", false)) {
            orientToTarget = false;
        }
    }

    public Location getLocation(CastContext context) {
        Mage mage;
        Location eyeLocation;
        Location feetLocation;
        if (isSource) {
            mage = context.getMage();
            eyeLocation = context.getEyeLocation();
            feetLocation = context.getLocation();
        } else {
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
                if (feetLocation != null) {
                    location = feetLocation.getBlock().getLocation();
                }
                break;
        }
        if (location == null) {
            location = feetLocation;
        }
        Location targetLocation = context.getTargetLocation();
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
        return locationType == LocationType.EYES;
    }

    public boolean shouldUseHitLocation() {
        return locationType == LocationType.HIT;
    }

    public boolean shouldUseBlockLocation() {
        return locationType == LocationType.BLOCK;
    }
}
