package com.elmakers.mine.bukkit.heroes;

import java.util.Collection;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

public class HeroesSpellSkill extends ActiveSkill {
    private final SpellTemplate spellTemplate;
    private final MageController controller;
    private final ConfigurationSection parameters = ConfigurationUtils.newConfigurationSection();
    private int spellLevel = 1;

    public HeroesSpellSkill(Heroes heroes, String spellKey) {
        super(heroes, getSkillName(heroes, spellKey));
        Plugin magicPlugin = heroes.getServer().getPluginManager().getPlugin("Magic");
        MagicAPI api = (MagicAPI) magicPlugin;
        controller = api.getController();
        spellTemplate = controller.getSpellTemplate(spellKey);
        String skillKey = spellTemplate.getName().replace(" ", "");

        this.setDescription(spellTemplate.getDescription());
        this.setUsage("/skill " + skillKey);
        this.setArgumentRange(0, 0);
        this.setIdentifiers(new String[]{"skill " + skillKey});
    }

    /**
     * This code is redudant, but unfortunately it needs to be since we need to know the
     * skill name for the super() constructor call.
     */
    private static String getSkillName(Heroes heroes, String spellKey) {
        Plugin magicPlugin = heroes.getServer().getPluginManager().getPlugin("Magic");
        if (magicPlugin == null || (!(magicPlugin instanceof MagicAPI) && !magicPlugin.isEnabled())) {
            heroes.getLogger().warning("MagicHeroes skills require the Magic plugin");
            throw new RuntimeException("MagicHeroes skills require the Magic plugin");
        }
        MagicAPI api = (MagicAPI) magicPlugin;
        MageController controller = api.getController();
        // This is unfortunately needed because Heroes tries to load these skills before
        // Magic has loaded spell configs
        ((MagicController)controller).checkPostStartupLoad();

        SpellTemplate spellTemplate = controller.getSpellTemplate(spellKey);
        if (spellTemplate == null) {
            controller.getLogger().warning("Failed to load Magic skill spell: " + spellKey);
            throw new RuntimeException("Failed to load Magic skill spell: " + spellKey);
        }
        String baseName = ChatColor.stripColor(spellTemplate.getName().replace(" ", ""));
        return controller.getHeroesSkillPrefix() + baseName;
    }

