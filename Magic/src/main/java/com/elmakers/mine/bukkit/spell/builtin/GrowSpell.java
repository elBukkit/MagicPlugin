package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class GrowSpell extends BlockSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();

		if (!target.hasEntity()) {
            return SpellResult.NO_TARGET;
        }

        Entity targetEntity = target.getEntity();
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
            registerModified(li);
            ((Ageable)li).setAdult();
        } else  if (li instanceof Zombie) {
            registerModified(li);
            Zombie zombie = (Zombie)li;
            if (!zombie.isBaby()) {
                UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
                Location targetLocation = li.getLocation();
                li.remove();
                Entity giant = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.GIANT);
                registerForUndo(giant);
                if (spawnedList != null) {
                    spawnedList.add(giant);
                }
            } else {
                ((Zombie) li).setBaby(false);
            }
        } else  if (li instanceof PigZombie && ((PigZombie)li).isBaby()) {
            registerModified(li);
            ((PigZombie)li).setBaby(false);
        } else  if (li instanceof Slime) {
            registerModified(li);
            Slime slime = (Slime)li;
            slime.setSize(slime.getSize() + 1);
        } else {
            return SpellResult.NO_TARGET;
        }

        registerForUndo();
		return SpellResult.CAST;
	}
}
