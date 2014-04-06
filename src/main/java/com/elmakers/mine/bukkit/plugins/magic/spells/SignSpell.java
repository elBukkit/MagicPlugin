package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class SignSpell extends BlockSpell
{  
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String typeString = parameters.getString("type", "");
		boolean autoAscend = parameters.getBoolean("auto_give", true);

		if (typeString.equals("give") || (autoAscend && getYRotation() > 80))
		{
			castMessage(getMessage("cast_give"));
			return giveMaterial(Material.SIGN, 8, (short)0, (byte)0) ? SpellResult.CAST : SpellResult.FAIL;
		}

		Target target = getTarget();
		Block block = target.getBlock();
		if (target.isValid() && block != null)
		{
			Block targetBlock = getFaceBlock();
			if (targetBlock == null || !hasBuildPermission(targetBlock)) {
				return SpellResult.INSUFFICIENT_PERMISSION;
			}
			if (targetBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				targetBlock.setType(Material.WALL_SIGN);
				switch (block.getFace(targetBlock))
				{
				case EAST:
					targetBlock.setData((byte)2);
					break;
				case WEST:
					targetBlock.setData((byte)3);
					break;
				case NORTH:
					targetBlock.setData((byte)4);
					break;
				case SOUTH:
					targetBlock.setData((byte)5);
					break;
				default:
					targetBlock.setData((byte)0);
					break;
				}
			}
			else
			{
				targetBlock.setType(Material.SIGN_POST);
				float yaw = getLocation().getYaw();
				yaw = yaw < 180 ? yaw + 180 : yaw - 180;
				targetBlock.setData((byte)(yaw * 15 / 360));
			}
			if (targetBlock.getState() instanceof Sign)
			{
				Sign sign = (Sign)targetBlock.getState();
				String playerName = getPlayer().getName();
				playerName = mage.getController().getMessagePrefix() + playerName;
				sign.setLine(0, playerName);
				sign.setLine(1, getMessage("sign_message"));
				Date currentDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				sign.setLine(2, dateFormat.format(currentDate));
				sign.setLine(3, timeFormat.format(currentDate));
				sign.update();
				
				BlockList signBlocks = new BlockList();
				signBlocks.add(targetBlock);
				registerForUndo(signBlocks);
				controller.updateBlock(targetBlock);
				
				return SpellResult.CAST;
			}
			else
			{
				return SpellResult.FAIL;
			}
		}
		else if (target.hasEntity() && target.getEntity() instanceof Player)
		{
			// Spell will take care of messaging the target player with cast_player_message
			return SpellResult.CAST;
		}

		return SpellResult.NO_TARGET;
	}
}