    @Override
    public void init() {
        super.init();
        Set<String> parameterKeys = parameters.getKeys(false);
        for (String parameterKey : parameterKeys) {
            String value = SkillConfigManager.getRaw(this, parameterKey, null);
            parameters.set(parameterKey, value);
        }
        String spellLevelString = SkillConfigManager.getRaw(this, "tier", null);
        if (spellLevelString != null && spellLevelString.isEmpty()) {
            try {
                spellLevel = Integer.parseInt(spellLevelString);
            } catch (NumberFormatException ex) {
                controller.getLogger().warning("Invalid tier in skill config for " + spellTemplate.getKey() + ": " + spellLevelString);
            }
        }
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        Mage mage = controller.getMage(hero.getPlayer());
        boolean success = false;
        {
            String spellKey = spellTemplate.getKey();
            int targetLevel = SkillConfigManager.getUseSetting(hero, this, "tier", spellLevel, true);
            if (targetLevel != 1) {
                SpellKey key = new SpellKey(spellTemplate.getSpellKey().getBaseKey(), targetLevel);
                spellKey = key.getKey();
            }

            Spell spell = mage.getSpell(spellKey);
            if (spell == null) {
                if (targetLevel > 1) {
                    controller.getLogger().warning("Invalid tier for spell in skills config: " + spellKey + " (tier " + spellLevel + ")");
                } else {
                    controller.getLogger().warning("Invalid spell in skills config: " + spellKey);
                }
                return SkillResult.FAIL;
            }
            Set<String> parameterKeys = parameters.getKeys(false);
            ConfigurationSection spellParameters = spellTemplate.getSpellParameters();
            ConfigurationSection heroParameters = ConfigurationUtils.newConfigurationSection();
            for (String parameterKey : parameterKeys) {
                String value = parameters.getString(parameterKey);
                String magicKey = heroesToMagic(parameterKey);
                Double doubleValue = null;
                try {
                    doubleValue = Double.parseDouble(value);
                } catch (NumberFormatException ignored) {
                }

                Object magicValue = spellParameters.getString(magicKey);
                if (doubleValue != null) {
                    doubleValue = SkillConfigManager.getUseSetting(hero, this, parameterKey, doubleValue, true);
                    Double doubleMagicValue = null;
                    try {
                        if (magicValue != null) {
                            doubleMagicValue = Double.parseDouble(magicValue.toString());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    if (doubleMagicValue != null && doubleValue.equals(doubleMagicValue)) continue;
                } else {
                    value = SkillConfigManager.getUseSetting(hero, this, parameterKey, value);
                    if (magicValue != null && value != null && value.equals(magicValue)) continue;
                }
                if (doubleValue != null) {
                    heroParameters.set(magicKey, doubleValue);
                } else {
                    heroParameters.set(magicKey, value);
                }
            }
            // Don't let Magic get in the way of using the skill
            heroParameters.set("cost_reduction", 2);
            heroParameters.set("cooldown_reduction", 2);
            success = spell.cast(heroParameters);
        }
        if (success) {
            this.broadcastExecuteText(hero);
        }
        return success ? SkillResult.NORMAL : SkillResult.FAIL;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();

        // Add in all configured spell parameters
        // Change keys to match heroes-format
        ConfigurationSection spellParameters = spellTemplate.getSpellParameters();
        if (spellParameters != null) {
            Set<String> parameterKeys = spellParameters.getKeys(false);
            for (String parameterKey : parameterKeys) {
                if (!spellParameters.isConfigurationSection(parameterKey) && !spellParameters.isList(parameterKey)) {
                    String heroesKey = magicToHeroes(parameterKey);
                    Object value = spellParameters.get(parameterKey);
                    node.set(heroesKey, value);
                    parameters.set(heroesKey, value);
                }
            }
        }

        node.set("icon-url", spellTemplate.getIconURL());

        MaterialAndData icon = spellTemplate.getIcon();
        if (icon != null && icon.getMaterial() != Material.AIR) {
            node.set("icon", icon.getKey());
        }
        MaterialAndData disabledIcon = spellTemplate.getDisabledIcon();
        if (disabledIcon != null && disabledIcon.getMaterial() != Material.AIR) {
            node.set("icon-disabled", disabledIcon.getKey());
        }
        node.set("icon-url", spellTemplate.getIconURL());
        node.set("icon-disabled-url", spellTemplate.getDisabledIconURL());
        node.set("cooldown", spellTemplate.getCooldown());
        node.set("name", spellTemplate.getName());
        Collection<CastingCost> costs = spellTemplate.getCosts();
        if (costs != null) {
            for (CastingCost cost : costs) {
                if (cost.getMana() > 0) {
                    node.set(SkillSetting.MANA.node(), cost.getMana());
                }
                // TODO: Reagent costs from item costs
            }
        }

        // Check for an upgrade, set tier if present
        SpellKey upgradeKey = new SpellKey(spellTemplate.getSpellKey().getBaseKey(), 2);
        SpellTemplate upgrade = controller.getSpellTemplate(upgradeKey.getKey());
        if (upgrade != null) {
            int maxUpgrade = 2;
            while (upgrade != null) {
                upgradeKey = new SpellKey(spellTemplate.getSpellKey().getBaseKey(), maxUpgrade + 1);
                upgrade = controller.getSpellTemplate(upgradeKey.getKey());
                if (upgrade != null) maxUpgrade++;
            }
            node.set("tier", 1);
            node.set("tier-max", maxUpgrade);
        }
        return node;
    }

    private String magicToHeroes(String key) {
        if (key.equals("range")) {
            return SkillSetting.MAX_DISTANCE.node();
        }
        return key.replace('_', '-');
    }

    private String heroesToMagic(String key) {
        if (key.equals(SkillSetting.MAX_DISTANCE.node())) {
            return "range";
        }
        return key.replace('-', '_');
    }

    public SpellTemplate getSpellTemplate() {
        return spellTemplate;
    }

    @Override
    public String getDescription(Hero hero) {
        return this.getDescription();
    }
}
