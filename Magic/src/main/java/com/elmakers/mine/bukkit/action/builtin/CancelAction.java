package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;

public class CancelAction extends BaseSpellAction
{
	private String undoListName;
    private Collection<String> spellKeys;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        if (parameters.contains("spells")) {
            spellKeys = ConfigurationUtils.getStringList(parameters, "spells");
        } else if (parameters.contains("spell")) {
            String targetSpellKey = parameters.getString("spell", null);
            if (targetSpellKey != null && !targetSpellKey.isEmpty()) {
                spellKeys = new ArrayList<String>();
                spellKeys.add(targetSpellKey);
            }
        }
    }

    @Override
	public SpellResult perform(CastContext context)
	{
		Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
		if (targetEntity == null || !controller.isMage(targetEntity)) {
            return SpellResult.NO_TARGET;
        }

        SpellResult result = SpellResult.NO_TARGET;
        Mage targetMage = controller.getMage(targetEntity);
        if (spellKeys == null) {
            Batch batch = targetMage.cancelPending(true);
            if (batch != null) {
                result = SpellResult.CAST;
                undoListName = batch.getName();
            }
        } else {
            for (String spellKey : spellKeys) {
                Batch batch = targetMage.cancelPending(spellKey);
                if (batch != null) {
                    result = SpellResult.CAST;
                    undoListName = batch.getName();
                }
            }
        }
        return result;
	}

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public String transformMessage(String message) {
		return message.replace("$undo", undoListName == null ? "Unknown" : undoListName);
	}
}
