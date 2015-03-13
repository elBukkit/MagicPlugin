package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class AddSpellAction extends BaseSpellAction
{
    private String spellKey;

    public void prepare(CastContext context, ConfigurationSection parameters) {
        spellKey = parameters.getString("spell");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null || spellKey == null || spellKey.isEmpty()) {
            return SpellResult.FAIL;
        }
        if (wand.hasSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        SpellTemplate currentSpell = wand.getBaseSpell(spellKey);
        if (!wand.addSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }

        Messages messages = context.getController().getMessages();
        SpellTemplate spell = context.getController().getSpellTemplate(spellKey);
        if (spell != null) {
            if (currentSpell != null) {
                String levelDescription = spell.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = spell.getName();
                }
                mage.sendMessage(messages.get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$wand", wand.getName()).replace("$level", levelDescription));
                mage.sendMessage(spell.getUpgradeDescription().replace("$name", currentSpell.getName()));
            } else {
                mage.sendMessage(messages.get("wand.spell_added").replace("$name", spell.getName()).replace("$wand", wand.getName()));
            }
        }

        return SpellResult.CAST;
	}
}
