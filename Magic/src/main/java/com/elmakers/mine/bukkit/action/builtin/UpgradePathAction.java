package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;

public class UpgradePathAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();

        // For now... eventually I need to separate out the wand part, but not doing it now.
        if (wand == null) {
            return SpellResult.NO_TARGET;
        }

        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
        WandUpgradePath nextPath = path != null ? path.getUpgrade() : null;
        if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
            path.upgrade(wand, mage);
            return SpellResult.CAST;
        }

        return SpellResult.NO_TARGET;
    }
}
