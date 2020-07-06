package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CancelAction extends BaseSpellAction
{
    private String undoListName;
    private Collection<String> spellKeys;
    private boolean force;
    private boolean current;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        force = parameters.getBoolean("force", false);
        current = parameters.getBoolean("current", true);
        if (parameters.contains("spells")) {
            spellKeys = ConfigurationUtils.getStringList(parameters, "spells");
        } else if (parameters.contains("spell")) {
            String targetSpellKey = parameters.getString("spell", null);
            if (targetSpellKey != null && !targetSpellKey.isEmpty()) {
                spellKeys = new ArrayList<>();
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
            Batch batch = targetMage.cancelPending(force);
            if (batch != null) {
                result = SpellResult.CAST;
                undoListName = batch.getName();
            }
        } else {
            for (String spellKey : spellKeys) {
                Batch batch = targetMage.cancelPending(spellKey, force, current);
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
