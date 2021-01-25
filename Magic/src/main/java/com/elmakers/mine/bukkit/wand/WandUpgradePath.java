package com.elmakers.mine.bukkit.wand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.event.PathUpgradeEvent;
import com.elmakers.mine.bukkit.api.event.WandUpgradeEvent;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

/**
 * A represents a randomized upgrade path that a wand may use
 * when upgrading.
 *
 * <p>Upgrading is generally done by spending XP on an enchanting table.
 */
public class WandUpgradePath implements com.elmakers.mine.bukkit.api.wand.WandUpgradePath {
    private static Map<String, WandUpgradePath> paths = new HashMap<>();

    private TreeMap<Integer, WandLevel> levelMap = null;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<>();
    private List<String> upgradeCommands;
    private String upgradeBroadcast;
    private int[] levels = null;
    private final String key;
    private final WandUpgradePath parent;
    private final Set<String> spells = new HashSet<>();
    private final Set<String> brushes = new HashSet<>();
    private final Set<String> extraSpells = new HashSet<>();
    private Collection<PrerequisiteSpell> requiredSpells = new HashSet<>();
    private Set<String> requiredSpellKeys = new HashSet<>();
    private final Set<String> allSpells = new HashSet<>();
    private final Set<String> allExtraSpells = new HashSet<>();
    private final Set<String> allRequiredSpells = new HashSet<>();
    private final Set<String> allBrushes = new HashSet<>();
    private String upgradeKey;
    private String upgradeItemKey;
    private String name;
    private String description;
    private String followsPath;
    private Set<String> tags;
    private boolean hidden = false;
    private boolean earnsSP = true;
    private MaterialAndData icon;
    private MaterialAndData migrateIcon;

    private boolean matchSpellMana = true;

    private int maxUses = 500;
    private int maxMaxMana = 0;
    private int maxManaRegeneration = 0;
    private int maxMana = 0;
    private int manaRegeneration = 0;

    private Map<String, Double> maxProperties = new HashMap<>();
    private static Set<String> resolvingKeys = new LinkedHashSet<>();

    private int minLevel = 1;
    private int maxLevel = 1;

    private float bonusLevelMultiplier = 0.5f;

    public WandUpgradePath(MageController controller, String key, WandUpgradePath inherit, ConfigurationSection template)
    {
        this.parent = inherit;
        this.key = key;
        this.levels = inherit.levels;
        this.maxMaxMana = inherit.maxMaxMana;
        this.maxManaRegeneration = inherit.maxManaRegeneration;
        this.maxProperties.putAll(inherit.maxProperties);
        this.minLevel = inherit.minLevel;
        this.maxLevel = inherit.maxLevel;
        this.matchSpellMana = inherit.matchSpellMana;
        this.earnsSP = inherit.earnsSP;
        this.levelMap = new TreeMap<>(inherit.levelMap);
        this.icon = inherit.icon;
        this.migrateIcon = inherit.migrateIcon;
        this.maxMana = inherit.maxMana;
        this.manaRegeneration = inherit.manaRegeneration;
        this.upgradeBroadcast = inherit.upgradeBroadcast;
        effects.putAll(inherit.effects);
        allRequiredSpells.addAll(inherit.allRequiredSpells);
        allSpells.addAll(inherit.allSpells);
        allExtraSpells.addAll(inherit.allExtraSpells);
        allBrushes.addAll(inherit.allBrushes);

        if (inherit.tags != null && !inherit.tags.isEmpty())
        {
            this.tags = new HashSet<>(inherit.tags);
        }

        load(controller, key, template);

        if ((this.upgradeCommands == null || this.upgradeCommands.size() == 0) && inherit.upgradeCommands != null)
        {
            this.upgradeCommands = new ArrayList<>();
            this.upgradeCommands.addAll(inherit.upgradeCommands);
        }
    }

    public WandUpgradePath(MageController controller, String key, ConfigurationSection template) {
        this.key = key;
        this.parent = null;
        this.tags = null;
        load(controller, key, template);
    }

