package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import de.slikey.effectlib.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class SourceLocation {
    private boolean useWandLocation = true;
    private boolean useCastLocation = true;
    private boolean useEyeLocation = false;
    private boolean useTargetLocation = true;

    private Location location;

    public SourceLocation(ConfigurationSection configuration) {
        // This is here for backwards-compatibility
        useWandLocation = configuration.getBoolean("use_wand_location", true);
        if (!useWandLocation) {
            useEyeLocation = true;
            useCastLocation = false;
        }

        useCastLocation = configuration.getBoolean("use_cast_location", useCastLocation);
        useEyeLocation = configuration.getBoolean("use_eye_location", useEyeLocation);
        useTargetLocation = configuration.getBoolean("use_target_location", true);

        // This is a special-case here for CustomProjectile
        if (configuration.getBoolean("reorient", false)) {
            useTargetLocation = false;
        }
    }

    public Location getLocation(CastContext context) {
        if (location != null) {
            return location;
        }
        Mage mage = context.getMage();
        boolean useWand = mage != null && useWandLocation;
        boolean useCast = mage != null && useCastLocation;

        // Order is important here, given how we interpret defaults in the constructor
        if (useEyeLocation) {
            location = context.getEyeLocation();
        }
        if (location == null && useCast) {
            location = context.getCastLocation();
        }
        if (location == null && useWand) {
            location = context.getWandLocation();
        }
        if (location == null) {
            location = context.getLocation();
        }
        Location targetLocation = context.getTargetLocation();
        if (useTargetLocation && targetLocation != null) {
            Vector direction = targetLocation.toVector().subtract(location.toVector()).normalize();
            if (MathUtils.isFinite(direction.getX()) && MathUtils.isFinite(direction.getY()) && MathUtils.isFinite(direction.getZ())) {
                location.setDirection(direction);
            }
        }
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean shouldUseWandLocation() {
        return useWandLocation;
    }

    public boolean shouldUseCastLocation() {
        return useCastLocation;
    }

    public boolean shouldUseEyeLocation() {
        return useEyeLocation;
    }
}
