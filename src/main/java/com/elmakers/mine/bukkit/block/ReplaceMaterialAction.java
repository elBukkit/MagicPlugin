package com.elmakers.mine.bukkit.block;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.plugins.magic.Mage;

public class ReplaceMaterialAction extends SimpleBlockAction
{
	protected Mage mage;
	protected MaterialBrush brush;
	protected Set<MaterialAndData> replaceable = new HashSet<MaterialAndData>();
	
	private boolean spawnFallingBlocks = false;
	private Vector fallingBlockVelocity = null;
	
	public ReplaceMaterialAction(Mage mage, Block targetBlock, MaterialBrush brush)
	{
		this.mage = mage;
		replaceable.add(new MaterialAndData(targetBlock));
		this.brush = brush;
	}

	public ReplaceMaterialAction(Mage playerSpells, MaterialBrush brush)
	{
		this.mage = playerSpells;
		this.brush = brush;
	}

	public void addReplaceable(Material material)
	{
		replaceable.add(new MaterialAndData(material));
	}

	public void addReplaceable(Material material, byte data)
	{
		replaceable.add(new MaterialAndData(material, data));
	}

	@SuppressWarnings("deprecation")
	public SpellResult perform(Block block)
	{
		if (brush == null)
		{
			return SpellResult.FAIL;
		}
		
		if (!mage.hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		if (mage.isIndestructible(block))
		{
			return SpellResult.FAIL;
		}

		if (replaceable == null || replaceable.contains(new MaterialAndData(block)))
		{
			Material previousMaterial = block.getType();
			byte previousData = block.getData();
			
			if (brush.isDifferent(block)) {
				brush.update(mage, block.getLocation());
				brush.modify(block);
				mage.getController().updateBlock(block);
				
				if (spawnFallingBlocks) {
					FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
					falling.setDropItem(false);
					if (fallingBlockVelocity != null) {
						falling.setVelocity(fallingBlockVelocity);
					}
				}
			}
			super.perform(block);
			return SpellResult.CAST;
		}

		return SpellResult.FAIL;
	}
}
