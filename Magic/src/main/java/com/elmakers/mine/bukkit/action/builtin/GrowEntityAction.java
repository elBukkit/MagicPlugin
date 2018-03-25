package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class GrowEntityAction extends BaseSpellAction
{
    private boolean skeletons;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        skeletons = parameters.getBoolean("skeletons", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
        if (controller.isElemental(targetEntity))
        {
            double elementalSize = controller.getElementalScale(targetEntity);
            elementalSize *= 1.2;
            controller.setElementalScale(targetEntity, elementalSize);

            return SpellResult.CAST;
        }

        if (!(targetEntity instanceof LivingEntity)) return SpellResult.NO_TARGET;

        LivingEntity li = (LivingEntity)targetEntity;

        if (li instanceof Ageable && !((Ageable)li).isAdult() && !(li instanceof Player)) {
            context.registerModified(li);
            ((Ageable)li).setAdult();
        } else  if (li instanceof Zombie) {
            context.registerModified(li);
            Zombie zombie = (Zombie)li;
            if (!zombie.isBaby()) {
                UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
                Location targetLocation = li.getLocation();
                li.remove();
                Entity giant = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.GIANT);
                context.registerForUndo(giant);
                if (spawnedList != null) {
                    spawnedList.add(giant);
                }
            } else {
                ((Zombie) li).setBaby(false);
            }
        } else  if (li instanceof PigZombie && ((PigZombie)li).isBaby()) {
            context.registerModified(li);
            ((PigZombie)li).setBaby(false);
        } else  if (li instanceof Slime) {
            context.registerModified(li);
            Slime slime = (Slime)li;
            slime.setSize(slime.getSize() + 1);
        } else  if (li instanceof Skeleton && skeletons && ((Skeleton)li).getSkeletonType() == Skeleton.SkeletonType.NORMAL) {
            context.registerModified(li);
            Skeleton skeleton = (Skeleton)li;
            skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
        } else {
            return SpellResult.NO_TARGET;
        }

        return SpellResult.CAST;
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
        parameters.add("skeleton");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("skeletons")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
