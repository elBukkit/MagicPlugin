package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
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
    private final Set<String> requiredSpells = new HashSet<String>();
    private String upgradeKey;
    private String upgradeItemKey;
    private String name;
    private String description;
    private boolean hidden = false;

    private boolean matchSpellMana = true;

    private int maxUses = 500;
    private int maxMaxXp = 1500;
    private int maxXpRegeneration = 150;
    private float maxHungerRegeneration = 2;
    private float maxHealthRegeneration = 2;
    private float maxDamageReduction = 0.4f;
    private float maxDamageReductionExplosions = 0.3f;
    private float maxDamageReductionFalling = 0.9f;
    private float maxDamageReductionFire = 0.5f;
    private float maxDamageReductionPhysical = 0.1f;
    private float maxDamageReductionProjectiles = 0.2f;
    private float maxCostReduction = 0.5f;
    private float maxCooldownReduction = 0.5f;
    private float maxHaste = 1;
    private float maxPower = 1;
    private int minLevel = 10;
    private int maxLevel = 50;

    private float bonusLevelMultiplier = 0.5f;

    public WandUpgradePath(MageController controller, String key, WandUpgradePath inherit, ConfigurationSection template)
    {
        this.parent = inherit;
        this.key = key;
        this.levels = inherit.levels;
        this.maxMaxXp = inherit.maxMaxXp;
        this.maxXpRegeneration = inherit.maxXpRegeneration;
        this.maxHungerRegeneration = inherit.maxHungerRegeneration;
        this.maxHealthRegeneration = inherit.maxHealthRegeneration;
        this.maxDamageReduction = inherit.maxDamageReduction;
        this.maxDamageReductionExplosions = inherit.maxDamageReductionExplosions;
        this.maxDamageReductionFalling = inherit.maxDamageReductionFalling;
        this.maxDamageReductionFire = inherit.maxDamageReductionFire;
        this.maxDamageReductionPhysical = inherit.maxDamageReductionPhysical;
        this.maxDamageReductionProjectiles = inherit.maxDamageReductionProjectiles;
        this.maxCostReduction = inherit.maxCostReduction;
        this.maxHaste = inherit.maxHaste;
        this.maxPower = inherit.maxPower;
        this.minLevel = inherit.minLevel;
        this.maxLevel = inherit.maxLevel;
        this.matchSpellMana = inherit.matchSpellMana;
        this.levelMap = new TreeMap<Integer, WandLevel>(inherit.levelMap);
        effects.putAll(inherit.effects);
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
        load(controller, key, template);
    }

    protected void load(MageController controller, String key, ConfigurationSection template) {
        // Cache spells, mainly used for spellbooks
        ConfigurationSection spellSection = template.getConfigurationSection("spells");
        if (spellSection != null) {
            spells.addAll(spellSection.getKeys(false));
        }

        // Upgrade information
        upgradeKey = template.getString("upgrade");
        upgradeItemKey = template.getString("upgrade_item");
        requiredSpells.addAll(template.getStringList("required_spells"));

        matchSpellMana = template.getBoolean("match_spell_mana", matchSpellMana);
        hidden = template.getBoolean("hidden", false);

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
        maxMaxXp = template.getInt("max_mana", maxMaxXp);
        maxXpRegeneration = template.getInt("max_mana_regeneration", maxXpRegeneration);
        maxHealthRegeneration = (float)template.getDouble("max_health_regeneration", maxHealthRegeneration);
        maxHungerRegeneration = (float)template.getDouble("max_hunger_regeneration", maxHungerRegeneration);

        minLevel = template.getInt("min_enchant_level", minLevel);
        maxLevel = template.getInt("max_enchant_level", maxLevel);

        maxDamageReduction = (float)template.getDouble("max_damage_reduction", maxDamageReduction);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_explosions", maxDamageReductionExplosions);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_falling", maxDamageReductionFalling);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_fire", maxDamageReductionFire);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_physical", maxDamageReductionPhysical);
        maxDamageReduction = (float)template.getDouble("max_damage_reduction_projectiles", maxDamageReductionProjectiles);
        maxCostReduction = (float)template.getDouble("max_cost_reduction", maxCostReduction);
        maxCooldownReduction = (float)template.getDouble("max_cooldown_reduction", maxCooldownReduction);
        maxHaste = (float)template.getDouble("max_haste", maxHaste);

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
           return;
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
            WandLevel newLevel = new WandLevel(this, template, levelIndex, nextLevelIndex, distance);
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

    public int getMaxMaxXp() {
        return maxMaxXp;
    }

    public int getMaxXpRegeneration() {
        return maxXpRegeneration;
    }

    public float getMaxHungerRegeneration() {
        return maxHungerRegeneration;
    }

    public float getMaxHealthRegeneration() {
        return maxHealthRegeneration;
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

    public float getMaxHaste() {
        return maxHaste;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public Collection<String> getSpells() {
        return new ArrayList(spells);
    }

    @Override
    public Collection<String> getRequiredSpells() {
        return new ArrayList(requiredSpells);
    }

    @Override
    public boolean requiresSpell(String spellKey) {
        return requiredSpells.contains(spellKey);
    }

    @Override
    public boolean hasSpell(String spellKey) {
        return spells.contains(spellKey);
    }

    @Override
    public String getName() {
        return name;
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
    private void applyUpgradeItems(Wand wand, Mage mage) {
        applyUpgradeItem(wand, mage);
        if (parent != null) {
            parent.applyUpgradeItems(wand, mage);
        }
    }

    public void catchup(Wand wand, Mage mage) {
        if (parent != null) {
            parent.applyUpgradeItems(wand, mage);
        }
    }

    public void upgraded(com.elmakers.mine.bukkit.api.wand.Wand wand, Mage mage) {
        CommandSender sender = Bukkit.getConsoleSender();
        Location location = null;
        if (mage != null) {
            playEffects(mage, "upgrade");
            location = mage.getLocation();
        }
        if (upgradeCommands != null) {
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

    public boolean checkEnchant(Wand wand) {
        // First check to see if the path has more spells available
        WandLevel maxLevel = levelMap.get(levels[levels.length - 1]);
        int spellCount = maxLevel.getSpellCount();
        int materialCount = maxLevel.getMaterialCount();
        LinkedList<WeightedPair<String>> remainingSpells = maxLevel.getRemainingSpells(wand);
        LinkedList<WeightedPair<String>> remainingMaterials = maxLevel.getRemainingMaterials(wand);
        return ((spellCount > 0 && remainingSpells.size() > 0) || (materialCount > 0 && remainingMaterials.size() > 0));
    }

    @Override
    public boolean checkUpgradeRequirements(com.elmakers.mine.bukkit.api.wand.Wand wand, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (requiredSpells == null && requiredSpells.isEmpty()) return true;

        // Then check for spell requirements to advance
        for (String requiredKey : requiredSpells) {
            if (!wand.hasSpell(requiredKey)) {
                SpellTemplate spell = wand.getController().getSpellTemplate(requiredKey);
                if (spell == null) {
                    wand.getController().getLogger().warning("Invalid spell required for upgrade: " + requiredKey);
                    continue;
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
        if (parent != null) {
            return parent.hasPath(pathName);
        }

        return false;
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
        wand.setPath(newPath.getKey());
    }
}
