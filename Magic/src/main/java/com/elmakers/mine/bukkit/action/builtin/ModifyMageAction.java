package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyMageAction extends BaseSpellAction
{
    public enum ModifyType {
        ACTIVATE,
        LOCK,
        UNLOCK,
        SWITCH,
        UNLOCK_AND_ACTIVATE
    }

    private String mageClass;
    private ModifyType modifyType;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        mageClass = parameters.getString("mage_class", "");
        String modifyTypeString = parameters.getString("modify_type", "activate");
        try {
            modifyType = ModifyType.valueOf(modifyTypeString.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().warning("Invalid ModifyMage modify_type: " + modifyType);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (modifyType == null || mageClass.isEmpty()) {
            return SpellResult.FAIL;
        }
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(target instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }
        MageController controller = context.getController();
        Mage mage = controller.getMage(target);
        MageClass targetClass;
        MageClass activeClass;
        switch (modifyType) {
            case ACTIVATE:
                if (!mage.setActiveClass(mageClass)) {
                    return SpellResult.FAIL;
                }
                break;
            case UNLOCK_AND_ACTIVATE:
                targetClass = mage.unlockClass(mageClass);
                if (targetClass == null) {
                    return SpellResult.FAIL;
                }
                if (!mage.setActiveClass(mageClass)) {
                    return SpellResult.FAIL;
                }
                break;
            case UNLOCK:
                targetClass = mage.unlockClass(mageClass);
                if (targetClass == null) {
                    return SpellResult.FAIL;
                }
                break;
            case LOCK:
                if (!mage.lockClass(mageClass)) {
                    return SpellResult.FAIL;
                }
                break;
            case SWITCH:
                activeClass = mage.getActiveClass();
                if (activeClass != null) {
                    mage.lockClass(activeClass.getKey());
                }
                targetClass = mage.unlockClass(mageClass);
                if (targetClass == null) {
                    if (activeClass != null) {
                        mage.unlockClass(activeClass.getKey());
                    }
                    return SpellResult.FAIL;
                }
                if (!mage.setActiveClass(mageClass)) {
                    return SpellResult.FAIL;
                }
                break;
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("mage_class");
        parameters.add("modify_type");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("modify_type")) {
            for (ModifyType modifyType : ModifyType.values()) {
                examples.add(modifyType.name().toLowerCase());
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
