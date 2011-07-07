package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class AlterSpell extends Spell
{
	static final String DEFAULT_ADJUSTABLES = "6, 8, 9, 10,11,17,18,23,27,28,29,33,35,50,52,53,54,55,58,59,60,61,62,63,64,65,66,67,68,69,71,75,76,77,81,83,85,86,93,94,95";
	static final String DEFAULT_ADJUST_MAX =  "15,15,15,15,15, 2,15,5 ,9, 9, 5, 5 ,15,5, 15,3 ,5 ,15,5 ,15,8 ,5 ,5 ,15,15,3 ,9 ,3 ,2 ,14,15,5 ,5 ,15,15,15,5, 0, 5, 5 ,5";
	static final String DEFAULT_ADJUST_MIN =  "0 ,0 ,0 ,0 ,0 ,0 ,0 ,2 ,0, 0, 0, 0 ,0 ,0 ,0 ,0 ,2 ,0 ,2 ,0 ,0 ,2 ,2 ,0 ,0 ,0 ,0 ,0 ,5 ,6 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,3  2, 2 ,2";
	static final String DEFAULT_RECURSABLES = "17,18,59";
	
	private List<Material> adjustableMaterials = new ArrayList<Material>();
	private List<Integer> maxData = new ArrayList<Integer>();
	private List<Integer> minData = new ArrayList<Integer>();
	private List<Material> recursableMaterials = new ArrayList<Material>();
	
	private int recurseDistance = 32;	
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		if (!adjustableMaterials.contains(targetBlock.getType()))
		{
			player.sendMessage("Can't adjust " + targetBlock.getType().name().toLowerCase());
			return false;
		}
		
		BlockList undoList = new BlockList();
		int originalData = targetBlock.getData();
		
		int materialIndex = adjustableMaterials.indexOf(targetBlock.getType());
		int minValue = minData.get(materialIndex);
		int maxValue = maxData.get(materialIndex);
		int dataSize = maxValue - minValue + 1;

		byte data = (byte)((((originalData - minValue) + 1) % dataSize) + minValue);

		boolean recursive = recursableMaterials.contains(targetBlock.getType());
		
		adjust(targetBlock, data, undoList, recursive, 0);
		
		spells.addToUndoQueue(player, undoList);
		
		castMessage(player, "Adjusting " + targetBlock.getType().name().toLowerCase() + " from " + originalData + " to " + data);
		
		return true;
	}
	
	protected void adjust(Block block, byte dataValue, BlockList adjustedBlocks, boolean recursive, int rDepth)
	{
		adjustedBlocks.add(block);
		block.setData(dataValue);
		
		if (recursive && rDepth < recurseDistance)
		{
			Material targetMaterial = block.getType();
			tryAdjust(block.getFace(BlockFace.NORTH), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.WEST), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.SOUTH), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.EAST), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.UP), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.DOWN), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);			
		}
	}
	
	protected void tryAdjust(Block target, byte dataValue, Material targetMaterial, BlockList adjustedBlocks, int rDepth)
	{
		if (target.getType() != targetMaterial || adjustedBlocks.contains(target))
		{
			return;
		}
		
		adjust(target, dataValue, adjustedBlocks, true, rDepth);
	}
	
	@Override
	public String getName()
	{
		return "alter";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Alter certain objects";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		recurseDistance = properties.getInteger("spells-alter-recursion-depth", recurseDistance);
		recursableMaterials = properties.getMaterials("spells-alter-recursable", DEFAULT_RECURSABLES);

		//adjustableMaterials = properties.getMaterials("spells-alter-adjustable", DEFAULT_ADJUSTABLES);
		adjustableMaterials = PluginProperties.parseMaterials(DEFAULT_ADJUSTABLES);
		maxData = PluginProperties.parseIntegers(DEFAULT_ADJUST_MAX);
		minData = PluginProperties.parseIntegers(DEFAULT_ADJUST_MIN);
		
		if (adjustableMaterials.size() != maxData.size() || maxData.size() != minData.size())
		{
			spells.getLog().warning("Spells:Alter: Mis-match in adjustable material lists!");
		}
	}

	@Override
	public Material getMaterial()
	{
		return Material.REDSTONE_TORCH_ON;
	}

}
