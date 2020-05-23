package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class AddSpellAction extends BaseSpellAction
{
    private String spellKey;
    private String requiredPath = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    private String permissionNode = null;
    protected boolean autoUpgrade = false;

    @Override
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
        if (spellKey == null || spellKey.isEmpty()) {
            return SpellResult.FAIL;
        }
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        Wand wand = context.getWand();
        String wandName = wand == null ? "?" : wand.getName();
        CasterProperties caster = wand != null ? wand : mage.getActiveProperties();
        if (caster.hasSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        if (requiredPath != null || exactPath != null) {
            ProgressionPath path = caster.getPath();
            if (path == null) {
                context.showMessage(context.getMessage("no_upgrade", "You may not learn here").replace("$wand", wandName));
                return SpellResult.FAIL;
            }
            MageController controller = context.getController();
            if (requiredPath != null && !path.hasPath(requiredPath)) {
                WandUpgradePath requiresPath = controller.getPath(requiredPath);
                if (requiresPath != null) {
                    context.showMessage(context.getMessage("no_required_path", "You must be at least $path!").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (exactPath != null && !exactPath.equals(path.getKey())) {
                WandUpgradePath requiresPath = controller.getPath(exactPath);
                if (requiresPath != null) {
                    context.showMessage(context.getMessage("no_path_exact", "You must be at $path!").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + exactPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                ProgressionPath pathUpgrade = path.getNextPath();
                if (pathUpgrade == null) {
                    context.showMessage(context.getMessage("no_upgrade", "There is nothing more for you here.").replace("$wand", wandName));
                    return SpellResult.FAIL;
                }
                if (path.canProgress(wand)) {
                    context.showMessage(context.getMessage("no_path_end", "You must be ready to advance to $path!").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
        }

        if (!caster.addSpell(spellKey)) {
            return SpellResult.NO_TARGET;
        }
        if (wand != null) {
            wand.setActiveSpell(spellKey);
        }

        if (autoUpgrade) {
            ProgressionPath path = caster.getPath();
            ProgressionPath nextPath = path != null ? path.getNextPath() : null;
            if (nextPath != null && path.checkUpgradeRequirements(caster, true) && !path.canProgress(caster)) {
                path.upgrade(mage, wand);
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
