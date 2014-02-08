package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ShrinkSpell extends Spell
{
	private int             DEFAULT_PLAYER_DAMAGE = 1;
	private int             DEFAULT_ENTITY_DAMAGE = 100;

	private final static int 		maxEffectRange = 16;
	private final static int 		effectSpeed = 1;
	private final static int 		effectPeriod = 2;
    private final static float 		particleData = 2;
    private final static int 		particleCount = 6;
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String castType = parameters.getString("type");
		if (castType != null && castType.equalsIgnoreCase("self")) {
			dropHead(getPlayer().getLocation(), getPlayer().getName(), null, (byte)3);
			return SpellResult.SUCCESS;
		}
		String giveName = parameters.getString("name");
		if (giveName != null) {
			dropHead(getPlayer().getLocation(), giveName, null, (byte)3);
			return SpellResult.SUCCESS;
		}
		
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.INSTANT_SPELL);
		effectTrail.setParticleCount(particleCount);
		effectTrail.setEffectData(particleData);
		effectTrail.setParticleOffset(0.2f, 0.2f, 0.2f);
		effectTrail.setSpeed(effectSpeed);
		effectTrail.setPeriod(effectPeriod);
		effectTrail.start();
		
		this.targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

		if (target.isEntity()) {
			if (!(target.getEntity() instanceof LivingEntity)) return SpellResult.NO_TARGET;
			
			int playerDamage = parameters.getInteger("player_damage", DEFAULT_PLAYER_DAMAGE);
			int entityDamage = parameters.getInteger("entity_damage", DEFAULT_ENTITY_DAMAGE);

			Entity targetEntity = target.getEntity();
			LivingEntity li = (LivingEntity)targetEntity;
			String ownerName = null;
			String itemName = null;
			byte data = 3;
			if (li instanceof Player)
			{
				li.damage(mage.getDamageMultiplier() * playerDamage, getPlayer());
				ownerName = ((Player)li).getName();
			}
			else
			{
				li.damage(mage.getDamageMultiplier() * entityDamage);
				switch (li.getType()) {
					case CREEPER:
						data = 4;
						ownerName = null;
					break;
					case ZOMBIE:
						data = 2;
						ownerName = null;
					break;
					case SKELETON:
						Skeleton skeleton = (Skeleton)li;
						data = (byte)(skeleton.getSkeletonType() == SkeletonType.NORMAL ? 0 : 1);
						ownerName = null;
					break;
					default:
						ownerName = getMobSkin(li.getType());
						if (ownerName != null) {
							itemName = li.getType().getName() + " Head";
						}
				}
				
			}
			if ((ownerName != null || data != 3) && li.isDead()) {
				dropHead(targetEntity.getLocation(), ownerName, itemName, data);
			}
			castMessage("Boogidie Boogidie");
		} else {
			Block targetBlock = target.getBlock();
			if (targetBlock == null) {
				return SpellResult.NO_TARGET;
			}
			String blockSkin = getBlockSkin(targetBlock.getType());
			if (blockSkin == null) return SpellResult.NO_TARGET;
			
			if (!hasBuildPermission(targetBlock)) 
			{
				return SpellResult.INSUFFICIENT_PERMISSION;
			}
			if (mage.isIndestructible(targetBlock)) 
			{
				return SpellResult.NO_TARGET;
			}

			BlockList shrunk = new BlockList();
			shrunk.add(targetBlock);

			dropHead(targetBlock.getLocation(), blockSkin, targetBlock.getType().name(), (byte)3);
			targetBlock.setType(Material.AIR);
			mage.registerForUndo(shrunk);
			
			castMessage("Shrink!");
		}
		
		return SpellResult.SUCCESS;
	}
	
	@SuppressWarnings("deprecation")
	protected void dropHead(Location location, String ownerName, String itemName, byte data) {
		ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)data);
		if (ownerName != null) {
			shrunkenHead = InventoryUtils.getCopy(shrunkenHead);
			ItemMeta itemMeta = shrunkenHead.getItemMeta();
			if (itemName != null) {
				itemMeta.setDisplayName(itemName);
			}
			shrunkenHead.setItemMeta(itemMeta);
			InventoryUtils.setMeta(shrunkenHead, "SkullOwner", ownerName);
		}
		location.getWorld().dropItemNaturally(location, shrunkenHead);
	}
}
