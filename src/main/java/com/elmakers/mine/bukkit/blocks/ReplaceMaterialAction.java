package com.elmakers.mine.bukkit.blocks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;

public class ReplaceMaterialAction extends SimpleBlockAction
{
	protected Mage mage;
	protected MaterialBrush brush;
	protected Set<Material> replaceable = new HashSet<Material>();
	
	private boolean spawnFallingBlocks = false;
	private Vector fallingBlockVelocity = null;
	
	public ReplaceMaterialAction(Mage mage, Block targetBlock, MaterialBrush brush)
	{
		this.mage = mage;
		replaceable.add(targetBlock.getType());
		this.brush = brush;
	}

	public ReplaceMaterialAction(Mage playerSpells, MaterialBrush brush)
	{
		this.mage = playerSpells;
		this.brush = brush;
	}

	public void addReplaceable(Material material)
	{
		replaceable.add(material);
	}

	@SuppressWarnings("deprecation")
	public SpellResult perform(Block block)
	{
		if (brush == null)
		{
			return SpellResult.FAILURE;
		}
		
		if (!mage.hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		if (mage.isIndestructible(block))
		{
			return SpellResult.FAILURE;
		}

		if (replaceable == null || replaceable.contains(block.getType()))
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
			return SpellResult.SUCCESS;
		}

		return SpellResult.FAILURE;
	}
}
