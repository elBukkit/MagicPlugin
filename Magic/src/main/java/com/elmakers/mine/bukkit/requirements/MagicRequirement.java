package com.elmakers.mine.bukkit.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.integration.ClientPlatform;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ModifierTemplate;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicRequirement {

    private @Nonnull final MageController controller;
    private @Nullable String permissionNode = null;
    private @Nullable String notPermissionNode = null;
    private @Nullable String requiredPath = null;
    private @Nullable String requiredTemplate = null;
    private @Nullable Set<String> requiredTemplates = null;
    private @Nullable List<String> requiredModifiers = null;
    private @Nullable List<String> wandTags = null;
    private @Nullable List<String> wandSlots = null;
    private boolean openSpellInventory = false;
    private @Nullable String requiresCompletedPath = null;
    private @Nullable String exactPath = null;
    private @Nullable List<String> mageClass = null;
    private @Nullable List<String> activeClass = null;
    private @Nullable List<PropertyRequirement> wandProperties = null;
    private @Nullable List<PropertyRequirement> classProperties = null;
    private @Nullable List<PropertyRequirement> attributes = null;
    private @Nullable List<PropertyRequirement> variables = null;
    private @Nullable Set<String> regionTags = null;
    private @Nullable RangedRequirement lightLevel = null;
    private @Nullable TimeRequirement timeOfDay = null;
    private @Nullable MoonRequirement moonPhase = null;
    private @Nullable RangedRequirement height = null;
    private @Nullable RangedRequirement serverVersion = null;
    private @Nullable CurrencyRequirement currency = null;
    private @Nullable ItemRequirement item = null;
    private @Nullable String weather = null;
    private @Nullable String castSpell = null;
    private @Nullable Set<String> worldNames = null;
    private @Nullable ClientPlatform clientPlatform = null;
    private String messageSection = "";
    private int castTimeout = 0;
    private boolean requireResourcePack = false;
    private boolean requireWand = false;
    private boolean ignoreMissing = false;
    private Boolean indoors;
    private Boolean stay;
    private Boolean owned;

    public MagicRequirement(@Nonnull MageController controller, @Nonnull Requirement requirement) {
        this.controller = controller;
        ConfigurationSection configuration = requirement.getConfiguration();

        messageSection = configuration.getString("message_section", "");
        if (!messageSection.isEmpty()) {
            messageSection = messageSection + ".";
        }
        permissionNode = configuration.getString("permission");
        notPermissionNode = configuration.getString("not_permission");
        requiredPath = configuration.getString("path");
        exactPath = configuration.getString("path_exact");
        requiresCompletedPath = configuration.getString("path_end");
        requiredTemplate = configuration.getString("wand");
        requireWand = configuration.getBoolean("holding_wand");
        requireResourcePack = configuration.getBoolean("resource_pack");
        mageClass = ConfigurationUtils.getStringList(configuration, "class");
        activeClass = ConfigurationUtils.getStringList(configuration, "active_class");
        wandTags = ConfigurationUtils.getStringList(configuration, "wand_tags");
        wandSlots = ConfigurationUtils.getStringList(configuration, "wand_slots");
        openSpellInventory = configuration.getBoolean("spell_inventory_open");
        ignoreMissing = configuration.getBoolean("ignore_missing", false);
        indoors = ConfigurationUtils.getOptionalBoolean(configuration,"indoors");
        stay = ConfigurationUtils.getOptionalBoolean(configuration,"stay");
        owned = ConfigurationUtils.getOptionalBoolean(configuration,"owned");
        if (activeClass != null && mageClass == null) {
            mageClass = activeClass;
        }
        List<String> regionTagList = ConfigurationUtils.getStringList(configuration, "region_tags");
        if (regionTagList != null && !regionTagList.isEmpty()) {
            regionTags = new HashSet<>(regionTagList);
        }

        if (configuration.contains("wands")) {
            requiredTemplates = new HashSet<>(ConfigurationUtils.getStringList(configuration, "wands"));
        }

        if (configuration.contains("modifiers")) {
            requiredModifiers = ConfigurationUtils.getStringList(configuration, "modifiers");
        }

        wandProperties = parsePropertyRequirements(configuration, "wand_properties", "wand_property", "property");
        classProperties = parsePropertyRequirements(configuration, "class_properties", "class_property", "property");
        attributes = parsePropertyRequirements(configuration, "attributes", "attribute", "attribute");
        variables = parsePropertyRequirements(configuration, "variables", "variable", "variable");

        lightLevel = parseRangedRequirement(configuration, "light");
        timeOfDay = parseTimeRequirement(configuration, "time");
        // For compatibility with spawners
        String moonKey = configuration.contains("moon") ? "moon" : "moon_phase";
        moonPhase = parseMoonRequirement(configuration, moonKey);
        weather = configuration.getString("weather");
        if (configuration.contains("worlds")) {
            worldNames = new HashSet<>(ConfigurationUtils.getStringList(configuration, "worlds"));
        }
        height = parseRangedRequirement(configuration, "height");
        serverVersion = parseRangedRequirement(configuration, "server_version");
        String defaultCurrencyType = configuration.getString("currency_type", "currency");
        currency = CurrencyRequirement.parse(configuration, "currency", defaultCurrencyType);
        item = ItemRequirement.parse(configuration, "item");

        castSpell = configuration.getString("cast_spell");
        castTimeout = configuration.getInt("cast_timeout", 0);

        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
            exactPath = requiresCompletedPath;
        }

        if (requiredTemplate != null || requiredTemplates != null || wandProperties != null || wandTags != null || wandSlots != null || openSpellInventory) {
            requireWand = true;
        }
        String clientPlatformKey = configuration.getString("client_platform");
        if (clientPlatformKey != null && !clientPlatformKey.isEmpty()) {
            try {
                clientPlatform = ClientPlatform.valueOf(clientPlatformKey.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid client_platform in requirement: " + clientPlatformKey);
            }
        }
    }

    @Nullable
    private RangedRequirement parseRangedRequirement(ConfigurationSection configuration, String key) {
        ConfigurationSection rangedConfig = ConfigurationUtils.getConfigurationSection(configuration, key);
        if (rangedConfig != null) {
            return new RangedRequirement(rangedConfig);
        }
        if (configuration.contains(key)) {
            return new RangedRequirement(configuration.getString(key));
        }
        return null;
    }

    @Nullable
    private TimeRequirement parseTimeRequirement(ConfigurationSection configuration, String key) {
        ConfigurationSection rangedConfig = ConfigurationUtils.getConfigurationSection(configuration, key);
        if (rangedConfig != null) {
            return new TimeRequirement(rangedConfig, controller.getLogger());
        }
        if (configuration.contains(key)) {
            return new TimeRequirement(configuration.getString(key), controller.getLogger());
        }
        return null;
    }

    @Nullable
    private MoonRequirement parseMoonRequirement(ConfigurationSection configuration, String key) {
        ConfigurationSection rangedConfig = ConfigurationUtils.getConfigurationSection(configuration, key);
        if (rangedConfig != null) {
            return new MoonRequirement(rangedConfig, controller.getLogger());
        }
        if (configuration.contains(key)) {
            return new MoonRequirement(configuration.getString(key), controller.getLogger());
        }
        return null;
    }

    @Nullable
    private List<PropertyRequirement> parsePropertyRequirements(ConfigurationSection configuration, String section, String singleSection, String type) {
        List<PropertyRequirement> requirements = null;
        if (configuration.isList(section)) {
            requirements = new ArrayList<>();
            Collection<ConfigurationSection> propertyConfigs = ConfigurationUtils.getNodeList(configuration, section);
            for (ConfigurationSection propertyConfig : propertyConfigs) {
                if (!propertyConfig.contains(type)) {
                    controller.getLogger().warning("Property requirement missing " + type + " parameter");
                    continue;
                }

                PropertyRequirement requirement = new PropertyRequirement(type, propertyConfig);
                requirements.add(requirement);
            }
        }
        Object singleConfigObject = configuration.get(singleSection);
        ConfigurationSection propertyConfig = null;
        if (singleConfigObject != null) {
            if (singleConfigObject instanceof Map) {
                propertyConfig = ConfigurationUtils.toConfigurationSection(configuration, (Map<?, ?>)singleConfigObject);
            } else if (singleConfigObject instanceof ConfigurationSection) {
                propertyConfig = (ConfigurationSection) singleConfigObject;
            }
        }
        if (propertyConfig != null) {
            if (!propertyConfig.contains(type)) {
                controller.getLogger().warning("Property requirement missing " + type + " parameter");
                return null;
            }
            if (requirements == null) {
                requirements = new ArrayList<>();
            }
            PropertyRequirement requirement = new PropertyRequirement(type, propertyConfig);
            requirements.add(requirement);
        }
        return requirements;
    }

    public boolean checkRequirement(@Nonnull MageContext context) {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();

        if (permissionNode != null && (player == null || !player.hasPermission(permissionNode))) {
            return false;
        }
        if (notPermissionNode != null && player != null && player.hasPermission(notPermissionNode)) {
            return false;
        }
        if (clientPlatform != null && mage.getClientPlatform() != clientPlatform) {
            return false;
        }
        if (serverVersion != null) {
            int[] versionPieces = CompatibilityLib.getServerVersion(controller.getPlugin());
            if (versionPieces.length < 2 || !serverVersion.check((double)versionPieces[1])) {
                return false;
            }
        }
        Wand wand = context.getWand();
        if (wand == null && requireWand) {
            return false;
        }
        if (requireResourcePack && !mage.hasResourcePack()) {
            return false;
        }

        Location location = mage.getLocation();
        if (indoors != null) {
            Block highest = location.getWorld().getHighestBlockAt(location.getBlockX(), location.getBlockZ());
            if (location.getBlockY() < highest.getY() != indoors) {
                return false;
            }
        }

        Entity entity = mage.getEntity();
        if (stay != null) {
            if (CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY) != stay) {
                return false;
            }
        }
        if (owned != null) {
            UUID ownerId = CompatibilityLib.getCompatibilityUtils().getOwnerId(entity);
            if ((ownerId != null) != owned) {
                return false;
            }
        }

        if (worldNames != null) {
            if (location == null || !worldNames.contains(location.getWorld().getName())) {
                return false;
            }
        }

        if (weather != null) {
            switch (weather) {
                case "storm":
                    if (location == null || !location.getWorld().hasStorm()) {
                        return false;
                    }
                    break;
                case "thunder":
                    if (location == null || !location.getWorld().isThundering()) {
                        return false;
                    }
                    break;
                case "clear":
                    if (location == null || location.getWorld().isThundering() || location.getWorld().hasStorm()) {
                        return false;
                    }
                    break;
                default:
                    context.getLogger().warning("Invalid weather specified in requirement " + weather + ", looking for clear/storm/thunder");
            }
        }
        if (timeOfDay != null) {
             if (location == null || !timeOfDay.check((double)location.getWorld().getTime())) {
                 return false;
             }
        }
        if (moonPhase != null) {
            int phase = (int)((location.getWorld().getFullTime() / 24000) % 8);
            if (location == null || !moonPhase.check((double)phase)) {
                return false;
            }
        }
        if (lightLevel != null) {
            if (location == null || !lightLevel.check((double)location.getBlock().getLightLevel())) {
                return false;
            }
        }
        if (height != null) {
            if (location == null || !height.check(location.getY())) {
                return false;
            }
        }
        if (currency != null) {
            if (location == null || !currency.check(mage)) {
                return false;
            }
        }
        if (item != null) {
            if (location == null || !item.check(mage)) {
                return false;
            }
        }
        if (castSpell != null && !castSpell.isEmpty()) {
            Spell spell = mage.getSpell(castSpell);
            if (spell == null) {
                return false;
            }
            long lastCast = spell.getLastCast();
            if (lastCast == 0) {
                return false;
            }
            if (castTimeout > 0 && System.currentTimeMillis() - lastCast > castTimeout) {
                return false;
            }
        }

        if (wandTags != null) {
            if (!hasTags(wand)) {
                return false;
            }
        }

        if (wandSlots != null) {
            if (!hasSlots(wand)) {
                return false;
            }
        }

        if (openSpellInventory) {
            if (!wand.isInventoryOpen()) {
                return false;
            }
        }

        if (regionTags != null && !controller.inTaggedRegion(location, regionTags)) {
            return false;
        }

        if (requiredTemplate != null) {
            String template = wand.getTemplateKey();
            if (template == null || !template.equals(requiredTemplate)) {
                return false;
            }
        }

        if (requiredTemplates != null && !requiredTemplates.isEmpty()) {
            String template = wand.getTemplateKey();
            if (template == null || !requiredTemplates.contains(template)) {
                return false;
            }
        }

        if (requiredModifiers != null) {
            boolean hasAny = false;
            for (String modifierKey : requiredModifiers) {
                if (mage.hasModifier(modifierKey)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                return false;
            }
        }

        CasterProperties checkProperties = context.getActiveProperties();
        ProgressionPath path = checkProperties.getPath();

        if (mageClass != null && !mageClass.isEmpty()) {
            boolean hasAny = false;
            for (String testClass : mageClass) {
                if (mage.hasClassUnlocked(testClass)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                return false;
            }
        }
        if (activeClass != null && !activeClass.isEmpty()) {
            MageClass currentClass = mage.getActiveClass();
            boolean hasAny = false;
            if (currentClass != null) {
                for (String testClass : activeClass) {
                    if (currentClass.getKey().equals(testClass)) {
                        hasAny = true;
                        break;
                    }
                }
            }
            if (!hasAny) {
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
            MageClass activeClass = mage.getActiveClass();
            if (mageClass != null) {
                for (String tryClass : mageClass) {
                    activeClass = mage.getClass(tryClass);
                    if (activeClass != null) break;
                }
            }
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
                Double value = context.getAttribute(key);
                if (!requirement.check(value)) {
                    return false;
                }
            }
        }

        if (variables != null) {
            for (PropertyRequirement requirement : variables) {
                String key = requirement.key;
                Double value = context.getVariable(key);
                if (!requirement.check(value)) {
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
            if (!requirement.check(value)) {
                return false;
            }
        }
        return true;
    }

    protected String getMessage(MageContext context, String key) {
        return context.getMessage(messageSection + key, getDefaultMessage(context, key));
    }

    protected String getDefaultMessage(MageContext context, String key) {
        return context.getController().getMessages().get("requirements." + key);
    }

    @Nullable
    public String getRequirementDescription(@Nonnull MageContext context) {
        Mage mage = context.getMage();
        MageController controller = mage.getController();
        Player player = mage.getPlayer();

        if (permissionNode != null && (player == null || !player.hasPermission(permissionNode))) {
            return context.getMessage(SpellResult.INSUFFICIENT_PERMISSION.name().toLowerCase());
        }
        if (notPermissionNode != null && player != null && player.hasPermission(notPermissionNode)) {
            return context.getMessage(SpellResult.INSUFFICIENT_PERMISSION.name().toLowerCase());
        }
        if (clientPlatform != null && mage.getClientPlatform() != clientPlatform) {
            return context.getMessage("no_client_platform");
        }
        if (serverVersion != null) {
            Double majorVersion = null;
            int[] versionPieces = CompatibilityLib.getServerVersion(controller.getPlugin());
            if (versionPieces.length > 1) {
                majorVersion = (double)versionPieces[1];
            }
            String message = checkRequiredProperty(context, serverVersion, getMessage(context, "server_version"), majorVersion);
            if (message != null) {
                return message;
            }
        }
        Wand wand = context.getWand();
        if (wand == null && requireWand) {
            return getMessage(context, "no_wand");
        }

        if (requireResourcePack && !mage.hasResourcePack()) {
            return getMessage(context, "no_resource_pack");
        }

        Location location = mage.getLocation();
        if (indoors != null) {
            Block highest = location.getWorld().getHighestBlockAt(location.getBlockX(), location.getBlockZ());
            if (location.getBlockY() < highest.getY() != indoors) {
                return getMessage(context, indoors ? "no_indoors" : "indoors");
            }
        }

        Entity entity = mage.getEntity();
        if (stay != null) {
            if (CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY) != stay) {
                return getMessage(context, stay ? "no_stay" : "stay");
            }
        }
        if (owned != null) {
            UUID ownerId = CompatibilityLib.getCompatibilityUtils().getOwnerId(entity);
            if ((ownerId != null) != owned) {
                return getMessage(context, owned ? "no_owned" : "owned");
            }
        }
        if (worldNames != null) {
            if (location == null || !worldNames.contains(location.getWorld().getName())) {
                return getMessage(context, "no_world");
            }
        }
        if (weather != null) {
            switch (weather) {
                case "storm":
                    if (location == null || !location.getWorld().hasStorm()) {
                        return getMessage(context, "no_weather");
                    }
                    break;
                case "thunder":
                    if (location == null || !location.getWorld().isThundering()) {
                        return getMessage(context, "no_weather");
                    }
                    break;
                case "clear":
                    if (location == null || location.getWorld().isThundering() || location.getWorld().hasStorm()) {
                        return getMessage(context, "no_weather");
                    }
                    break;
                default:
                    context.getLogger().warning("Invalid weather specified in requirement " + weather + ", looking for clear/storm/thunder");
            }
        }
        if (moonPhase != null) {
            int phase = (int)((location.getWorld().getFullTime() / 24000) % 8);
            String message = checkRequiredProperty(context, moonPhase, getMessage(context, "moon"), location == null ? null : (double)phase);
            if (message != null) {
                return message;
            }
        }
        if (timeOfDay != null) {
            String message = checkRequiredProperty(context, timeOfDay, getMessage(context, "time"), location == null ? null : (double)location.getWorld().getTime());
            if (message != null) {
                return message;
            }
        }
        if (lightLevel != null) {
            String message = checkRequiredProperty(context, lightLevel, getMessage(context, "light"), location == null ? null : (double)location.getBlock().getLightLevel());
            if (message != null) {
                return message;
            }
        }
        if (height != null) {
            String message = checkRequiredProperty(context, height, getMessage(context, "height"), location == null ? null : location.getY());
            if (message != null) {
                return message;
            }
        }
        if (currency != null) {
            String currencyMessage = getMessage(context, "currency");
            String currencyKey = currency.getKey();
            Currency currencyType = controller.getCurrency(currencyKey);
            currencyKey = currencyType == null ? "?" : currencyType.getName(controller.getMessages());
            currencyMessage = currencyMessage.replace("$currency", currencyKey);
            String message = checkRequiredProperty(context, currency, currencyMessage, mage.getCurrency(currencyKey));
            if (message != null) {
                return message;
            }
        }
        if (item != null) {
            String itemMessage = getMessage(context, "item");
            ItemStack itemStack = item.getItemStack(controller);
            String itemName = itemStack == null ? "?" : controller.describeItem(itemStack);
            itemMessage = itemMessage.replace("$item", itemName);
            Double count = itemStack == null ? null : (double)mage.getItemCount(itemStack);
            String message = checkRequiredProperty(context, item, itemMessage, count);
            if (message != null) {
                return message;
            }
        }
        if (castSpell != null) {
            String spellName = "Unknown";
            SpellTemplate template = controller.getSpellTemplate(castSpell);
            if (template != null) {
                spellName = template.getName();
            }
            Spell spell = mage.getSpell(castSpell);
            if (spell == null) {
                return getMessage(context, "no_cast").replace("$spell", spellName);
            }
            long lastCast = spell.getLastCast();
            if (lastCast == 0) {
                return getMessage(context, "no_cast").replace("$spell", spellName);
            }
            if (castTimeout > 0 && System.currentTimeMillis() - lastCast > castTimeout) {
                return getMessage(context, "no_cast").replace("$spell", spellName);
            }
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

        if (requiredModifiers != null && !requiredModifiers.isEmpty()) {
            boolean hasAny = false;
            for (String modifierKey : requiredModifiers) {
                if (mage.hasModifier(modifierKey)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                ModifierTemplate modifier = controller.getModifierTemplate(requiredModifiers.get(0));
                String name = modifier == null ? "?" : modifier.getName();
                return getMessage(context, "no_modifier").replace("$modifier", name);
            }
        }

        if (wandTags != null) {
            if (!hasTags(wand)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (wandSlots != null) {
            if (!hasSlots(wand)) {
                return getMessage(context, "no_template").replace("$wand", wand.getName());
            }
        }

        if (openSpellInventory) {
            if (!wand.isInventoryOpen()) {
                return getMessage(context, "no_spell_inventory").replace("$wand", wand.getName());
            }
        }

        if (regionTags != null && !controller.inTaggedRegion(location, regionTags)) {
            return getMessage(context, "no_region");
        }

        if (mageClass != null && !mageClass.isEmpty()) {
            boolean hasAny = false;
            for (String testClass : mageClass) {
                if (mage.hasClassUnlocked(testClass)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                return getMessage(context, "no_class").replace("$class", mageClass.get(0));
            }
        }

        if (activeClass != null && !activeClass.isEmpty()) {
            MageClass currentClass = mage.getActiveClass();
            boolean hasAny = false;
            if (currentClass != null) {
                for (String testClass : activeClass) {
                    if (currentClass.getKey().equals(testClass)) {
                        hasAny = true;
                        break;
                    }
                }
            }
            if (!hasAny) {
                return getMessage(context, "no_class").replace("$class", activeClass.get(0));
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
                } else if (!ignoreMissing) {
                    context.getLogger().warning("Invalid path specified in requirement " + requiredPath);
                }
                return getMessage(context, "no_required_path").replace("$path", pathName);
            }
            if (exactPath != null && !exactPath.equals(path.getKey())) {
                WandUpgradePath requiresPath = controller.getPath(exactPath);
                String pathName = exactPath;
                if (requiresPath != null) {
                    pathName = requiresPath.getName();
                } else if (!ignoreMissing) {
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
                    } else if (!ignoreMissing) {
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
            MageClass activeClass = mage.getActiveClass();
            if (mageClass != null) {
                for (String tryClass : mageClass) {
                    activeClass = mage.getClass(tryClass);
                    if (activeClass != null) break;
                }
            }
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
                Double value = context.getAttribute(key);
                String message = checkRequiredProperty(context, requirement, key, value);
                if (message != null) {
                    return message;
                }
            }
        }

        if (variables != null) {
            for (PropertyRequirement requirement : variables) {
                String key = requirement.key;
                Double value = context.getVariable(key);
                String message = checkRequiredProperty(context, requirement, key, value);
                if (message != null) {
                    return message;
                }
            }
        }

        return null;
    }

    @Nullable
    protected String getRequiredProperty(MageContext context, CasterProperties properties, List<PropertyRequirement> requirements) {
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
    protected String checkRequiredProperty(MageContext context, RangedRequirement requirement, String name, Double value) {
        if (requirement.value != null && (value == null || !value.equals(requirement.value))) {
            return getMessage(context, "property_requirement")
                .replace("$property", name)
                .replace("$value", Double.toString(requirement.value)
                .replace("@value", Integer.toString((int)(double)requirement.value)));
        }
        if (requirement.min != null) {
            if (requirement.inclusive) {
                if (value == null || value < requirement.min) {
                    return getMessage(context, "property_min_inclusive")
                        .replace("$property", name).replace("$value", Double.toString(requirement.min))
                        .replace("@value", Integer.toString((int)(double)requirement.min));
                }
            } else {
                 if (value == null || value <= requirement.min) {
                     return getMessage(context, "property_min")
                         .replace("$property", name)
                         .replace("$value", Double.toString(requirement.min))
                         .replace("@value", Integer.toString((int)(double)requirement.min));
                 }
            }
        }
        if (requirement.max != null) {
            if (requirement.inclusive) {
                if (value != null && value > requirement.max) {
                    return getMessage(context, "property_max_inclusve")
                        .replace("$property", name)
                        .replace("$value", Double.toString(requirement.max))
                        .replace("value", Integer.toString((int)(double)requirement.max));
                }
            } else {
                if (value != null && value >= requirement.max) {
                    return getMessage(context, "property_max")
                        .replace("$property", name)
                        .replace("$value", Double.toString(requirement.max))
                        .replace("@value", Integer.toString((int)(double)requirement.max));
                }
            }
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

    protected boolean hasSlots(Wand wand) {
        for (String checkSlot : wandSlots) {
            if (wand.hasSlot(checkSlot)) {
                return true;
            }
        }

        return false;
    }
}
