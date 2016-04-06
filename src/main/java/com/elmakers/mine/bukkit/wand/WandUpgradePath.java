package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.event.WandUpgradeEvent;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A represents a randomized upgrade path that a wand may use
 * when upgrading.
 *
 * Upgrading is generally done by spending XP on an enchanting table.
 */
public class WandUpgradePath implements com.elmakers.mine.bukkit.api.wand.WandUpgradePath {
    private static Map<String, WandUpgradePath> paths = new HashMap<String, WandUpgradePath>();

    private TreeMap<Integer, WandLevel> levelMap = null;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<String, Collection<EffectPlayer>>();
    private List<String> upgradeCommands;
    private int[] levels = null;
    private final String key;
    private final WandUpgradePath parent;
    private final Set<String> spells = new HashSet<String>();
    private Collection<PrerequisiteSpell> requiredSpells = new HashSet<PrerequisiteSpell>();
    private Set<String> requiredSpellKeys = new HashSet<String>();
    private final Set<String> allSpells = new HashSet<String>();
    private final Set<String> allRequiredSpells = new HashSet<String>();
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
    private int maxMaxMana = 1500;
    private int maxManaRegeneration = 150;
    private float maxDamageReduction = 0.4f;
    private float maxDamageReductionExplosions = 0.3f;
    private float maxDamageReductionFalling = 0.9f;
    private float maxDamageReductionFire = 0.5f;
    private float maxDamageReductionPhysical = 0.1f;
    private float maxDamageReductionProjectiles = 0.2f;
    private float maxCostReduction = 0.5f;
    private float maxCooldownReduction = 0.5f;
    private float maxPower = 1;
    private int minLevel = 10;
    private int maxLevel = 50;

    private float bonusLevelMultiplier = 0.5f;

