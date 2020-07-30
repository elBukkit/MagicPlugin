package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ApplyCooldownAction extends BaseSpellAction
{
    private int cooldownAmount;
    private List<String> spells;
    private Set<String> excludeSpells;
    private boolean clear;
    private boolean bypassReduction;
    private boolean targetCaster;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        cooldownAmount = parameters.getInt("duration", 0);
        if (parameters.contains("reduce")) {
            cooldownAmount = -parameters.getInt("reduce");
        }
        clear = parameters.getBoolean("clear", false);
        bypassReduction = parameters.getBoolean("bypass_reduction", false);
        targetCaster = parameters.getBoolean("target_caster", false);
        if (parameters.contains("spells")) {
            spells = ConfigurationUtils.getStringList(parameters, "spells");
        }
        if (parameters.contains("exclude_spells")) {
            excludeSpells = new HashSet<>(ConfigurationUtils.getStringList(parameters, "exclude_spells"));
        }
        if (parameters.contains("tags")) {
            if (spells == null) {
                spells = new ArrayList<>();
            } else {
                spells = new ArrayList<>(spells);
            }
            List<String> tags = ConfigurationUtils.getStringList(parameters, "tags");
            for (SpellTemplate spell : context.getController().getSpellTemplates()) {
                if (spell.hasAnyTag(tags)) {
                    spells.add(spell.getKey());
                }
            }
        }

        String excludeCSV = parameters.getString("exclude_spells", null);
        if (excludeCSV != null)
        {
            excludeSpells = new HashSet<>(Arrays.asList(StringUtils.split(excludeCSV, ',')));
        }
        else
        {
            excludeSpells = null;
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Mage targetMage = context.getMage();
        if (!targetCaster) {
            Entity entity = context.getTargetEntity();
            MageController controller = context.getController();
            if (entity == null || !controller.isMage(entity)) {
                return SpellResult.NO_TARGET;
            }
            targetMage = controller.getMage(entity);
        }
        int amount = cooldownAmount;
        if (!bypassReduction && cooldownAmount > 0) {
            double cooldownReduction = targetMage.getCooldownReduction();
            if (cooldownReduction < 1) {
                amount = (int)Math.ceil((1.0f - cooldownReduction) * amount);
            } else {
                amount = 0;
            }
        }
        if (spells != null) {
            List<String> spells = this.spells;
            if (spells.size() == 1 && spells.get(0).equals("*")) {
                spells = new ArrayList<>();
                for (Spell spell : targetMage.getSpells()) {
                    spells.add(spell.getKey());
                }
            }
            Wand wand = targetMage.getActiveWand();
            for (String spellName : spells) {
                Spell spell = null;
                if (wand != null) {
                    spell = wand.getSpell(spellName);
                }
                if (spell == null) {
                    spell = targetMage.getSpell(spellName);
                }
                if (spell != null) {
                    if (clear) {
                        spell.clearCooldown();
                    } else if (amount > 0) {
                        spell.setRemainingCooldown(amount);
                    } else {
                        spell.reduceRemainingCooldown(-amount);
                    }
                }
            }
        } else if (excludeSpells != null) {
            Collection<Spell> allSpells = targetMage.getSpells();
            for (Spell spell : allSpells) {
                if (!excludeSpells.contains(spell.getSpellKey().getBaseKey())) {
                    if (clear) {
                        spell.clearCooldown();
                    } else if (amount > 0) {
                        spell.setRemainingCooldown(amount);
                    } else {
                        spell.reduceRemainingCooldown(-amount);
                    }
                }
            }
        } else {
            if (clear) {
                targetMage.clearCooldown();
            } else if (amount > 0) {
                targetMage.setRemainingCooldown(amount);
            } else {
                targetMage.reduceRemainingCooldown(-amount);
            }
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return !targetCaster;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("air");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("air")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
