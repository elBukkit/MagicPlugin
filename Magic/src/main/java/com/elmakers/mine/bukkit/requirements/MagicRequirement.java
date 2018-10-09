package com.elmakers.mine.bukkit.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public  class MagicRequirement {
    private static class PropertyRequirement {
        public final String key;
        public Double max;
        public Double min;
        public Double value;

        public PropertyRequirement(String type, ConfigurationSection configuration) {
            key = configuration.getString(type);
            if (configuration.contains("min")) {
                min = configuration.getDouble("min");
            }
            if (configuration.contains("max")) {
                max = configuration.getDouble("max");
            }
            if (configuration.contains("value")) {
                value = configuration.getDouble("value");
            }
        }

        @Override
        public String toString() {
            return "[Require " + key + "=" + value + " from (" + min + " to " + max + ")]";
        }
    }

    private @Nonnull final MageController controller;
    private @Nullable String permissionNode = null;
    private @Nullable String requiredPath = null;
    private @Nullable String requiredTemplate = null;
    private @Nullable Set<String> requiredTemplates = null;
    private @Nullable List<String> wandTags = null;
    private @Nullable String requiresCompletedPath = null;
    private @Nullable String exactPath = null;
    private @Nullable String mageClass = null;
    private @Nullable String activeClass = null;
    private @Nullable List<PropertyRequirement> wandProperties = null;
    private @Nullable List<PropertyRequirement> classProperties = null;
    private @Nullable List<PropertyRequirement> attributes = null;
    private boolean requireWand = false;

    public MagicRequirement(@Nonnull MageController controller, @Nonnull Requirement requirement) {
        this.controller = controller;
        ConfigurationSection configuration = requirement.getConfiguration();

        permissionNode = configuration.getString("permission");
        requiredPath = configuration.getString("path");
        exactPath = configuration.getString("path_exact");
        requiresCompletedPath = configuration.getString("path_end");
        requiredTemplate = configuration.getString("wand");
        requireWand = configuration.getBoolean("holding_wand");
        mageClass = configuration.getString("class");
        activeClass = configuration.getString("active_class");
        wandTags = ConfigurationUtils.getStringList(configuration, "wand_tags");
        if (activeClass != null && mageClass == null) {
            mageClass = activeClass;
        }

        if (configuration.contains("wands")) {
            requiredTemplates = new HashSet<>(ConfigurationUtils.getStringList(configuration, "wands"));
        }

        wandProperties = parsePropertyRequirements(configuration, "wand_properties", "property");
        classProperties = parsePropertyRequirements(configuration, "class_properties", "property");
        attributes = parsePropertyRequirements(configuration, "attributes", "attribute");

        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
            exactPath = requiresCompletedPath;
        }

        if (requiredTemplate != null || requiredTemplates != null || wandProperties != null || wandTags != null) {
            requireWand = true;
        }
    }

    @Nullable
    private List<PropertyRequirement> parsePropertyRequirements(ConfigurationSection configuration, String section, String type) {
        if (!configuration.contains(section)) return null;

        List<PropertyRequirement> requirements = new ArrayList<>();
        Collection<ConfigurationSection> propertyConfigs = ConfigurationUtils.getNodeList(configuration, section);
        for (ConfigurationSection propertyConfig : propertyConfigs) {
            if (!propertyConfig.contains(type)) {
                controller.getLogger().warning("Property requirement missing " + type + " parameter");
                continue;
            }

            PropertyRequirement requirement = new PropertyRequirement(type, propertyConfig);
            requirements.add(requirement);
        }
        return requirements;
    }

    public boolean checkRequirement(@Nonnull CastContext context) {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();

        if (permissionNode != null && (player == null || !player.hasPermission(permissionNode))) {
            return false;
        }
        Wand wand = context.getWand();
        if (wand == null && requireWand) {
            return false;
        }

        if (wandTags != null) {
            if (!hasTags(wand)) {
                return false;
            }
        }

        if (requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(requiredTemplate)) {
                return false;
            }
        }

        if (requiredTemplates != null) {
            String template = wand.getTemplateKey();
            if (template == null || !requiredTemplates.contains(template)) {
                return false;
            }
        }

        CasterProperties checkProperties = context.getActiveProperties();
        ProgressionPath path = checkProperties.getPath();

        if (mageClass != null && !mageClass.isEmpty()) {
            if (!mage.hasClassUnlocked(mageClass)) {
                return false;
            }
        }
        if (activeClass != null && !activeClass.isEmpty()) {
            MageClass currentClass = mage.getActiveClass();
            if (currentClass == null || !currentClass.getKey().equals(activeClass)) {
                return false;
            }
        }

        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                return false;
            }

            if (requiredPath != null && !path.hasPath(requiredPath)) {
                return false;
            }
            if (exactPath != null && !exactPath.equals(path.getKey())) {
                return false;
            }
            if (requiresCompletedPath != null) {
                boolean hasPathCompleted = false;
                if (path.hasPath(requiresCompletedPath)) {
                    if (path.getKey().equals(requiresCompletedPath)) {
                        hasPathCompleted = !path.canProgress(checkProperties);
                    } else {
                        hasPathCompleted = true;
                    }
                }

                if (!hasPathCompleted) {
                    return false;
                }
            }
        }

        if (wandProperties != null) {
            if (!checkProperties(wand, wandProperties)) {
                return false;
            }
        }

        if (classProperties != null) {
            MageClass activeClass = mageClass == null ? mage.getActiveClass() : mage.getClass(mageClass);
            if (activeClass == null) {
                return false;
            }
            if (!checkProperties(activeClass, classProperties)) {
                return false;
            }
        }

        if (attributes != null) {
            for (PropertyRequirement requirement : attributes) {
                String key = requirement.key;
                Double value = mage.getAttribute(key);
                if (!checkProperty(requirement, value)) {
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean checkProperties(CasterProperties properties, List<PropertyRequirement> requirements) {
        for (PropertyRequirement requirement : requirements) {
            String key = requirement.key;
            Double value = properties.hasProperty(key) ? properties.getProperty(key, 0.0) : null;
            if (!checkProperty(requirement, value)) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkProperty(PropertyRequirement requirement, Double value) {
        if (requirement.value != null && (value == null || !value.equals(requirement.value))) return false;
        if (requirement.min != null && (value == null || value <= requirement.min)) return false;
        if (requirement.max != null && (value != null && value >= requirement.max)) return false;
        return true;
    }

    protected String getMessage(CastContext context, String key) {
        return context.getMessage(key, getDefaultMessage(context, key));
    }

    protected String getDefaultMessage(CastContext context, String key) {
        return context.getController().getMessages().get("requirements." + key);
    }

    @Nullable
    public String getRequirementDescription(@Nonnull CastContext context) {
        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Player player = mage.getPlayer();

        if (permissionNode != null && (player == null || !player.hasPermission(permissionNode))) {
            return context.getMessage(SpellResult.INSUFFICIENT_PERMISSION.name().toLowerCase());
        }
        Wand wand = context.getWand();
        if (wand == null && requireWand) {
            return getMessage(context, "no_wand");
        }

        if (requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(requiredTemplate)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (requiredTemplates != null) {
            String template = wand.getTemplateKey();
            if (template == null || !requiredTemplates.contains(template)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (wandTags != null) {
            if (!hasTags(wand)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (mageClass != null && !mageClass.isEmpty()) {
            if (mage.hasClassUnlocked(mageClass)) {
                return getMessage(context, "no_class").replace("$class", mageClass);
            }
        }

        if (activeClass != null && !activeClass.isEmpty()) {
            MageClass currentClass = mage.getActiveClass();
            if (currentClass == null || !currentClass.getKey().equals(activeClass)) {
                return getMessage(context, "no_class").replace("$class", activeClass);
            }
        }

        CasterProperties checkProperties = context.getActiveProperties();
        ProgressionPath path = checkProperties.getPath();

        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                return getMessage(context, "no_path");
            }

            if (requiredPath != null && !path.hasPath(requiredPath)) {
                WandUpgradePath requiresPath = controller.getPath(requiredPath);
                String pathName = requiredPath;
                if (requiresPath != null) {
                    pathName = requiresPath.getName();
                } else {
                    context.getLogger().warning("Invalid path specified in requirement " + requiredPath);
                }
                return getMessage(context, "no_required_path").replace("$path", pathName);
            }
            if (exactPath != null && !exactPath.equals(path.getKey())) {
                WandUpgradePath requiresPath = controller.getPath(exactPath);
                String pathName = exactPath;
                if (requiresPath != null) {
                    pathName = requiresPath.getName();
                } else {
                    context.getLogger().warning("Invalid path specified in requirement: " + exactPath);
                }
                return getMessage(context, "no_path_exact").replace("$path", pathName);
            }
            if (requiresCompletedPath != null) {
                boolean hasPathCompleted = false;
                if (path.hasPath(requiresCompletedPath)) {
                    if (path.getKey().equals(requiresCompletedPath)) {
                        hasPathCompleted = !path.canProgress(checkProperties);
                    } else {
                        hasPathCompleted = true;
                    }
                }

                if (!hasPathCompleted) {
                    WandUpgradePath requiresPath = controller.getPath(requiresCompletedPath);
                    String pathName = requiresCompletedPath;
                    if (requiresPath != null) {
                        pathName = requiresPath.getName();
                    } else {
                        context.getLogger().warning("Invalid path specified in requirement: " + exactPath);
                    }

                    return getMessage(context, "no_path_end").replace("$path", pathName);
                }
            }
        }


        if (wandProperties != null) {
            String message = getRequiredProperty(context, wand, wandProperties);
            if (message != null) {
                return message;
            }
        }

        if (classProperties != null) {
            MageClass activeClass = mageClass == null ? mage.getActiveClass() : mage.getClass(mageClass);
            if (activeClass == null) {
                return getMessage(context, "no_path");
            }
            String message = getRequiredProperty(context, activeClass, classProperties);
            if (message != null) {
                return message;
            }
        }

        if (attributes != null) {
            for (PropertyRequirement requirement : attributes) {
                String key = requirement.key;
                Double value = mage.getAttribute(key);
                String message = checkRequiredProperty(context, requirement, key, value);
                if (message != null) {
                    return message;
                }
            }
        }

        return null;
    }

    @Nullable
    protected String getRequiredProperty(CastContext context, CasterProperties properties, List<PropertyRequirement> requirements) {
        for (PropertyRequirement requirement : requirements) {
            String key = requirement.key;
            Double value = properties.hasProperty(key) ? properties.getProperty(key, 0.0) : null;
            String messageKey = "wand." + key;
            String name = key;
            if (controller.getMessages().containsKey(messageKey)) {
                name = controller.getMessages().getLevelString(messageKey, value == null ? 0.0f : (float)(double)value);
            }
            String message = checkRequiredProperty(context, requirement, name, value);
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    @Nullable
    protected String checkRequiredProperty(CastContext context, PropertyRequirement requirement, String name, Double value) {
        if (requirement.value != null && (value == null || !value.equals(requirement.value))) {
            return getMessage(context, "property_requirement")
                .replace("$property", name).replace("$value", Double.toString(requirement.value));
        }
        if (requirement.min != null && (value == null || value <= requirement.min)) {
            return getMessage(context, "property_min")
                .replace("$property", name).replace("$value", Double.toString(requirement.min));
        }
        if (requirement.max != null && (value != null && value >= requirement.max)) {
            return getMessage(context, "property_max")
                .replace("$property", name).replace("$value", Double.toString(requirement.max));
        }

        return null;
    }

    protected boolean hasTags(Wand wand) {
        for (String checkTag : wandTags) {
            if (wand.hasTag(checkTag)) {
                return true;
            }
        }

        return false;
    }
}