    public WandUpgradePath(MageController controller, String key, WandUpgradePath inherit, ConfigurationSection template)
    {
        this.parent = inherit;
        this.key = key;
        this.levels = inherit.levels;
        this.maxMaxMana = inherit.maxMaxMana;
        this.maxManaRegeneration = inherit.maxManaRegeneration;
        this.maxDamageReduction = inherit.maxDamageReduction;
        this.maxDamageReductionExplosions = inherit.maxDamageReductionExplosions;
        this.maxDamageReductionFalling = inherit.maxDamageReductionFalling;
        this.maxDamageReductionFire = inherit.maxDamageReductionFire;
        this.maxDamageReductionPhysical = inherit.maxDamageReductionPhysical;
        this.maxDamageReductionProjectiles = inherit.maxDamageReductionProjectiles;
        this.maxCostReduction = inherit.maxCostReduction;
        this.maxPower = inherit.maxPower;
        this.minLevel = inherit.minLevel;
        this.maxLevel = inherit.maxLevel;
        this.matchSpellMana = inherit.matchSpellMana;
        this.earnsSP = inherit.earnsSP;
        this.levelMap = new TreeMap<Integer, WandLevel>(inherit.levelMap);
        this.icon = inherit.icon;
        this.migrateIcon = inherit.migrateIcon;
        effects.putAll(inherit.effects);
        allRequiredSpells.addAll(inherit.allRequiredSpells);
        allSpells.addAll(inherit.allSpells);

        if (inherit.tags != null && !inherit.tags.isEmpty())
        {
            this.tags = new HashSet<String>(inherit.tags);
        }

        load(controller, key, template);

        if ((this.upgradeCommands == null || this.upgradeCommands.size() == 0) && inherit.upgradeCommands != null)
        {
            this.upgradeCommands = new ArrayList<String>();
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
        Collection<PrerequisiteSpell> pathSpells = ConfigurationUtils.getPrerequisiteSpells(template, "spells");
        if (!pathSpells.isEmpty()) {
            for (PrerequisiteSpell prereq : pathSpells) {
                if (controller.getSpellTemplate(prereq.getSpellKey().getKey()) != null) {
                    spells.add(prereq.getSpellKey().getKey());
                } else {
                    controller.getLogger().warning("Unknown or disabled spell " + prereq.getSpellKey().getKey() + " in enchanting path " + key + ", ignoring");
                }
            }
        }
        allSpells.addAll(spells);

        // Upgrade information
        followsPath = template.getString("follows");
        upgradeKey = template.getString("upgrade");
        upgradeItemKey = template.getString("upgrade_item");

        Collection<PrerequisiteSpell> prerequisiteSpells = ConfigurationUtils.getPrerequisiteSpells(template, "required_spells");
        this.requiredSpells = new ArrayList<PrerequisiteSpell>();
        requiredSpells.addAll(pathSpells);
        requiredSpells.addAll(prerequisiteSpells);

        requiredSpellKeys = new HashSet<String>(prerequisiteSpells.size());
        for (PrerequisiteSpell prereq : prerequisiteSpells) {
            requiredSpellKeys.add(prereq.getSpellKey().getKey());
            allRequiredSpells.add(prereq.getSpellKey().getKey());
        }

        // Icon information for upgrading/migrating wands
        icon = ConfigurationUtils.getMaterialAndData(template, "icon");
        migrateIcon = ConfigurationUtils.getMaterialAndData(template, "migrate_icon");

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

        // Effects
        if (template.contains("effects")) {
            effects.clear();
            ConfigurationSection effectsNode = template.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList(effects.get(referenceKey)));
                    }
                } else {
                    effects.put(effectKey, EffectPlayer.loadEffects(controller.getPlugin(), effectsNode, effectKey));
                }
            }
        }

        // Fetch overall limits
        maxUses = template.getInt("max_uses", maxUses);
        maxMaxMana = template.getInt("max_mana", maxMaxMana);
        maxManaRegeneration = template.getInt("max_mana_regeneration", maxManaRegeneration);

        minLevel = template.getInt("min_enchant_level", minLevel);
        maxLevel = template.getInt("max_enchant_level", maxLevel);

        maxDamageReduction = (float)template.getDouble("max_protection", maxDamageReduction);
        maxDamageReductionExplosions = (float)template.getDouble("max_protection_explosions", maxDamageReductionExplosions);
        maxDamageReductionFalling = (float)template.getDouble("max_protection_falling", maxDamageReductionFalling);
        maxDamageReductionFire = (float)template.getDouble("max_protection_fire", maxDamageReductionFire);
        maxDamageReductionPhysical = (float)template.getDouble("max_protection_physical", maxDamageReductionPhysical);
        maxDamageReductionProjectiles = (float)template.getDouble("max_protection_projectiles", maxDamageReductionProjectiles);
        maxCostReduction = (float)template.getDouble("max_cost_reduction", maxCostReduction);
        maxCooldownReduction = (float)template.getDouble("max_cooldown_reduction", maxCooldownReduction);

        Collection<String> tagList = ConfigurationUtils.getStringList(template, "tags");
        if (tagList != null && !tagList.isEmpty()) {
            if (tags == null) {
                tags = new HashSet<String>(tagList);
            } else {
                tags.addAll(tagList);
            }
        }

        // Parse defined levels
        if (levelMap == null) {
            levelMap = new TreeMap<Integer, WandLevel>();
        }
        if (template.contains("levels")) {
            String[] levelStrings = StringUtils.split(template.getString("levels"), ",");
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

    protected static WandUpgradePath getPath(MageController controller, String key, ConfigurationSection configuration)
    {
        WandUpgradePath path = paths.get(key);
        if (path == null) {
            ConfigurationSection parameters = configuration.getConfigurationSection(key);
            if (!parameters.getBoolean("enabled", true)) {
                return null;
            }
            String inheritKey = parameters.getString("inherit");
            if (inheritKey != null && !inheritKey.isEmpty()) {
                WandUpgradePath inherit = getPath(controller, inheritKey, configuration);
                if (inherit == null) {
                    Bukkit.getLogger().warning("Failed to load inherited enchanting path '" + inheritKey + "' for path: " + key);
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

    public static WandUpgradePath getPath(String key) {
        return paths.get(key);
    }

    public int getMaxLevel() {
        if (levels == null) return 0;

        return Math.min(levels[levels.length - 1], maxLevel);
    }

    public Set<Integer> getLevels() {
        if (levelMap == null) return null;
        Set<Integer> filteredLevels = new HashSet<Integer>();
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

    public float getMaxDamageReduction() {
        return maxDamageReduction;
    }

    public float getMaxDamageReductionExplosions() {
        return maxDamageReductionExplosions;
    }

    public float getMaxDamageReductionFalling() {
        return maxDamageReductionFalling;
    }

    public float getMaxDamageReductionFire() {
        return maxDamageReductionFire;
    }

    public float getMaxDamageReductionPhysical() {
        return maxDamageReductionPhysical;
    }

    public float getMaxDamageReductionProjectiles() {
        return maxDamageReductionProjectiles;
    }

    public float getMaxCostReduction() {
        return maxCostReduction;
    }

    public float getMaxCooldownReduction() {
        return maxCooldownReduction;
    }

    public float getMaxPower() {
        return maxPower;
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
            Set<String> s = new HashSet<String>(tagSet);
            s.removeAll(tags);
            tags = s;
        } else {
            tags = new HashSet<String>(tagSet);
        }
        return tags;
    }

    @Override
    public Collection<String> getSpells() {
        return new ArrayList(allSpells);
    }

    @Override
    public Collection<String> getRequiredSpells() {
        return new ArrayList<String>(allRequiredSpells);
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
    public String getName() {
        return name == null ? key : name;
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

    private void applyUpgradeItem(Wand wand, Mage mage) {
        if (upgradeItemKey != null && !upgradeItemKey.isEmpty()) {
            com.elmakers.mine.bukkit.api.wand.Wand upgradeWand = mage.getController().createWand(upgradeItemKey);
            if (upgradeWand != null) {
                wand.add(upgradeWand);
            }
        }
    }

    @Override
    public void checkMigration(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        if (icon != null && migrateIcon != null && migrateIcon.equals(wand.getIcon()))
        {
            wand.setIcon(icon);
        }
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon() {
        return icon;
    }

    public void upgraded(com.elmakers.mine.bukkit.api.wand.Wand wand, Mage mage) {
        CommandSender sender = Bukkit.getConsoleSender();
        Location location = null;
        if (mage != null) {
            playEffects(mage, "upgrade");
            location = mage.getLocation();
        }
        Player player = mage != null ? mage.getPlayer() : null;
        boolean shouldRunCommands = (player == null || !player.hasPermission("Magic.bypass_upgrade_commands"));
        if (upgradeCommands != null && shouldRunCommands) {
            for (String command : upgradeCommands) {
                if (command.contains("@uuid") || command.contains("@pn") || command.contains("@pd")) {
                    if (mage == null ) {
                        Bukkit.getLogger().warning("Tried to upgrade with commands but no mage");
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
                wand.getController().getPlugin().getServer().dispatchCommand(sender, command);
            }
        }
        if (upgradeItemKey != null && !upgradeItemKey.isEmpty()) {
            com.elmakers.mine.bukkit.api.wand.Wand upgradeWand = wand.getController().createWand(upgradeItemKey);
            if (upgradeWand != null) {
                wand.add(upgradeWand, mage);
            }
        }
    }

    @Override
    public boolean hasUpgrade() {
        return upgradeKey != null && !upgradeKey.isEmpty();
    }

    @Override
    public WandUpgradePath getUpgrade() {
        return getPath(upgradeKey);
    }

    public boolean getMatchSpellMana() {
        return matchSpellMana;
    }

    @Override
    public boolean canEnchant(com.elmakers.mine.bukkit.api.wand.Wand apiWand) {
        // First check to see if the path has more spells available
        if (!(apiWand instanceof Wand)) return false;
        Wand wand = (Wand)apiWand;
        if (levelMap == null) return false;

        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        int spellCount = maxLevel.getSpellCount();
        LinkedList<WeightedPair<String>> remainingSpells = maxLevel.getRemainingSpells(wand);

        Mage mage = wand.getActivePlayer();
        if (mage != null && mage.getDebugLevel() > 0) {
            mage.sendDebugMessage("Spells remaining: " + remainingSpells.size() + ", max per enchant: " + spellCount);
        }

        return (spellCount > 0 && remainingSpells.size() > 0);
    }

    public boolean hasSpells() {
        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        return maxLevel.getSpellProbabilityCount() > 0 && maxLevel.getSpellCount() > 0;
    }

    public boolean hasMaterials() {
        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        return maxLevel.getMaterialProbabilityCount() > 0 && maxLevel.getMaterialCount() > 0;
    }

    @Override
    public boolean checkUpgradeRequirements(com.elmakers.mine.bukkit.api.wand.Wand wand, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (requiredSpells == null || requiredSpells.isEmpty()) return true;

        // Then check for spell requirements to advance
        for (PrerequisiteSpell prereq : requiredSpells) {
            if (!wand.hasSpell(prereq.getSpellKey().getKey())) {
                SpellTemplate spell = wand.getController().getSpellTemplate(prereq.getSpellKey().getKey());
                if (spell == null) {
                    wand.getController().getLogger().warning("Invalid spell required for upgrade: " + prereq.getSpellKey().getKey());
                    return false;
                }
                if (mage != null)
                {
                    String message = wand.getController().getMessages().get("spell.required_spell").replace("$spell", spell.getName());
                    com.elmakers.mine.bukkit.api.wand.WandUpgradePath upgradePath = getUpgrade();
                    if (upgradePath != null) {
                        message = message.replace("$path", upgradePath.getName());
                    }
                    mage.sendMessage(message);
                }
                return false;
            } else {
                Spell spell = wand.getSpell(prereq.getSpellKey().getKey());
                if (!PrerequisiteSpell.isSpellSatisfyingPrerequisite(spell, prereq)) {
                    if (mage != null && spell != null) {
                        String message = wand.getController().getMessages().get("spell.prerequisite_spell_level")
                                .replace("$name", spell.getName())
                                .replace("$level", Integer.toString(prereq.getSpellKey().getLevel()));
                        if (prereq.getProgressLevel() > 1) {
                            message += wand.getController().getMessages().get("spell.prerequisite_spell_progress_level")
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

    @Override
    public void upgrade(com.elmakers.mine.bukkit.api.wand.Wand wand, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath newPath = getUpgrade();
        if (newPath == null) {
            if (mage != null) mage.sendMessage("Configuration issue, please check logs");
            wand.getController().getLogger().warning("Invalid upgrade path: " + this.getUpgrade());
            return;
        }

        if (mage != null) {
            MageController controller = mage.getController();
            mage.sendMessage(controller.getMessages().get("wand.level_up").replace("$wand", wand.getName()).replace("$path", newPath.getName()));
        }
        this.upgraded(wand, mage);
        if (this.icon != null && this.icon.equals(wand.getIcon())) {
            wand.setIcon(newPath.getIcon());
        }
        wand.setPath(newPath.getKey());

        WandUpgradeEvent upgradeEvent = new WandUpgradeEvent(mage, wand, this, newPath);
        Bukkit.getPluginManager().callEvent(upgradeEvent);
    }

    @Override
    public boolean earnsSP() {
        return earnsSP;
    }
}
