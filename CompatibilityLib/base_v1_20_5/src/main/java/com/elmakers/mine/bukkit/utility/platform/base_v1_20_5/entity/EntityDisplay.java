package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.utilities.AngleUtils;

public abstract class EntityDisplay extends EntityExtraData {
    private Transformation transformation;
    private Float viewRange;
    private Float shadowRadius;

    public EntityDisplay(ConfigurationSection configuration, MageController controller) {
        ConfigurationSection transformationConfig = configuration.getConfigurationSection("transformation");
        if (transformationConfig != null) {
            final Vector3f translation = AngleUtils.parseVector(transformationConfig, "translation", new Vector3f());
            final Vector3f scale = AngleUtils.parseVector(transformationConfig, "scale", new Vector3f());
            final AxisAngle4f leftRotation = AngleUtils.parseAngle(transformationConfig, "rotation_left", new AxisAngle4f());
            final AxisAngle4f rightRotation = AngleUtils.parseAngle(transformationConfig, "rotation_right", new AxisAngle4f());
            transformation = new Transformation(translation, leftRotation, scale, rightRotation);
        }
        viewRange = configuration.contains("view_range") ? (float)configuration.getDouble("view_range") : null;
        shadowRadius = configuration.contains("shadow_radius") ? (float)configuration.getDouble("shadow_radius") : null;
    }

    public EntityDisplay(Entity entity, MageController controller) {
        if (entity instanceof Display) {
            Display display = (Display) entity;
            transformation = display.getTransformation();
            viewRange = display.getViewRange();
            shadowRadius = display.getShadowRadius();
        }
    }

    public void apply(Entity entity) {
        if (entity instanceof Display) {
            Display display = (Display)entity;
            if (transformation != null) {
                display.setTransformation(transformation);
            }
            if (viewRange != null) {
                display.setViewRange(viewRange);
            }
            if (shadowRadius != null) {
                display.setShadowRadius(shadowRadius);
            }
        }
    }
}
