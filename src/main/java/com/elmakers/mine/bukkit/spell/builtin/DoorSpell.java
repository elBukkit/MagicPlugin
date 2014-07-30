package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class DoorSpell extends BlockSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();

		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}
		
		Block targetBlock = target.getBlock();
        byte data = targetBlock.getData();
        if ((data & 0x8) != 0) {
            targetBlock = targetBlock.getRelative(BlockFace.DOWN);
        }

        if (!hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        registerForUndo(targetBlock);
        String type = parameters.getString("type", "open");
        if (type.equalsIgnoreCase("open")) {
            targetBlock.setData((byte)(data | 0x4));
        } else if (type.equalsIgnoreCase("close")) {
            targetBlock.setData((byte)(data & ~0x4));
        } else if (type.equalsIgnoreCase("toggle")) {
            targetBlock.setData((byte)(data ^ 0x4));
        } else {
            return SpellResult.FAIL;
        }
		
		registerForUndo();
		return SpellResult.CAST;
	}
}
