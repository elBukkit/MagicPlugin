package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class AlterSpell extends Spell
{
	static final String DEFAULT_ADJUSTABLES = "3 ,5, 6, 8, 9, 10,11,12,17,162,18,161,23,24,27,28,29,31,33,35,37,38,43,44,50,52,53,54,55,58,59,60,61,62,63,64,65,66,67,68,69,71,75,76,77,81,83,85,86,93,94,95,98,99,100,104,105,108,109,114,115,125,126,128,134,135,136,140,141,142,144,155,156,159,160,171,172,175";
	static final String DEFAULT_ADJUST_MAX =  "2 ,5, 5 ,15,15,15,15,1 ,15,15 ,3 ,1  ,5 ,2 ,9 ,9 ,5 ,2 ,5 ,15,8 ,8 ,15,15,5, 15,3 ,5 ,15,5 ,7 ,8 ,5 ,5 ,15,15,3 ,9 ,3 ,2 ,14,15,5 ,5 ,15,15,15,5 ,0 ,5 ,5 ,15 ,3 ,15,15 ,7  ,7  ,3  ,3  ,3  ,7  ,15 ,15 ,3  ,3  ,3  ,3  ,15 ,7  ,7  ,4  ,4  ,3  ,15 ,15 ,15 ,15 ,6";
	static final String DEFAULT_ADJUST_MIN =  "0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0  ,0 ,0  ,2 ,0 ,0, 0, 0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,2 ,0 ,2 ,0 ,0 ,2 ,2 ,0 ,0 ,0 ,0 ,0 ,5 ,6 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,3 ,2 ,2 ,0  ,0 ,0 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0";
	static final int DEFAULT_RECURSE_DISTANCE = 0;

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (target.isEntity()) {
			SpellResult result = alterEntity(target.getEntity());
			if (result != SpellResult.NO_TARGET) {
				return result;
			}
		}
		Block targetBlock = target.getBlock();
		if (targetBlock == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		
		int recurseDistance = parameters.getInteger("depth", DEFAULT_RECURSE_DISTANCE);
		recurseDistance = (int)(playerSpells.getRadiusMultiplier() * recurseDistance);

		List<Integer> adjustableMaterials = csv.parseIntegers(DEFAULT_ADJUSTABLES);
		List<Integer> maxData = csv.parseIntegers(DEFAULT_ADJUST_MAX);
		List<Integer> minData = csv.parseIntegers(DEFAULT_ADJUST_MIN);

		if (adjustableMaterials.size() != maxData.size() || maxData.size() != minData.size())
		{
			spells.getLog().warning("Spells:Alter: Mis-match in adjustable material lists!");
		}
		
		if (!adjustableMaterials.contains(targetBlock.getTypeId()))
		{
			castMessage("Can't adjust " + targetBlock.getType().name().toLowerCase());
			return SpellResult.FAILURE;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		BlockList undoList = new BlockList();
		int originalData = targetBlock.getData();

		int materialIndex = adjustableMaterials.indexOf(targetBlock.getTypeId());
		int minValue = minData.get(materialIndex);
		int maxValue = maxData.get(materialIndex);
		int dataSize = maxValue - minValue + 1;

		byte data = (byte)((((originalData - minValue) + 1) % dataSize) + minValue);

		boolean recursive = recurseDistance > 0;

		adjust(targetBlock, data, undoList, recursive, recurseDistance, 0);

		spells.addToUndoQueue(player, undoList);

		castMessage("Adjusting " + targetBlock.getType().name().toLowerCase() + " from " + originalData + " to " + data);
		spells.updateBlock(targetBlock);
		return SpellResult.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	protected void adjust(Block block, byte dataValue, BlockList adjustedBlocks, boolean recursive, int recurseDistance, int rDepth)
	{
		adjustedBlocks.add(block);
		block.setData(dataValue);

		if (recursive && rDepth < recurseDistance)
		{
			Material targetMaterial = block.getType();
			tryAdjust(block.getRelative(BlockFace.NORTH), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);
			tryAdjust(block.getRelative(BlockFace.WEST), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);
			tryAdjust(block.getRelative(BlockFace.SOUTH), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);
			tryAdjust(block.getRelative(BlockFace.EAST), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);
			tryAdjust(block.getRelative(BlockFace.UP), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);
			tryAdjust(block.getRelative(BlockFace.DOWN), dataValue,targetMaterial, adjustedBlocks, recurseDistance, rDepth + 1);			
		}
	}
	
	protected SpellResult alterEntity(Entity entity) {
		EntityType entityType = entity.getType();
		switch (entityType) {
			case HORSE:
				Horse horse = (Horse)entity;

				Color color = horse.getColor();
				Color[] colorValues = Color.values();
				color = colorValues[(color.ordinal() + 1) % colorValues.length];
			
				Variant variant = horse.getVariant();
				Variant[] variantValues = Variant.values();
				variant = variantValues[(variant.ordinal() + 1) % variantValues.length];
			
				Style horseStyle = horse.getStyle();
				Style[] styleValues = Style.values();
				horseStyle = styleValues[(horseStyle.ordinal() + 1) % styleValues.length];
				
				horse.setStyle(horseStyle);
				horse.setColor(color);
				horse.setVariant(variant);
				castMessage("You altered a horse");
			break;
			case OCELOT:
				Ocelot ocelot = (Ocelot)entity;
				Type catType = ocelot.getCatType();
				Type[] typeValues = Type.values();
				catType = typeValues[(catType.ordinal() + 1) % typeValues.length];
				ocelot.setCatType(catType);
				castMessage("You altered an ocelot");
				break;
			case VILLAGER:
				Villager villager = (Villager)entity;
				Profession profession = villager.getProfession();
				Profession[] professionValues = Profession.values();
				profession = professionValues[(profession.ordinal() + 1) % professionValues.length];
				villager.setProfession(profession);
				castMessage("You altered a villager");
				break;
			case WOLF:
			{
				Wolf wolf = (Wolf)entity;
				DyeColor dyeColor = wolf.getCollarColor();
				DyeColor[] dyeColorValues = DyeColor.values();
				dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
				wolf.setCollarColor(dyeColor);
				castMessage("You altered a wolf's collar");
			}
				break;
			case SHEEP:
				{
					Sheep sheep = (Sheep)entity;
					DyeColor dyeColor = sheep.getColor();
					DyeColor[] dyeColorValues = DyeColor.values();
					dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
					sheep.setColor(dyeColor);
					castMessage("You altered a sheep");
				}
				break;
			case SKELETON:
				Skeleton skeleton = (Skeleton)entity;
				SkeletonType skeletonType = skeleton.getSkeletonType();
				SkeletonType[] skeletonTypeValues = SkeletonType.values();
				skeletonType = skeletonTypeValues[(skeletonType.ordinal() + 1) % skeletonTypeValues.length];
				skeleton.setSkeletonType(skeletonType);
				castMessage("You altered a skeleton");
				break;
			default:
				return SpellResult.NO_TARGET;
		};
		
		return SpellResult.SUCCESS;
	}

	protected void tryAdjust(Block target, byte dataValue, Material targetMaterial, BlockList adjustedBlocks, int recurseDistance, int rDepth)
	{
		if (target.getType() != targetMaterial || adjustedBlocks.contains(target))
		{
			return;
		}

		adjust(target, dataValue, adjustedBlocks, true, recurseDistance, rDepth);
	}
}
