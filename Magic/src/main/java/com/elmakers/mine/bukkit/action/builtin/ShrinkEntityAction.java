package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class ShrinkEntityAction extends DamageAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
        if (controller.isElemental(targetEntity))
        {
            double elementalSize = controller.getElementalScale(targetEntity);
            if (elementalSize < 0.1) {
                return super.perform(context);
            }
            elementalSize /= 2;
            controller.setElementalScale(targetEntity, elementalSize);
            return SpellResult.CAST;
        }

        if (!(targetEntity instanceof LivingEntity)) return SpellResult.NO_TARGET;

        LivingEntity li = (LivingEntity)targetEntity;
        boolean alreadyDead = li.isDead() || li.getHealth() <= 0;
        String itemName = DeprecatedUtils.getDisplayName(li) + " Head";
        EntityType replaceType = null;

        Location targetLocation = targetEntity.getLocation();
        if (li instanceof Player) {
            super.perform(context);
            if (li.isDead() && !alreadyDead) {
                dropHead(context, targetEntity, itemName);
            }
        } else if (li.getType() == EntityType.GIANT) {
            replaceType = EntityType.ZOMBIE;
        } else if (li instanceof Ageable && ((Ageable)li).isAdult()) {
            context.registerModified(li);
            ((Ageable)li).setBaby();
        } else  if (li instanceof Zombie && !((Zombie)li).isBaby()) {
            context.registerModified(li);
            ((Zombie)li).setBaby(true);
        } else  if (li instanceof Slime && ((Slime)li).getSize() > 1) {
            context.registerModified(li);
            Slime slime = (Slime)li;
            slime.setSize(slime.getSize() - 1);
        /*
        } else if (li instanceof WitherSkeleton && skeletons) {
            replaceType = EntityType.SKELETON;
        */
        } else {
            super.perform(context);
            if ((li.isDead() || li.getHealth() == 0) && !alreadyDead) {
                dropHead(context, targetEntity, itemName);
            }
        }

        if (replaceType != null) {
            UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
            context.registerModified(li);
            li.remove();
            Entity replacement = targetLocation.getWorld().spawnEntity(targetLocation, replaceType);
            if (replacement instanceof Zombie) {
                ((Zombie)replacement).setBaby(false);
            }
            context.registerForUndo(replacement);
            if (spawnedList != null) {
                spawnedList.add(replacement);
            }
        }

        return SpellResult.CAST;
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
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("skeletons");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("skeletons")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
