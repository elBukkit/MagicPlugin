package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class CancelAction extends BaseSpellAction
{
	private String undoListName;
    private String targetSpellKey;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        targetSpellKey = parameters.getString("spell", null);
    }

    @Override
	public SpellResult perform(CastContext context)
	{
		Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
		if (targetEntity == null || !controller.isMage(targetEntity)) {
            return SpellResult.NO_TARGET;
        }

        Mage targetMage = controller.getMage(targetEntity);
        Batch batch = (targetSpellKey == null || targetSpellKey.isEmpty())
                ? targetMage.cancelPending(true)
                : targetMage.cancelPending(targetSpellKey);
        if (batch == null) {
            return SpellResult.NO_TARGET;
        }
        undoListName = batch.getName();

        return SpellResult.CAST;
	}

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public String transformMessage(String message) {
		return message.replace("$spell", undoListName == null ? "Unknown" : undoListName);
	}
}
