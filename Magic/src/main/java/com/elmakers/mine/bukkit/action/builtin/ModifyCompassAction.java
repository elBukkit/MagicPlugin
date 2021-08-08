package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class ModifyCompassAction extends BaseSpellAction {
    private boolean isTrackedLodestone;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        isTrackedLodestone = parameters.getBoolean("tracked", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Player player = context.getMage().getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item == null ? null : item.getItemMeta();
        if (!CompatibilityLib.getCompatibilityUtils().setCompassTarget(meta, context.getTargetLocation(), isTrackedLodestone)) {
            return SpellResult.NO_TARGET;
        }
        item.setItemMeta(meta);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("tracked");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("tracked")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return false;
    }
}
