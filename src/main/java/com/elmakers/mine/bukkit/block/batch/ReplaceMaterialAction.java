package com.elmakers.mine.bukkit.block.batch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class ReplaceMaterialAction extends SimpleBlockAction
{
	protected MaterialBrush brush;
	protected Set<MaterialAndData> replaceable = new HashSet<MaterialAndData>();
	
	private boolean spawnFallingBlocks = false;
	private Vector fallingBlockVelocity = null;
	
	public ReplaceMaterialAction(BrushSpell spell, Block targetBlock)
	{
		super(spell, spell.getUndoList());
		this.brush = spell.getBrush();
		if (targetBlock != null) {
			replaceable.add(new MaterialAndData(targetBlock));
		}
	}

	public ReplaceMaterialAction(BrushSpell spell)
	{
		this(spell, null);
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
	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		if (brush == null)
		{
			return SpellResult.FAIL;
		}
		
		if (!hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		if (isIndestructible(block))
		{
			return SpellResult.FAIL;
		}

		if (replaceable == null || replaceable.contains(new MaterialAndData(block)))
		{
			Material previousMaterial = block.getType();
			byte previousData = block.getData();
			
			if (brush.isDifferent(block)) {
				Mage mage = getMage();
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
			super.perform(parameters, block);
			return SpellResult.CAST;
		}

		return SpellResult.FAIL;
	}
}
