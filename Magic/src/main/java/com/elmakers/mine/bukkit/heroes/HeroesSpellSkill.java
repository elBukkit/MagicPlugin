package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.logging.Level;

public class HeroesSpellSkill extends ActiveSkill {
    private SpellTemplate spellTemplate;
    private MageController controller;

    public HeroesSpellSkill(Heroes heroes, String spellKey) {
        super(heroes, spellKey);
        Plugin magicPlugin = heroes.getServer().getPluginManager().getPlugin("Magic");
        if (magicPlugin == null || !(magicPlugin instanceof MagicAPI) && !magicPlugin.isEnabled()) {
            heroes.getLogger().warning("MagicHeroes skills require the Magic plugin");
            throw new RuntimeException("MagicHeroes skills require the Magic plugin");
        }
        try {
            MagicAPI api = (MagicAPI) magicPlugin;
            controller = api.getController();
            spellTemplate = controller.getSpellTemplate(spellKey);
            if (spellTemplate == null) {
                heroes.getLogger().warning("Failed to load Magic skill spell: " + spellKey);
                throw new RuntimeException("Failed to load Magic skill spell: " + spellKey);
            }

            this.setDescription(spellTemplate.getDescription());
            this.setUsage("/skill " + spellKey);
            this.setArgumentRange(0, 0);
            this.setIdentifiers(new String[]{"skill " + spellKey});
        } catch (Throwable ex) {
            heroes.getLogger().log(Level.SEVERE, "Error loading Magic spell " + spellKey, ex);
            throw new RuntimeException("Failed to load Magic skill spell: " + spellKey, ex);
        }
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        Mage mage = controller.getMage(hero.getPlayer());
        boolean success = false;
        if (mage != null) {
            Spell spell = mage.getSpell(spellTemplate.getKey());
            if (spell != null) {
                success = spell.cast();
            }
        }
        return success ? SkillResult.NORMAL : SkillResult.FAIL;
    }

    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.MAX_DISTANCE.node(), spellTemplate.getRange());
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

    @Override
    public String getDescription(Hero hero) {
        return this.getDescription();
    }
}
