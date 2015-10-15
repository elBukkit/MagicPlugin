package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

public class AddSpellAction extends BaseSpellAction
{
    private String spellKey;
    private String requiredPath = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    private String permissionNode = null;
    protected boolean autoUpgrade = false;

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        permissionNode = parameters.getString("permission", null);
        spellKey = parameters.getString("spell");
        requiredPath = parameters.getString("path", null);
        requiresCompletedPath = parameters.getString("path_end", null);
        exactPath = parameters.getString("path_exact", null);
        autoUpgrade = parameters.getBoolean("auto_upgrade", true);
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
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (wand == null || spellKey == null || spellKey.isEmpty()) {
            context.sendMessageKey("no_wand");
            return SpellResult.FAIL;
        }
        if (wand.hasSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        if (requiredPath != null || exactPath != null) {
            WandUpgradePath path = wand.getPath();
            if (path == null) {
                context.sendMessage(context.getMessage("no_upgrade").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }
            if ((requiredPath != null && !path.hasPath(requiredPath)) || (exactPath != null && !exactPath.equals(path.getKey()))) {
                WandUpgradePath requiresPath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPath);
                if (requiresPath != null) {
                    context.sendMessage(context.getMessage("no_path").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                WandUpgradePath pathUpgrade = path.getUpgrade();
                if (pathUpgrade == null) {
                    context.sendMessage(context.getMessage("no_upgrade").replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
                if (path.canEnchant(wand)) {
                    context.sendMessage(context.getMessage("no_path_end").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
        }

        SpellTemplate currentSpell = wand.getBaseSpell(spellKey);
        if (!wand.addSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        wand.setActiveSpell(spellKey);

        Messages messages = context.getController().getMessages();
        SpellTemplate spell = context.getController().getSpellTemplate(spellKey);
        if (spell != null) {
            if (currentSpell != null) {
                String levelDescription = spell.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = spell.getName();
                }
                context.sendMessage(messages.get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$wand", wand.getName()).replace("$level", levelDescription));
                context.sendMessage(spell.getUpgradeDescription().replace("$name", currentSpell.getName()));
            } else {
                context.sendMessage(messages.get("wand.spell_added").replace("$name", spell.getName()).replace("$wand", wand.getName()));
            }
        }

        if (autoUpgrade) {
            com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
            WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
            if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                path.upgrade(wand, mage);
            }
        }

        return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("spell");
        parameters.add("path");
        parameters.add("path_end");
        parameters.add("path_exact");
        parameters.add("permission");
        parameters.add("auto_upgrade");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("spell")) {
            Collection<SpellTemplate> spellList = MagicPlugin.getAPI().getSpellTemplates();
            for (SpellTemplate spellTemplate : spellList) {
                examples.add(spellTemplate.getKey());
            }
        } else if (parameterKey.equals("path") || parameterKey.equals("path_exact") || parameterKey.equals("path_end")) {
            examples.addAll(com.elmakers.mine.bukkit.wand.WandUpgradePath.getPathKeys());
        } else if (parameterKey.equals("auto_upgrade")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
