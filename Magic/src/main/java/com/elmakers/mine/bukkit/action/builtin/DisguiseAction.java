package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class DisguiseAction extends BaseSpellAction
{
    private static class UndoDisguise implements Runnable
    {
        private final Entity entity;
        private final MageController controller;

        public UndoDisguise(MageController controller, Entity entity)
        {
            this.entity = entity;
            this.controller = controller;
        }

        @Override
        public void run()
        {
            controller.disguise(entity, null);
        }
    }

    private ConfigurationSection disguiseConfig;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        disguiseConfig = ConfigurationUtils.getConfigurationSection(parameters, "disguise");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (entity == null)
        {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        if (disguiseConfig == null && !controller.isDisguised(entity)) {
            return SpellResult.NO_TARGET;
        } else if (disguiseConfig != null && controller.isDisguised(entity)) {
            return SpellResult.NO_TARGET;
        }

        ConfigurationSection disguiseConfig = this.disguiseConfig;
        if (disguiseConfig != null && (disguiseConfig.contains("name") || disguiseConfig.contains("custom_name"))) {
            disguiseConfig = ConfigurationUtils.cloneConfiguration(disguiseConfig);
            if (disguiseConfig.contains("name")) {
                disguiseConfig.set("name", context.parameterize(disguiseConfig.getString("name")));
            }
            if (disguiseConfig.contains("custom_name")) {
                disguiseConfig.set("custom_name", context.parameterize(disguiseConfig.getString("custom_name")));
            }
        }
        controller.disguise(entity, disguiseConfig);
        if (disguiseConfig != null) {
            context.registerForUndo(new UndoDisguise(controller, entity));
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
