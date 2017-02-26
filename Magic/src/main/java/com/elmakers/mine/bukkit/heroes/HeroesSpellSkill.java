package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.google.common.collect.ImmutableSet;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

public class HeroesSpellSkill extends ActiveSkill {
    private SpellTemplate spellTemplate;
    private MageController controller;
    private ConfigurationSection parameters = new MemoryConfiguration();

    public HeroesSpellSkill(Heroes heroes, String spellKey) {
        super(heroes, spellKey);
        Plugin magicPlugin = heroes.getServer().getPluginManager().getPlugin("Magic");
        if (magicPlugin == null || !(magicPlugin instanceof MagicAPI) && !magicPlugin.isEnabled()) {
            controller.getLogger().warning("MagicHeroes skills require the Magic plugin");
            throw new RuntimeException("MagicHeroes skills require the Magic plugin");
        }
        try {
            MagicAPI api = (MagicAPI) magicPlugin;
            controller = api.getController();
            spellTemplate = controller.getSpellTemplate(spellKey);
            if (spellTemplate == null) {
                controller.getLogger().warning("Failed to load Magic skill spell: " + spellKey);
                throw new RuntimeException("Failed to load Magic skill spell: " + spellKey);
            }

            this.setDescription(spellTemplate.getDescription());
            this.setUsage("/skill " + spellKey);
            this.setArgumentRange(0, 0);
            this.setIdentifiers(new String[]{"skill " + spellKey});
        } catch (Throwable ex) {
            controller.getLogger().log(Level.SEVERE, "Error loading Magic spell " + spellKey, ex);
            throw new RuntimeException("Failed to load Magic skill spell: " + spellKey, ex);
        }
    }

    @Override
    public void init() {
        try {
            Set<String> parameterKeys = parameters.getKeys(false);
            for (String parameterKey : parameterKeys) {
                String value = SkillConfigManager.getRaw(this, parameterKey, null);
                parameters.set(parameterKey, value);
            }
        } catch (Throwable ex) {
            controller.getLogger().log(Level.SEVERE, "Error initializing skill spell " + spellTemplate.getKey(), ex);
            throw new RuntimeException("Failed to init Magic skill spell", ex);
        }
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        Mage mage = controller.getMage(hero.getPlayer());
        boolean success = false;
        if (mage != null) {
            Spell spell = mage.getSpell(spellTemplate.getKey());
            if (spell != null) {
                Set<String> parameterKeys = parameters.getKeys(false);
                if (parameterKeys.size() > 0) {
                    ConfigurationSection spellParameters = spell.getSpellParameters();
                    ConfigurationSection heroParameters = new MemoryConfiguration();
                    for (String parameterKey : parameterKeys) {
                        String value = parameters.getString(parameterKey);
                        String magicKey = heroesToMagic(parameterKey);
                        Double doubleValue = null;
                        try {
                            doubleValue = Double.parseDouble(value);
                        } catch (NumberFormatException cantparse) {
                        }

                        Object magicValue = spellParameters.getString(magicKey);
                        if (doubleValue != null) {
                            doubleValue = SkillConfigManager.getUseSetting(hero, this, parameterKey, doubleValue, true);
                            Double doubleMagicValue = null;
                            try {
                                if (magicValue != null) {
                                    doubleMagicValue = Double.parseDouble(magicValue.toString());
                                }
                            } catch (NumberFormatException cantparse) {
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
                    heroParameters.set("cost_reduction" , 2);
                    heroParameters.set("cooldown_reduction" , 2);
                    success = spell.cast(heroParameters);
                } else {
                    success = spell.cast();
                }
            }
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
        for (CastingCost cost : costs) {
            if (cost.getMana() > 0) {
                node.set(SkillSetting.MANA.node(), cost.getMana());
            }
            // TODO: Reagent costs from item costs
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

    @Override
    public String getDescription(Hero hero) {
        return this.getDescription();
    }
}
