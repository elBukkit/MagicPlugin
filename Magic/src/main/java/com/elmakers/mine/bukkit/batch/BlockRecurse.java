package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockData;

import java.util.HashSet;
import java.util.Set;

public class BlockRecurse
{
    protected Set<MaterialAndData> replaceable = null;
    protected int maxRecursion = 8;

	public void recurse(ActionContext action, CastContext context)
	{
		recurse(context.getTargetBlock(), action, context, null, 0);
	}

	protected void recurse(Block block, ActionContext recurseAction, CastContext context, BlockFace nextFace, int rDepth)
	{
		if (nextFace != null)
		{
			block = block.getRelative(nextFace);
		}
        if (replaceable != null && !replaceable.contains(new MaterialAndData(block)))
        {
            return;
        }
        UndoList undoList = context.getUndoList();
		if (undoList != null)
		{
			if (undoList.contains(block))
			{
				return;
			}
			undoList.add(block);
		}

        context.setTargetLocation(block.getLocation());
		if (recurseAction.perform(context) != SpellResult.CAST)
		{
			return;
		}

		if (rDepth < maxRecursion)
		{
			for (BlockFace face : BlockData.FACES)
			{
				if (nextFace == null || nextFace != BlockData.getReverseFace(face))
				{
					recurse(block, recurseAction, context, face, rDepth + 1);
				}
			}
		}
	}

    public void addReplaceable(MaterialAndData material) {
        if (replaceable == null) {
            replaceable = new HashSet<>();
        }
        replaceable.add(material);
    }

    public void addReplaceable(Material material, byte data) {
        addReplaceable(new MaterialAndData(material, data));
    }

    public int getMaxRecursion() {
		return maxRecursion;
	}

	public void setMaxRecursion(int maxRecursion) {
		this.maxRecursion = maxRecursion;
	}
}
