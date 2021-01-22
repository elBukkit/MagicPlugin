package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

public class DropItemAction extends BaseSpellAction
{
    private ItemStack item = null;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        MageController controller = context.getController();

        String itemKey = parameters.getString("item");
        item = controller.createItem(itemKey);
        if (item == null) {
            context.getLogger().warning("Invalid item: " + itemKey);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (item == null) {
            return SpellResult.FAIL;
        }
        context.getTargetLocation().getWorld().dropItem(context.getTargetLocation(), item);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("item");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("item")) {
            MagicAPI api = MagicPlugin.getAPI();
            examples.addAll(api.getController().getItemKeys());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