    protected void load(MageController controller, String key, ConfigurationSection template) {
        // Cache spells, mainly used for spellbooks
        Collection<PrerequisiteSpell> pathSpells = ConfigurationUtils.getPrerequisiteSpells(controller, template, "spells", "path " + key, true);
        for (PrerequisiteSpell prereq : pathSpells) {
            spells.add(prereq.getSpellKey().getKey());
        }
        allSpells.addAll(spells);
        Collection<PrerequisiteSpell> pathExtraSpells = ConfigurationUtils.getPrerequisiteSpells(controller, template, "extra_spells", "path " + key, true);
        for (PrerequisiteSpell prereq : pathExtraSpells) {
            extraSpells.add(prereq.getSpellKey().getKey());
        }
        allExtraSpells.addAll(extraSpells);

        // Get brush info
        brushes.addAll(ConfigurationUtils.getKeysOrList(template, "brushes"));
        allBrushes.addAll(brushes);

        // Upgrade information
        followsPath = template.getString("follows");
        upgradeKey = template.getString("upgrade");
        upgradeItemKey = template.getString("upgrade_item");

        Collection<PrerequisiteSpell> prerequisiteSpells = ConfigurationUtils.getPrerequisiteSpells(controller, template, "required_spells", "path " + key, false);
        this.requiredSpells = new ArrayList<>(pathSpells.size() + prerequisiteSpells.size());
        requiredSpells.addAll(pathSpells);
        requiredSpells.addAll(prerequisiteSpells);

        requiredSpellKeys = new HashSet<>(prerequisiteSpells.size());
        for (PrerequisiteSpell prereq : prerequisiteSpells) {
            requiredSpellKeys.add(prereq.getSpellKey().getKey());
            allRequiredSpells.add(prereq.getSpellKey().getKey());
        }

        // Icon information for upgrading/migrating wands
        icon = ConfigurationUtils.toMaterialAndData(ConfigurationUtils.getIcon(template, controller.isLegacyIconsEnabled()));
        migrateIcon = ConfigurationUtils.toMaterialAndData(ConfigurationUtils.getIcon(template, controller.isLegacyIconsEnabled(), "migrate_icon"));

        // Validate requirements - disabling a required spell disables the upgrade.
        for (PrerequisiteSpell requiredSpell : requiredSpells) {
            SpellTemplate spell = controller.getSpellTemplate(requiredSpell.getSpellKey().getKey());
            if (spell == null) {
                controller.getLogger().warning("Invalid spell required for upgrade: " + requiredSpell.getSpellKey().getKey() + ", upgrade path " + key + " will disable upgrades");
                upgradeKey = null;
            }
        }

        matchSpellMana = template.getBoolean("match_spell_mana", matchSpellMana);
        hidden = template.getBoolean("hidden", false);
        earnsSP = template.getBoolean("earns_sp", earnsSP);

        // Description information
        Messages messages = controller.getMessages();
        name = template.getString("name", name);
        name = messages.get("paths." + key + ".name", name);
        description = template.getString("description", description);
        description = messages.get("paths." + key + ".description", description);

        // Upgrade commands
        upgradeCommands = template.getStringList("upgrade_commands");
        upgradeBroadcast = template.getString("upgrade_broadcast", upgradeBroadcast);

        // Effects
        if (template.contains("effects")) {
            effects.clear();
            ConfigurationSection effectsNode = template.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList<>(effects.get(referenceKey)));
                    }
                } else {
                    effects.put(effectKey, controller.loadEffects(effectsNode, effectKey));
                }
            }
        }

        // Fetch overall limits
        maxUses = template.getInt("max_uses", maxUses);
        maxMaxMana = template.getInt("max_mana", maxMaxMana);
        maxManaRegeneration = template.getInt("max_mana_regeneration", maxManaRegeneration);
        maxMana = template.getInt("mana_max", maxMana);
        manaRegeneration = template.getInt("mana_regeneration", manaRegeneration);

        minLevel = template.getInt("min_enchant_level", minLevel);
        maxLevel = template.getInt("max_enchant_level", maxLevel);

        ConfigurationSection maxConfig = template.getConfigurationSection("max_properties");
        if (maxConfig != null) {
            for (String maxKey : maxConfig.getKeys(false)) {
                double value = maxConfig.getDouble(maxKey);
                maxProperties.put(maxKey.replace("|", "."), value);
            }
        }

        Collection<String> tagList = ConfigurationUtils.getStringList(template, "tags");
        if (tagList != null && !tagList.isEmpty()) {
            if (tags == null) {
                tags = new HashSet<>(tagList);
            } else {
                tags.addAll(tagList);
            }
        }

        // Parse defined levels
        if (levelMap == null) {
            levelMap = new TreeMap<>();
        }
        if (template.contains("levels")) {
            String[] levelStrings = StringUtils.split(template.getString("levels"), ',');
            levels = new int[levelStrings.length];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = Integer.parseInt(levelStrings[i]);
            }
        }

        if (levels == null) {
            levels = new int[1];
            levels[0] = 1;
        }

        for (int level = 1; level <= levels[levels.length - 1]; level++) {
            // TODO: Could this be optimized?
            int levelIndex;
            int nextLevelIndex = 0;
            float distance = 1;
            for (levelIndex = 0; levelIndex < levels.length; levelIndex++) {
                if (level == levels[levelIndex] || levelIndex == levels.length - 1) {
                    nextLevelIndex = levelIndex;
                    distance = 0;
                    break;
                }

                if (level < levels[levelIndex + 1]) {
                    nextLevelIndex = levelIndex + 1;
                    int previousLevel = levels[levelIndex];
                    int nextLevel = levels[nextLevelIndex];
                    distance = (float)(level - previousLevel) / (float)(nextLevel - previousLevel);
                    break;
                }
            }

            WandLevel wandLevel = levelMap.get(level);
            WandLevel newLevel = new WandLevel(this, controller, template, levelIndex, nextLevelIndex, distance);
            if (wandLevel == null) {
                wandLevel = newLevel;
            } else {
                newLevel.add(wandLevel);
                wandLevel = newLevel;
            }
            levelMap.put(level, wandLevel);
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Nullable
    public WandLevel getLevel(int level) {
        if (levelMap == null) return null;

        if (!levelMap.containsKey(level)) {
            if (level > levelMap.lastKey()) {
                return levelMap.lastEntry().getValue();
            }

            return levelMap.firstEntry().getValue();
        }

        return levelMap.get(level);
    }

    @Nullable
    protected static WandUpgradePath getPath(MageController controller, String key, ConfigurationSection configuration) {
        resolvingKeys.clear();
        return getPath(controller, key, configuration, resolvingKeys);
    }

    @Nullable
    protected static WandUpgradePath getPath(MageController controller, String key, ConfigurationSection configuration, Set<String> resolving) {
        // Catch circular dependencies
        if (resolving.contains(key)) {
            controller.getLogger().log(Level.WARNING, "Circular dependency detected in paths: " + StringUtils.join(resolving, " -> ") + " -> " + key);
            return null;
        }
        resolving.add(key);
        WandUpgradePath path = paths.get(key);
        if (path == null) {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!ConfigurationUtils.isEnabled(parameters)) {
                return null;
            }
            String inheritKey = parameters.getString("inherit");
            if (inheritKey != null && !inheritKey.isEmpty()) {
                WandUpgradePath inherit = getPath(controller, inheritKey, configuration, resolving);
                if (inherit == null) {
                    Bukkit.getLogger().warning("Failed to load inherited path '" + inheritKey + "' for path: " + key);
                    return null;
                }
                path = new WandUpgradePath(controller, key, inherit, parameters);
            } else {
                path = new WandUpgradePath(controller, key, parameters);
            }

            paths.put(key, path);
        }

        return path;
    }

    public static WandUpgradePath getPath(String key) {
        return paths.get(key);
    }

    public static void loadPaths(MageController controller, ConfigurationSection configuration) {
        paths.clear();
        Set<String> pathKeys = configuration.getKeys(false);
        for (String key : pathKeys)
        {
            getPath(controller, key, configuration);
        }
    }

    public static Set<String> getPathKeys() {
        return paths.keySet();
    }

    public int getMaxLevel() {
        if (levels == null) return 0;

        return Math.min(levels[levels.length - 1], maxLevel);
    }

    @Nullable
    public Set<Integer> getLevels() {
        if (levelMap == null) return null;
        Set<Integer> filteredLevels = new HashSet<>();
        for (Integer level :  levelMap.keySet()) {
            if (level >= minLevel && level <= maxLevel) {
                filteredLevels.add(level);
            }
        }
        return filteredLevels;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getMaxMaxMana() {
        return maxMaxMana;
    }

    public int getMaxManaRegeneration() {
        return maxManaRegeneration;
    }

    public double getMaxProperty(String propertyKey) {
        Double maxValue = maxProperties.get(propertyKey);
        return maxValue == null ? 1 : maxValue;
    }

    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    @Override
    public boolean hasAnyTag(Collection<String> tagSet) {
        return tags != null && !Collections.disjoint(tagSet, tags);
    }

    @Override
    public boolean hasAllTags(Collection<String> tagSet) {
        return tags != null && tags.containsAll(tagSet);
    }

    @Override
    public Set<String> getMissingTags(Collection<String> tagSet) {
        Set<String> tags = getTags();
        if (tags != null) {
            Set<String> s = new HashSet<>(tagSet);
            s.removeAll(tags);
            tags = s;
        } else {
            tags = new HashSet<>(tagSet);
        }
        return tags;
    }

    @Override
    public Collection<String> getSpells() {
        return new ArrayList<>(allSpells);
    }

    @Override
    public Collection<String> getExtraSpells() {
        return new ArrayList<>(allExtraSpells);
    }

    @Override
    public Collection<String> getRequiredSpells() {
        return new ArrayList<>(allRequiredSpells);
    }

    @Override
    public boolean requiresSpell(String spellKey) {
        return requiredSpellKeys.contains(spellKey);
    }

    @Override
    public boolean hasSpell(String spellKey) {
        return spells.contains(spellKey);
    }

    @Override
    public boolean containsSpell(String spellKey) {
        return allSpells.contains(spellKey) || allExtraSpells.contains(spellKey);
    }

    @Override
    public boolean hasBrush(String brushKey) {
        return brushes.contains(brushKey);
    }

    @Override
    public boolean containsBrush(String brushKey) {
        return allBrushes.contains(brushKey);
    }

    @Override
    public boolean hasExtraSpell(String spellKey) {
        return extraSpells.contains(spellKey);
    }

    @Override
    public String getName() {
        return name == null || name.isEmpty() ? key : name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected void playEffects(Mage mage, String effectType) {
        Collection<EffectPlayer> players = effects.get(effectType);
        if (players == null || mage == null) return;

        Entity sourceEntity = mage.getEntity();
        Location mageLocation = mage.getEyeLocation();

        for (EffectPlayer player : players) {
            player.setColor(mage.getEffectColor());
            player.start(mageLocation, sourceEntity, null, null);
        }
    }

    public void enchanted(Mage mage) {
        playEffects(mage, "enchant");
    }

    @Override
    public void checkMigration(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        if (icon != null && migrateIcon != null && migrateIcon.equals(wand.getIcon())) {
            wand.setIcon(icon);
        } else if (parent != null) {
            parent.checkMigration(wand);
        }

        int manaRegeneration = wand.getManaRegeneration();
        if (this.manaRegeneration > 0 && maxManaRegeneration == 0 && this.manaRegeneration  > manaRegeneration) {
            wand.setManaRegeneration(this.manaRegeneration);
        }
        int manaMax = wand.getManaMax();
        if (this.maxMana > 0 && maxMaxMana == 0 && this.maxMaxMana > manaMax) {
            wand.setManaMax(this.maxMana);
        }
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon() {
        return icon;
    }

    public void upgraded(MageController controller, com.elmakers.mine.bukkit.api.wand.Wand wand, Mage mage) {
        CommandSender sender = Bukkit.getConsoleSender();
        Location location = null;
        if (mage != null) {
            playEffects(mage, "upgrade");
            location = mage.getLocation();
        }
        Player player = mage != null ? mage.getPlayer() : null;
        boolean shouldRunCommands = (player == null || !player.hasPermission("Magic.bypass_upgrade_commands"));
        boolean shouldBroadcast = (player == null || !player.hasPermission("Magic.bypass_upgrade_broadcast"));
        if (upgradeCommands != null && shouldRunCommands) {
            for (String command : upgradeCommands) {
                if (command.contains("@uuid") || command.contains("@pn") || command.contains("@pd")) {
                    if (mage == null) {
                        continue;
                    }
                    command = command.replace("@uuid", mage.getId())
                            .replace("@pn", mage.getName())
                            .replace("@pd", mage.getDisplayName());;
                }
                if (location != null) {
                    command = command
                        .replace("@world", location.getWorld().getName())
                        .replace("@x", Double.toString(location.getX()))
                        .replace("@y", Double.toString(location.getY()))
                        .replace("@z", Double.toString(location.getZ()));
                }
                WandUpgradePath upgrade = getPath(upgradeKey);
                command = command.replace("$path", upgrade.getName());
                command = ChatColor.translateAlternateColorCodes('&', command);
                controller.getPlugin().getServer().dispatchCommand(sender, command);
            }
        }
        if (upgradeBroadcast != null && !upgradeBroadcast.isEmpty() && shouldBroadcast) {
            WandUpgradePath upgrade = getPath(upgradeKey);
            String message = upgradeBroadcast
                .replace("$pn", mage.getName())
                .replace("$pn", mage.getDisplayName())
                .replace("$name", mage.getName())
                .replace("$path", upgrade.getName());
            message = ChatColor.translateAlternateColorCodes('&', message);
            for (Player messagePlayer : controller.getPlugin().getServer().getOnlinePlayers()) {
                TextUtils.sendMessage(messagePlayer, message);
            }
        }
        if (upgradeItemKey != null && !upgradeItemKey.isEmpty()) {
            com.elmakers.mine.bukkit.api.wand.Wand upgradeWand = controller.createWand(upgradeItemKey);
            if (upgradeWand != null) {
                if (wand != null) {
                    wand.add(upgradeWand);
                } else if (mage != null) {
                    mage.getActiveProperties().add(upgradeWand);
                }
            }
        }
    }

    @Override
    public boolean hasUpgrade() {
        return upgradeKey != null && !upgradeKey.isEmpty();
    }

    @Override
    @Nullable
    public WandUpgradePath getUpgrade() {
        return getPath(upgradeKey);
    }

    @Override
    @Nullable
    public ProgressionPath getNextPath() {
        return getUpgrade();
    }

    public boolean getMatchSpellMana() {
        return matchSpellMana;
    }

    @Override
    public boolean canProgress(CasterProperties properties) {
        if (levelMap == null) return false;

        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        Deque<WeightedPair<String>> remainingSpells = maxLevel.getRemainingSpells(properties);

        Mage mage = properties.getMage();
        if (mage != null && mage.getDebugLevel() > 0) {
            mage.sendDebugMessage("Spells remaining: " + remainingSpells.size());
        }

        return (remainingSpells.size() > 0);
    }

    @Override
    public boolean canEnchant(com.elmakers.mine.bukkit.api.wand.Wand apiWand) {
        return canProgress(apiWand);
    }

    public boolean hasSpells() {
        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        return maxLevel.getSpellProbabilityCount() > 0;
    }

    public boolean hasMaterials() {
        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        return maxLevel.getMaterialProbabilityCount() > 0;
    }

    private String getMessage(Messages messages, String messageKey) {
        String message = messages.get("spell." + messageKey);
        message = messages.get("wand." + messageKey, message);
        message = messages.get("path." + messageKey, message);
        return messages.get("paths." + key + "." + messageKey, message);
    }

    @Override
    public boolean checkUpgradeRequirements(com.elmakers.mine.bukkit.api.wand.Wand wand, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        return checkUpgradeRequirements(wand == null ? mage.getActiveProperties() : wand, mage == null);
    }

    @Override
    public boolean checkUpgradeRequirements(CasterProperties caster, boolean quiet) {
        if (requiredSpells == null || requiredSpells.isEmpty()) return true;
        if (caster == null) {
            return false;
        }
        MageController controller = caster.getController();
        Mage mage = caster.getMage();

        // Then check for spell requirements to advance
        for (PrerequisiteSpell prereq : requiredSpells) {
            if (!caster.hasSpell(prereq.getSpellKey().getKey())) {
                SpellTemplate spell = controller.getSpellTemplate(prereq.getSpellKey().getKey());
                if (spell == null) {
                    controller.getLogger().warning("Invalid spell required for upgrade: " + prereq.getSpellKey().getKey());
                    return false;
                }
                if (mage != null && !quiet)
                {
                    String requiredSpellMessage = getMessage(controller.getMessages(), "required_spell");
                    String message = requiredSpellMessage.replace("$spell", spell.getName());
                    com.elmakers.mine.bukkit.api.wand.WandUpgradePath upgradePath = getUpgrade();
                    if (upgradePath != null) {
                        message = message.replace("$path", upgradePath.getName());
                    }
                    mage.sendMessage(message);
                }
                return false;
            } else {
                Spell spell = caster.getSpell(prereq.getSpellKey().getKey());
                if (!PrerequisiteSpell.isSpellSatisfyingPrerequisite(spell, prereq)) {
                    if (mage != null && !quiet) {
                        String message = getMessage(controller.getMessages(), "spell.prerequisite_spell_level")
                                .replace("$name", spell.getName())
                                .replace("$level", Integer.toString(prereq.getSpellKey().getLevel()));
                        if (prereq.getProgressLevel() > 1) {
                            message += getMessage(controller.getMessages(), "spell.prerequisite_spell_progress_level")
                                    .replace("$level", Long.toString(prereq.getProgressLevel()))
                                    // This max level should never return 0 here but just in case we'll make the min 1.
                                    .replace("$max_level", Long.toString(Math.max(1, spell.getMaxProgressLevel())));
                        }
                        mage.sendMessage(message);
                    }
                    return false;
                }
            }
        }

        return true;
    }

    public float getBonusLevelMultiplier()
    {
        return bonusLevelMultiplier;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean hasPath(String pathName) {
        if (this.key.equalsIgnoreCase(pathName)) return true;
        if (followsPath != null && followsPath.equalsIgnoreCase(pathName)) return true;
        if (parent != null) {
            return parent.hasPath(pathName);
        }

        return false;
    }

    @Override
    public String translatePath(String pathKey) {
        if (followsPath != null) {
            if (followsPath.equalsIgnoreCase(pathKey)) {
                return key;
            }
            if (upgradeKey != null) {
                return getUpgrade().translatePath(pathKey);
            }
        }
        return pathKey;
    }

    protected void upgradeTo(CasterProperties properties) {
        properties.setPath(getKey());

        boolean addedProperties = false;
        ConfigurationSection wandProperties = ConfigurationUtils.newConfigurationSection();
        int manaRegeneration = properties.getManaRegeneration();
        if (this.manaRegeneration > 0 && maxManaRegeneration == 0 && this.manaRegeneration  > manaRegeneration) {
            addedProperties = true;
            wandProperties.set("mana_regeneration", this.manaRegeneration);
        }
        int manaMax = properties.getManaMax();
        if (this.maxMana > 0 && maxMaxMana == 0 && this.maxMana > manaMax) {
            addedProperties = true;
            wandProperties.set("mana_max", this.maxMana);
        }

        if (addedProperties) {
            properties.upgrade(wandProperties);
        }
    }

    private void upgrade(@Nonnull Mage mage, @Nonnull WandUpgradePath newPath) {
        com.elmakers.mine.bukkit.api.wand.Wand wand = mage.getActiveWand();
        MageController controller = mage.getController();
        String message = getMessage(controller.getMessages(), "level_up").replace("$path", newPath.getName());
        if (wand != null) {
            message = message.replace("$wand", wand.getName());
        }
        mage.sendMessage(message);

        WandUpgradeEvent legacyEvent = new WandUpgradeEvent(mage, wand, this, newPath);
        Bukkit.getPluginManager().callEvent(legacyEvent);

        MageClass mageClass = wand == null ? mage.getActiveClass() : wand.getMageClass();
        PathUpgradeEvent upgradeEvent = new PathUpgradeEvent(mage, wand, mageClass, this, newPath);
        Bukkit.getPluginManager().callEvent(upgradeEvent);
    }

    private void upgrade(@Nonnull com.elmakers.mine.bukkit.api.wand.Wand wand, @Nonnull WandUpgradePath newPath) {
        if (this.icon != null && this.icon.equals(wand.getIcon())) {
            com.elmakers.mine.bukkit.api.block.MaterialAndData newIcon = newPath.getIcon();
            if (newIcon != null) {
                wand.setIcon(newIcon);
            }
        }
    }

    @Override
    public void upgrade(@Nonnull Mage mage, @Nullable com.elmakers.mine.bukkit.api.wand.Wand wand) {
        doUpgrade(mage, wand);
    }

    @Override
    public void upgrade(com.elmakers.mine.bukkit.api.wand.Wand wand, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        doUpgrade(mage, wand);
    }

    private void doUpgrade(@Nullable Mage mage, @Nullable com.elmakers.mine.bukkit.api.wand.Wand wand) {
        WandUpgradePath newPath = getUpgrade();
        MageController controller = null;
        if (mage != null) {
            controller = mage.getController();
        }
        if (controller == null && wand != null) {
            controller = wand.getController();
        }
        if (controller == null) {
            return;
        }
        if (newPath == null) {
            if (mage != null) {
                mage.sendMessage("Configuration issue, please check logs");
            }
            controller.getLogger().warning("Invalid upgrade path: " + this.getUpgrade());
            return;
        }
        if (wand != null) {
            upgrade(wand, newPath);
        }
        if (mage != null) {
            upgrade(mage, newPath);
        }
        this.upgraded(controller, wand, mage);
        newPath.upgradeTo(mage != null ? mage.getActiveProperties() : wand);
    }

    @Override
    public boolean earnsSP() {
        return earnsSP;
    }
}
