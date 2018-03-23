package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class ShrinkEntityAction extends DamageAction
{
	private boolean skeletons;
	
	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) 
	{
		super.prepare(context, parameters);
		skeletons = parameters.getBoolean("skeletons", true);
	}

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
		String ownerName = null;
		String itemName = null;
		byte data = 3;
		
		if (li instanceof Player)
		{
			ownerName = ((Player)li).getName();
		}
		else
		{
			itemName = DeprecatedUtils.getName(li.getType()) + " Head";
			switch (li.getType()) {
				case CREEPER:
					data = 4;
				break;
				case ZOMBIE:
					data = 2;
				break;
				case SKELETON:
					Skeleton skeleton = (Skeleton)li;
					data = (byte)(skeleton.getSkeletonType() == SkeletonType.NORMAL ? 0 : 1);
				break;
				default:
					ownerName = controller.getMobSkin(li.getType());
			}
		}

		if (itemName == null && ownerName != null) {
			itemName = ownerName + "'s Head";
		}
		
		Location targetLocation = targetEntity.getLocation();
		if (li instanceof Player) {
			super.perform(context);
			if (li.isDead() && !alreadyDead) {
				dropPlayerHead(targetEntity.getLocation(), (Player)li, itemName);
			}
		}
		else if (li.getType() == EntityType.GIANT) {
			UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
			context.registerModified(li);
			li.remove();
			Entity zombie = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ZOMBIE);
			if (zombie instanceof Zombie) {
				((Zombie)zombie).setBaby(false);
			}
			context.registerForUndo(zombie);
			if (spawnedList != null) {
				spawnedList.add(zombie);
			}
		}
		else if (li instanceof Ageable && ((Ageable)li).isAdult() && !(li instanceof Player)) {
			context.registerModified(li);
			((Ageable)li).setBaby();
		} else  if (li instanceof Zombie && !((Zombie)li).isBaby()) {
			context.registerModified(li);
			((Zombie)li).setBaby(true);
		} else  if (li instanceof PigZombie && !((PigZombie)li).isBaby()) {
			context.registerModified(li);
			((PigZombie)li).setBaby(true);
		} else  if (li instanceof Slime && ((Slime)li).getSize() > 1) {
			context.registerModified(li);
			Slime slime = (Slime)li;
			slime.setSize(slime.getSize() - 1);
		} else  if (li instanceof Skeleton && skeletons && ((Skeleton)li).getSkeletonType() == SkeletonType.WITHER) {
			context.registerModified(li);
			Skeleton skeleton = (Skeleton)li;
			skeleton.setSkeletonType(SkeletonType.NORMAL);
		} else {
			super.perform(context);
			if ((ownerName != null || data != 3) && (li.isDead() || li.getHealth() == 0) && !alreadyDead) {
				dropHead(targetEntity.getLocation(), ownerName, itemName, data);
			}
		}
		
		return SpellResult.CAST;
	}

    protected void dropPlayerHead(Location location, Player player, String itemName) {
		dropHead(location, player.getName(), itemName, (byte)3);
    }
	
	@SuppressWarnings("deprecation")
	protected void dropHead(Location location, String ownerName, String itemName, byte data) {
        ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, data);
        ItemMeta meta = shrunkenHead.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        if (meta instanceof SkullMeta && ownerName != null) {
            SkullMeta skullData = (SkullMeta)meta;
            skullData.setOwner(ownerName);
        }
        shrunkenHead.setItemMeta(meta);
        location.getWorld().dropItemNaturally(location, shrunkenHead);
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
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
