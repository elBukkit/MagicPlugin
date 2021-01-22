package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckEntityAction extends CheckAction {
    private boolean allowCaster;
    private boolean onlyCaster;
    private Boolean onFire;
    private Set<EntityType> allowedTypes;
    private Set<EntityType> deniedTypes;
    private List<Class<? extends Entity>> allowedClasses;
    private List<Class<? extends Entity>> deniedClasses;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        allowCaster = parameters.getBoolean("allow_caster", true);
        onlyCaster = parameters.getBoolean("only_caster", false);
        if (parameters.contains("on_fire")) {
            onFire = parameters.getBoolean("on_fire");
        }

        if (parameters.contains("allowed_entities")) {
            List<String> keys = ConfigurationUtils.getStringList(parameters, "allowed_entities");
            allowedTypes = new HashSet<>();
            for (String key : keys) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    allowedTypes.add(entityType);
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid entity type in CheckEntity configuration: " + key);
                }
            }
        }
        if (parameters.contains("denied_entities")) {
            List<String> keys = ConfigurationUtils.getStringList(parameters, "denied_entities");
            deniedTypes = new HashSet<>();
            for (String key : keys) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    deniedTypes.add(entityType);
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid entity type in CheckEntity configuration: " + key);
                }
            }
        }

        if (parameters.contains("allowed_entity_classes")) {
            List<String> keys = ConfigurationUtils.getStringList(parameters, "allowed_entity_classes");
            allowedClasses = new ArrayList<>();
            for (String key : keys) {
                try {
                    Class<?> rawClass = Class.forName("org.bukkit.entity." + key);
                    if (!Entity.class.isAssignableFrom(rawClass)) {
                        context.getLogger().warning("Invalid entity class in CheckEntity configuration, does not extend from Entity: " + rawClass.getName());
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Class<? extends Entity> targetEntityType = (Class<? extends Entity>)rawClass;
                    allowedClasses.add(targetEntityType);
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid entity class in CheckEntity configuration: " + key);
                }
            }
        }
        if (parameters.contains("denied_entity_classes")) {
            List<String> keys = ConfigurationUtils.getStringList(parameters, "denied_entity_classes");
            deniedClasses = new ArrayList<>();
            for (String key : keys) {
                try {
                    Class<?> rawClass = Class.forName("org.bukkit.entity." + key);
                    if (!Entity.class.isAssignableFrom(rawClass)) {
                        context.getLogger().warning("Invalid entity class in CheckEntity configuration, does not extend from Entity: " + rawClass.getName());
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Class<? extends Entity> targetEntityType = (Class<? extends Entity>)rawClass;
                    deniedClasses.add(targetEntityType);
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid entity class in CheckEntity configuration: " + key);
                }
            }
        }
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) return false;
        boolean isCaster = targetEntity == context.getEntity();
        if (!allowCaster && isCaster) {
            return false;
        }
        if (onlyCaster && !isCaster) {
            return false;
        }
        if (onFire != null && onFire != (targetEntity.getFireTicks() > 0)) {
            return false;
        }
        if (deniedTypes != null && deniedTypes.contains(targetEntity.getType())) {
            return false;
        }
        if (deniedClasses != null) {
            for (Class<? extends Entity> entityClass : deniedClasses) {
                if (entityClass.isAssignableFrom(targetEntity.getClass())) {
                    return false;
                }
            }
        }

        boolean anyAllowed = allowedTypes == null && allowedClasses == null;
        if (!anyAllowed && allowedTypes != null) {
            anyAllowed = allowedTypes.contains(targetEntity.getType());
        }
        if (!anyAllowed && allowedClasses != null) {
            for (Class<? extends Entity> entityClass : allowedClasses) {
                if (entityClass.isAssignableFrom(targetEntity.getClass())) {
                    anyAllowed = true;
                    break;
                }
            }
        }
        return anyAllowed;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
