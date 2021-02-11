package com.elmakers.mine.bukkit.entity;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class EntityAreaEffectCloudData extends EntityExtraData {
    private Color color;
    private PotionData basePotionData;
    private List<PotionEffect> potionEffects;
    private int duration;
    private int durationOnUse;
    private Particle particle;
    private float radius;
    private float radiusOnUse;
    private float radiusPerTick;
    private int reapplicationDelay;
    private int waitTime;

    public EntityAreaEffectCloudData() {

    }

    public EntityAreaEffectCloudData(ConfigurationSection parameters, MageController controller) {
        if (parameters.contains("color")) {
            ColorHD colorHD = new ColorHD(parameters.getString("color"));
            color = colorHD.getColor();
        }
        String potionTypeKey = parameters.getString("base_potion_type");
        if (potionTypeKey != null && !potionTypeKey.isEmpty()) {
            PotionType potionType;
            try {
                potionType = PotionType.valueOf(potionTypeKey.toUpperCase());
                basePotionData = new PotionData(potionType);
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid base_potion_type: " + potionTypeKey);
            }
        }
        String particleKey = parameters.getString("particle");
        if (particleKey != null && !particleKey.isEmpty()) {
            try {
                particle = Particle.valueOf(particleKey.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid particle: " + particleKey);
            }
        }
        duration = parameters.getInt("duration", 2000) * 20 / 1000;
        durationOnUse = parameters.getInt("duration_on_use", 0) * 20 / 1000;
        reapplicationDelay = parameters.getInt("reapplication_delay", 0) * 20 / 1000;
        waitTime = parameters.getInt("wait_time", 0) * 20 / 1000;
        radius = (float)parameters.getDouble("radius", 5);
        radiusOnUse = (float)parameters.getDouble("radius_on_use", 0);
        radiusPerTick = (float)parameters.getDouble("radius_per_tick", 0);

        potionEffects = ConfigurationUtils.getPotionEffectObjects(parameters, "potion_effects", controller.getLogger(), duration);
    }

    public EntityAreaEffectCloudData(AreaEffectCloud cloud) {
        color = cloud.getColor();
        basePotionData = cloud.getBasePotionData();
        potionEffects = cloud.getCustomEffects();
        duration = cloud.getDuration();
        durationOnUse = cloud.getDurationOnUse();
        particle = cloud.getParticle();
        radius = cloud.getRadius();
        radiusOnUse = cloud.getRadiusOnUse();
        radiusPerTick = cloud.getRadiusPerTick();
        reapplicationDelay = cloud.getReapplicationDelay();
        waitTime = cloud.getWaitTime();
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof AreaEffectCloud) {
            AreaEffectCloud cloud = (AreaEffectCloud)entity;
            if (color != null) cloud.setColor(color);
            if (basePotionData != null) cloud.setBasePotionData(basePotionData);
            if (potionEffects != null) {
                for (PotionEffect effect : potionEffects) {
                    cloud.addCustomEffect(effect, true);
                }
            }
            cloud.setDuration(duration);
            cloud.setDurationOnUse(durationOnUse);
            if (particle != null) {
                cloud.setParticle(particle);
            }
            cloud.setRadius(radius);
            cloud.setRadiusOnUse(radiusOnUse);
            cloud.setRadiusPerTick(radiusPerTick);
            cloud.setWaitTime(waitTime);
            cloud.setReapplicationDelay(reapplicationDelay);
        }
    }
}
