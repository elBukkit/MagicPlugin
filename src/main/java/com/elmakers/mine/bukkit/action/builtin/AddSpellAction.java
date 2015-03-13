package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class AddSpellAction extends BaseSpellAction
{
    private String spellKey;
    private String requiredPath = null;
    private String requiresCompletedPath = null;

    public void prepare(CastContext context, ConfigurationSection parameters) {
        spellKey = parameters.getString("spell");
        requiredPath = parameters.getString("path", null);
        requiresCompletedPath = parameters.getString("path_end", null);
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
        }
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
            mage.sendMessage(context.getMessage("no_wand"));
            return SpellResult.FAIL;
        }
        if (wand.hasSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        if (requiredPath != null) {
            WandUpgradePath path = wand.getPath();
            if (requiredPath != null && !path.hasPath(requiredPath)) {
                WandUpgradePath requiresPath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPath);
                if (requiresPath != null) {
                    mage.sendMessage(context.getMessage("no_path").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                WandUpgradePath pathUpgrade = path.getUpgrade();
                if (pathUpgrade == null) {
                    mage.sendMessage(context.getMessage("no_upgrade").replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
                if (path.canEnchant(wand)) {
                    mage.sendMessage(context.getMessage("no_path_end").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
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
