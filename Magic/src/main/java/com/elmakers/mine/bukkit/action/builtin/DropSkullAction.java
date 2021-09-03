package com.elmakers.mine.bukkit.action.builtin;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class DropSkullAction extends BaseSpellAction
{
    private boolean dropAtSource = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        dropAtSource = parameters.getBoolean("drop_at_source");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof LivingEntity)) {
            Block targetBlock = context.getTargetBlock();
            if (targetBlock == null) {
                return SpellResult.NO_TARGET;
            }
            MageController controller = context.getController();
            String blockSkin = controller.getBlockSkin(targetBlock.getType());
            if (blockSkin == null) {
                return SpellResult.NO_TARGET;
            }
            return SpellResult.NO_TARGET;
        }

        LivingEntity li = (LivingEntity)targetEntity;
        String itemName = CompatibilityLib.getDeprecatedUtils().getDisplayName(li) + " Head";

        Location location;
        if (dropAtSource) {
            location = context.getEyeLocation();
        } else {
            location = targetEntity instanceof LivingEntity ? ((LivingEntity)targetEntity).getEyeLocation() : targetEntity.getLocation();
        }
        dropHead(context, targetEntity, location, itemName);

        return SpellResult.CAST;
    }

    protected void dropHead(CastContext context, Entity entity, Location location, String itemName) {
        context.getController().getSkull(entity, itemName, new ItemUpdatedCallback() {
            @Override
            public void updated(@Nullable ItemStack itemStack) {
                location.getWorld().dropItemNaturally(location, itemStack);
            }
        });
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
