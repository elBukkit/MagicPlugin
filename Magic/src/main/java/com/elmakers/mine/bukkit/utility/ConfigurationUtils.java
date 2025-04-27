package com.elmakers.mine.bukkit.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.configuration.MageParameters;
import com.elmakers.mine.bukkit.configuration.MagicConfiguration;
import com.elmakers.mine.bukkit.configuration.ParameterizedConfigurationSection;
import com.elmakers.mine.bukkit.configuration.SpellParameters;
import com.elmakers.mine.bukkit.configuration.TranslatingConfiguration;
import com.elmakers.mine.bukkit.configuration.TranslatingConfigurationSection;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import com.elmakers.mine.bukkit.magic.MagicController;

public class ConfigurationUtils extends ConfigUtils {
    private static MagicController controller;

    public static void setMagicController(MagicController controller) {
        ConfigurationUtils.controller = controller;
    }

    @Nullable
    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path) {
        return getMaterialAndData(node, path, null);
    }

    @Nullable
    public static MaterialAndData getMaterialAndData(ConfigurationSection node, String path, MaterialAndData def) {
        String stringData = node.getString(path);
        if (stringData == null) {
            return def;
        }

        return toMaterialAndData(stringData);
    }

    @Nullable
    public static MaterialAndData getIconMaterialAndData(ConfigurationSection node, String path, boolean legacy, MaterialAndData def) {
        if (legacy) {
            path = "legacy_" + path;
        }
        String stringData = node.getString(path);
        if (stringData == null) {
            return def;
        }

        return toMaterialAndData(stringData);
    }

    @Nullable
    public static MaterialAndData toMaterialAndData(Object o) {
        if (o instanceof MaterialAndData) {
            return (MaterialAndData)o;
        }
        if (o instanceof String) {
            String matName = (String)o;
            return new MaterialAndData(matName);
        }

        return null;
    }

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag) {
        return CompatibilityLib.getCompatibilityUtils().loadAllTagsFromNBT(tags, tag);
    }

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, ItemStack item) {
        if (item == null) {
            return false;
        }
        Object tag = CompatibilityLib.getItemUtils().getTag(item);
        if (tag == null) return false;

        return loadAllTagsFromNBT(tags, tag);
    }

    @SuppressWarnings("unchecked")
    protected void combine(Map<Object, Object> to, Map<? extends Object, Object> from) {
         for (Entry<?, Object> entry : from.entrySet()) {
             Object value = entry.getValue();
             Object key = entry.getKey();
             if (value instanceof Map && to.containsKey(key)) {
                 Object toValue = to.get(key);
                 if (toValue instanceof Map) {
                     combine((Map<Object, Object>)toValue, (Map<Object, Object>)value);
                     continue;
                 }
             }
             to.put(key, value);
         }
     }

    @Nonnull
    public static ConfigurationSection cloneEmptyConfiguration(ConfigurationSection section)
    {
        if (section instanceof SpellParameters) {
            return new SpellParameters((SpellParameters)section);
        }
        if (section instanceof MageParameters) {
            return new MageParameters((MageParameters)section);
        }
        if (section instanceof MagicConfiguration) {
            return new MagicConfiguration((MagicConfiguration)section);
        }
        if (section instanceof ParameterizedConfigurationSection) {
            return new ParameterizedConfigurationSection(section);
        }
        if (section instanceof TranslatingConfiguration) {
            return new TranslatingConfiguration();
        }
        if (section instanceof TranslatingConfigurationSection) {
            return new TranslatingConfigurationSection(section);
        }
        return ConfigurationUtils.newConfigurationSection();
    }

    @Nonnull
    public static ConfigurationSection cloneConfiguration(ConfigurationSection section)
    {
        ConfigurationSection copy = cloneEmptyConfiguration(section);
        return addConfigurations(copy, section);
    }

    @Nullable
    public static ConfigurationSection replaceParameters(ConfigurationSection configuration, ConfigurationSection parameters) {
        ConfigurationSection replaced = tryReplaceParameters(configuration, parameters);
        return replaced == null ? configuration : replaced;
    }

    @Nullable
    private static ConfigurationSection tryReplaceParameters(ConfigurationSection configuration, ConfigurationSection parameters) {
        if (configuration == null) return null;

        ConfigurationSection replaced = null;
        Set<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            Object value = configuration.get(key);
            Object replacement = tryReplaceParameters(value, parameters);
            if (replacement != null) {
                if (replaced == null) {
                    replaced = cloneConfiguration(configuration);
                }
                replaced.set(key, replacement);
            }
        }
        return replaced;
    }

    private static Map<String, Object> tryReplaceParameters(Map<String, Object> configuration, ConfigurationSection parameters)
    {
        if (configuration == null || configuration.isEmpty()) return null;

        Map<String, Object> replaced = null;
        for (Map.Entry<String, Object> entry : configuration.entrySet()) {
            Object entryValue = entry.getValue();
            Object replacement = tryReplaceParameters(entryValue, parameters);
            if (replacement != null) {
                if (replaced == null) {
                    replaced = new HashMap<>(configuration);
                }
                replaced.put(entry.getKey(), replacement);
            }
        }
        return replaced;
    }

    @Nullable
    private static Object tryReplaceParameters(Object value, ConfigurationSection parameters) {
        if (value == null) return null;
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)value;
            value = tryReplaceParameters(map, parameters);
        } else if (value instanceof ConfigurationSection) {
            value = tryReplaceParameters((ConfigurationSection)value, parameters);
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)value;
            value = tryReplaceParameters(list, parameters);
        } else if (value instanceof String) {
            value = tryReplaceParameter((String)value, parameters);
        }

        return value;
    }

    private static List<Object> tryReplaceParameters(List<Object> configurations, ConfigurationSection parameters)
    {
        if (configurations == null || configurations.size() == 0) return null;

        List<Object> replaced = null;
        for (int i = 0; i < configurations.size(); i++) {
            Object value = configurations.get(i);
            Object replacement = tryReplaceParameters(value, parameters);
            if (replacement != null) {
                if (replaced == null) {
                    replaced = new ArrayList<>(configurations);
                }
                replaced.set(i, replacement);
            }
        }
        return replaced;
    }

    private static Object tryReplaceParameter(String value, ConfigurationSection parameters)
    {
        if (value.length() < 2 || value.charAt(0) != '$') return null;
        return parameters.get(value.substring(1));
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        return addConfigurations(first, second, true);
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second, boolean override) {
        return addConfigurations(first, second, override, false, false);
    }

    public static ConfigurationSection addConfigurations(ConfigurationSection first, ConfigurationSection second, boolean override, boolean requireExisting) {
        return addConfigurations(first, second, override, requireExisting, false);
    }

    @Nonnull
    public static ConfigurationSection addConfigurations(
            @Nonnull ConfigurationSection first,
            @Nullable ConfigurationSection second,
            boolean override, boolean requireExisting, boolean isUserConfig) {
        if (second == null) {
            return first;
        }

        override = override || second.getBoolean("override");
        Map<String, Object> map = ConfigurationUtils.toMap(second);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            String key = entry.getKey();

            Object existingValue = first.get(key);
            if (existingValue == null && requireExisting) continue;

            if (value instanceof Map)
            {
                value = getConfigurationSection(second, key);
            }
            if (existingValue instanceof Map)
            {
                existingValue = getConfigurationSection(first, key);
            }
            if (value instanceof ConfigurationSection && (existingValue == null || existingValue instanceof ConfigurationSection)) {
                ConfigurationSection addChild = (ConfigurationSection)value;
                boolean skipMerge = isUserConfig ? addChild.contains("inherit") : addChild.isBoolean("inherit") && !addChild.getBoolean("inherit", true);
                if (existingValue == null || skipMerge) {
                    ConfigurationSection newChild = first.createSection(key);
                    addConfigurations(newChild, addChild, override);
                } else {
                    addConfigurations((ConfigurationSection)existingValue, addChild, override);
                }
            } else if (override || existingValue == null) {
                first.set(key, value);
            }
        }

        return first;
    }

    @Nonnull
    public static ConfigurationSection addNumericConfigurations(
            @Nonnull ConfigurationSection first,
            @Nullable ConfigurationSection second,
            boolean override) {
        if (second == null) {
            return first;
        }

        Set<String> keys = second.getKeys(true);
        for (String key : keys) {
            double value = second.getDouble(key);

            Object existingValue = first.get(key);
            if (existingValue != null && !override) continue;
            first.set(key, value);
        }

        return first;
    }


    @Nonnull
    public static ConfigurationSection overlayNumericConfigurations(
            @Nonnull ConfigurationSection first,
            @Nullable ConfigurationSection second) {
        return addNumericConfigurations(first, second, false);
    }


    public static ConfigurationSection replaceConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        if (second == null) return first;
        Map<String, Object> map = ConfigurationUtils.toMap(second);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            first.set(entry.getKey(), entry.getValue());
        }

        return first;
    }

    public static ConfigurationSection overlayConfigurations(ConfigurationSection first, ConfigurationSection second)
    {
        // This used to avoid combining sections but I can't remember why. Look at this more if something gets weird
        return addConfigurations(first, second, false);
    }

    public static ConfigurationSection mergeConfigurations(ConfigurationSection...configs) {
        ConfigurationSection merged = null;
        for (ConfigurationSection config : configs) {
            if (config == null) continue;
            if (merged == null) {
                merged = cloneConfiguration(config);
            } else {
                ConfigurationUtils.overlayConfigurations(merged, config);
            }
        }
        return merged;
    }

    public static void mergeText(ConfigurationSection first, ConfigurationSection second) {
        for (String key : second.getKeys(true)) {
            // Skip over sections
            if (!second.isString(key)) continue;
            String value = second.getString(key);
            String firstValue = first.getString(key);
            if (firstValue != null && !firstValue.isEmpty()) {
                value = firstValue + "\n" + value;
            }
            first.set(key, value);
        }
    }

    public static void addParameters(String[] extraParameters, ConfigurationSection parameters)
    {
        if (extraParameters != null)
        {
            for (int i = 0; i < extraParameters.length - 1; i += 2)
            {
                if (extraParameters[i] == null || extraParameters[i].isEmpty()) continue;
                set(parameters, extraParameters[i], extraParameters[i + 1]);
            }
        }
    }

    public static String getParameters(ConfigurationSection parameters) {
        Collection<String> parameterStrings = new ArrayList<>();
        Map<String, Object> map = ConfigurationUtils.toMap(parameters);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            parameterStrings.add(entry.getKey());
            parameterStrings.add(entry.getValue().toString());
        }
        return StringUtils.join(parameterStrings, ' ');
    }

    public static SoundEffect toSoundEffect(String soundConfig) {
        return new SoundEffect(soundConfig);
    }

    @Nullable
    public static Particle toParticleEffect(String effectParticleName) {
        Particle effectParticle = null;
        if (effectParticleName.length() > 0) {
            String particleName = effectParticleName.toUpperCase();
            try {
                effectParticle = Particle.valueOf(particleName);
            } catch (Exception ignored) {
            }
        }

        return effectParticle;
    }

    public static Collection<String> getKeysOrList(@Nonnull ConfigurationSection node, @Nonnull String key) {
        Collection<String> values = null;
        if (node.isString(key)) {
            values = ConfigurationUtils.getStringList(node, key);
        } else if (node.isConfigurationSection(key)) {
            ConfigurationSection spellSection = node.getConfigurationSection(key);
            if (spellSection != null) {
                values = spellSection.getKeys(false);
            }
        }
        if (values == null) {
            values = new ArrayList<>(0);
        }
        return values;
    }


    public static Collection<PrerequisiteSpell> getPrerequisiteSpells(MageController controller, ConfigurationSection node, String key, String loadContext, boolean removeMissing) {
        if (node == null || key == null) {
            return new ArrayList<>(0);
        }

        Collection<?> spells = null;
        if (node.isString(key)) {
            spells = ConfigurationUtils.getStringList(node, key);
        } else if (node.isConfigurationSection(key)) {
            ConfigurationSection spellSection = node.getConfigurationSection(key);
            if (spellSection != null) {
                spells = spellSection.getKeys(false);
            }
        } else {
            spells = node.getList(key);
        }
        if (spells == null) {
            spells = new ArrayList<>(0);
        }

        List<PrerequisiteSpell> requiredSpells = new ArrayList<>(spells.size());
        for (Object o : spells) {
            PrerequisiteSpell prerequisiteSpell = null;
            if (o instanceof String) {
                prerequisiteSpell = new PrerequisiteSpell(new SpellKey((String) o), 0);
            } else if (o instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) o;
                String spell = section.getString("spell");
                long progressLevel = section.getLong("progress_level");
                prerequisiteSpell = new PrerequisiteSpell(new SpellKey(spell), progressLevel);
            } else if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                String spell = map.get("spell").toString();
                String progressLevelString = map.containsKey("progress_level") ? map.get("progress_level").toString() : "0";
                if (spell != null && StringUtils.isNumeric(progressLevelString)) {
                    long progressLevel = 0;
                    try {
                        progressLevel = Long.parseLong(progressLevelString);
                    } catch (NumberFormatException ignore) { }
                    prerequisiteSpell = new PrerequisiteSpell(new SpellKey(spell), progressLevel);
                }
            }

            if (prerequisiteSpell != null) {
                if (controller.getSpellTemplate(prerequisiteSpell.getSpellKey().getKey()) != null) {
                    requiredSpells.add(prerequisiteSpell);
                } else {
                    if (!removeMissing) {
                        requiredSpells.add(prerequisiteSpell);
                        controller.getLogger().warning("Unknown or disabled spell requirement " + prerequisiteSpell.getSpellKey().getKey() + " in " + loadContext + ", upgrade will be disabled");
                    } else {
                        controller.info("Unknown or disabled spell prerequisite " + prerequisiteSpell.getSpellKey().getKey() + " in " + loadContext + ", ignoring", 5);
                    }
                }
            }
        }

        return requiredSpells;
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parentConfig, String effectKey, int defaultDuration) {
        if (parentConfig.isConfigurationSection(effectKey)) {
            return getPotionEffects(parentConfig.getConfigurationSection(effectKey), null);
        }

        List<PotionEffect> effects = null;
        if (parentConfig.isList(effectKey)) {
            effects = new ArrayList<>();
            List<String> effectList = parentConfig.getStringList(effectKey);
            for (String value : effectList) {
                String[] pieces = StringUtils.split(value, "@");
                String key = pieces[0];
                try {
                    PotionEffectType effectType = PotionEffectType.getByName(key.toUpperCase());

                    int power = 1;
                    if (pieces.length > 1) {
                        power = (int)Float.parseFloat(pieces[1]);
                    }

                    PotionEffect effect = new PotionEffect(effectType, defaultDuration, power, true, true);
                    effects.add(effect);

                } catch (Exception ex) {
                    Bukkit.getLogger().warning("Error parsing potion effect for " + key + ": " + value);
                }
            }
        }
        return effects;
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig) {
        return getPotionEffects(effectConfig, null);
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig, Integer duration) {
        return getPotionEffects(effectConfig, duration, true, true);
    }

    @Nullable
    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection effectConfig, Integer duration, boolean ambient, boolean particles) {
        if (effectConfig == null) return null;
        List<PotionEffect> effects = new ArrayList<>();
        Set<String> keys = effectConfig.getKeys(false);
        if (keys.isEmpty()) return null;

        for (String key : keys) {
            String value = effectConfig.getString(key);
            try {
                PotionEffectType effectType = PotionEffectType.getByName(key.toUpperCase());

                int ticks = 10;
                int power = 1;
                if (value.contains(",")) {
                    String[] pieces = StringUtils.split(value, ',');
                    ticks = (int)Float.parseFloat(pieces[0]);
                    power = (int)Float.parseFloat(pieces[1]);
                } else {
                    power = (int)Float.parseFloat(value);
                    if (duration != null) {
                        ticks = duration / 50;
                    }
                }

                PotionEffect effect = new PotionEffect(effectType, ticks, power, ambient, particles);
                effects.add(effect);

            } catch (Exception ex) {
                Bukkit.getLogger().warning("Error parsing potion effect for " + key + ": " + value);
            }
        }
        return effects;
    }

    public static ConfigurationSection subtractConfiguration(ConfigurationSection base, ConfigurationSection subtract) {
         Set<String> keys = subtract.getKeys(false);
         for (String key : keys) {
             Object baseObject = base.get(key);
             if (baseObject == null) continue;
             Object subtractObject = subtract.get(key);
             if (subtractObject == null) continue;
             if (subtractObject instanceof ConfigurationSection && baseObject instanceof ConfigurationSection) {
                 ConfigurationSection baseConfig = (ConfigurationSection)baseObject;
                 ConfigurationSection subtractConfig = (ConfigurationSection)subtractObject;
                 baseConfig = subtractConfiguration(baseConfig, subtractConfig);
                 if (!baseConfig.getKeys(false).isEmpty()) continue;
             } else if (!subtractObject.equals(baseObject)) continue;
             base.set(key, null);
         }

         return base;
    }

    public static VariableScope parseScope(String scopeString, VariableScope defaultScope, Logger logger) {
        VariableScope scope = defaultScope;
        if (scopeString != null && !scopeString.isEmpty()) {
            try {
                scope = VariableScope.valueOf(scopeString.toUpperCase());
            } catch (Exception ex) {
                logger.warning("Invalid variable scope: " + scopeString);
            }
        }
        return scope;
    }

    @Nullable
    public static Object convertProperty(@Nullable Object value) {
        if (value == null) return value;
        Object result = value;
        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            result = Boolean.valueOf(isTrue);
        } else {
            try {
                result = Integer.valueOf(value instanceof Integer ? ((Integer)value).intValue() : Integer.parseInt(value.toString()));
            } catch (Exception notAnInteger) {
                try {
                    result = Double.valueOf(value instanceof Double ? ((Double)value).doubleValue() : (value instanceof Float ? (double)((Float)value).floatValue() : Double.parseDouble(value.toString())));
                } catch (Exception ignored) {
                }
            }
        }

        return result;
    }

    public static String getIcon(ConfigurationSection node, boolean legacy) {
         return getIcon(node, legacy, "icon");
    }

    public static String getIcon(ConfigurationSection node, boolean legacy, String iconKey) {
        if (legacy) {
            return node.getString("legacy_" + iconKey, node.getString(iconKey));
        }
        return node.getString(iconKey);
    }

    public static boolean isEnabled(ConfigurationSection configuration) {
         if (configuration == null) return false;
         if (!configuration.getBoolean("enabled", true)) return false;
         String required = configuration.getString("requires");
         if (required != null && !required.isEmpty()) {
             if (Bukkit.getPluginManager().getPlugin(required) == null) {
                 return false;
             }
         }
         return true;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static ConfigurationSection newConfigurationSection(String logContext) {
        if (controller != null) {
            return new MagicConfiguration(controller, logContext);
        }
        return new TranslatingConfiguration();
    }

    public static ConfigurationSection newConfigurationSection() {
         return newConfigurationSection("Anonymous");
    }

    @Nullable
    public static Set<Biome> loadBiomes(List<String> biomeNames, Logger logger, String logContext) {
        if (biomeNames == null || biomeNames.isEmpty()) return null;
        Set<Biome> set = new HashSet<>();
        for (String biomeName : biomeNames) {
            try {
                Biome biome = Biome.valueOf(biomeName.trim().toUpperCase());
                set.add(biome);
            } catch (Exception ignore) {
            }
        }
        return set;
    }

    public static ConfigurationSection addSection(ConfigurationSection parent, String path, Map<?, ?> nodeMap) {
         ConfigurationSection newSection = toConfigurationSection(parent, path, nodeMap);
         parent.set(path, newSection);
         return newSection;
    }

    @Nullable
    public static Collection<Requirement> getRequirements(ConfigurationSection configuration) {
        List<Requirement> requirements = null;
        Collection<ConfigurationSection> requirementConfigurations = getNodeList(configuration, "requirements");
        if (requirementConfigurations != null) {
            requirements = new ArrayList<>();
            for (ConfigurationSection requirementConfiguration : requirementConfigurations) {
                requirements.add(new Requirement(requirementConfiguration));
            }
        }
        ConfigurationSection singleConfiguration = getConfigurationSection(configuration, "requirement");
        if (singleConfiguration != null) {
            if (requirements == null) {
                requirements = new ArrayList<>();
            }
            requirements.add(new Requirement(singleConfiguration));
        }
        return requirements;
    }

    @Nullable
    public static Material toMaterial(Object o) {
        if (o instanceof Material) {
            return (Material)o;
        }
        if (o instanceof String) {
            String matName = (String)o;
            return Material.getMaterial(matName.toUpperCase());
        }

        return null;
    }

    @Nullable
    public static Material getMaterial(ConfigurationSection node, String path, Material def) {
        String stringData = node.getString(path);
        if (stringData == null || stringData.isEmpty()) {
            return def;
        }

        return toMaterial(stringData);
    }

    @Nullable
    public static Material getMaterial(ConfigurationSection node, String path) {
        return getMaterial(node, path, null);
    }

    public static String fromMaterial(Material material) {
        if (material == null) return "";
        return material.name().toLowerCase();
    }

    @Nullable
    public static Integer parseTime(String timeString, Logger log, String logContext) {
        Integer time = null;
        if (timeString != null) {
            if (timeString.equalsIgnoreCase("day")) {
                time = 0;
            } else if (timeString.equalsIgnoreCase("night")) {
                time = 13000;
            } else if (timeString.equalsIgnoreCase("dusk") || timeString.equalsIgnoreCase("sunset")) {
                time = 12000;
            } else if (timeString.equalsIgnoreCase("dawn") || timeString.equalsIgnoreCase("sunrise")) {
                time = 23000;
            } else if (timeString.equalsIgnoreCase("noon") || timeString.equalsIgnoreCase("midday")) {
                time = 6000;
            } else if (timeString.equalsIgnoreCase("midnight")) {
                time = 18000;
            } else {
                try {
                    time = Integer.parseInt(timeString);
                } catch (Exception ex) {
                    log.warning("Invalid time in " + logContext + ": " + timeString);
                }
            }
        }
        return time;
    }

    @Nullable
    public static Integer parseMoonPhase(String phaseString, Logger log, String logContext) {
        Integer phase = null;
        if (phaseString != null) {
            if (phaseString.equalsIgnoreCase("new")) {
                phase = 4;
            } else if (phaseString.equalsIgnoreCase("full")) {
                phase = 0;
            } else {
                try {
                    phase = Integer.parseInt(phaseString);
                } catch (Exception ex) {
                    log.warning("Invalid phase of moon in " + logContext + " config: " + phaseString);
                }
            }
        }

        return phase;
    }

    public static void parseDisguiseTarget(ConfigurationSection parameters, CastContext context) {
        String disguiseTarget = parameters.getString("disguise_target");
        if (disguiseTarget != null) {
            Entity targetEntity = disguiseTarget.equals("target") ? context.getTargetEntity() : context.getEntity();
            if (targetEntity != null) {
                ConfigurationSection disguiseConfig = parameters.createSection("disguise");
                disguiseConfig.set("type", targetEntity.getType().name().toLowerCase());
                if (targetEntity instanceof Player) {
                    MageController controller = context.getController();
                    Player targetPlayer = (Player)targetEntity;
                    disguiseConfig.set("name", targetPlayer.getName());
                    disguiseConfig.set("skin", targetPlayer.getName());
                    PlayerInventory inventory = targetPlayer.getInventory();
                    ItemStack helmet = inventory.getHelmet();
                    if (!CompatibilityLib.getItemUtils().isEmpty(helmet)) {
                        disguiseConfig.set("helmet", controller.getItemKey(helmet));
                    }
                    ItemStack chestplate = inventory.getChestplate();
                    if (!CompatibilityLib.getItemUtils().isEmpty(chestplate)) {
                        disguiseConfig.set("chestplate", controller.getItemKey(chestplate));
                    }
                    ItemStack leggings = inventory.getLeggings();
                    if (!CompatibilityLib.getItemUtils().isEmpty(leggings)) {
                        disguiseConfig.set("leggings", controller.getItemKey(leggings));
                    }
                    ItemStack boots = inventory.getBoots();
                    if (!CompatibilityLib.getItemUtils().isEmpty(boots)) {
                        disguiseConfig.set("boots", controller.getItemKey(boots));
                    }
                    ItemStack mainhand = inventory.getItemInMainHand();
                    if (!CompatibilityLib.getItemUtils().isEmpty(mainhand)) {
                        disguiseConfig.set("mainhand", controller.getItemKey(mainhand));
                    }
                    ItemStack offhand = inventory.getItemInOffHand();
                    if (!CompatibilityLib.getItemUtils().isEmpty(offhand)) {
                        disguiseConfig.set("offhand", controller.getItemKey(offhand));
                    }
                }
            }
        }
    }
}
