package com.elmakers.mine.bukkit.action.builtin;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

public class DropSkullAction extends BaseSpellAction
{
    private boolean allowEntities;
    private boolean allowBlocks;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        allowEntities = parameters.getBoolean("allow_entities", true);
        allowBlocks = parameters.getBoolean("allow_blocks", true);
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
        String itemName = DeprecatedUtils.getDisplayName(li) + " Head";
        EntityType replaceType = null;

        Location targetLocation = targetEntity.getLocation();
        dropHead(context, targetEntity, itemName);

        return SpellResult.CAST;
    }

    protected void dropHead(MageController controller, Location location, String ownerName, String itemName) {
        controller.getSkull(ownerName, itemName, new ItemUpdatedCallback() {
            @Override
            public void updated(@Nullable ItemStack itemStack) {
                if (!ItemUtils.isEmpty(itemStack)) {
                    location.setX(location.getX() + 0.5);
                    location.setY(location.getY() + 0.5);
                    location.setZ(location.getZ() + 0.5);
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
        });
    }

    protected void dropHead(CastContext context, Entity entity, String itemName) {
        context.getController().getSkull(entity, itemName, new ItemUpdatedCallback() {
            @Override
            public void updated(@Nullable ItemStack itemStack) {
                Location location = entity instanceof LivingEntity ? ((LivingEntity)entity).getEyeLocation() : entity.getLocation();
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
