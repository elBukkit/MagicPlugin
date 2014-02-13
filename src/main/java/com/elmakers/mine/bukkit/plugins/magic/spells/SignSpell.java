package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class SignSpell extends Spell
{  
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String typeString = parameters.getString("type", "");
		if (typeString.equals("give"))
		{
			castMessage("Have some signs!");
			return giveMaterial(Material.SIGN, 8, (short)0, (byte)0) ? SpellResult.CAST : SpellResult.FAIL;
		}

		Target target = getTarget();
		if (target.isBlock())
		{
			Block targetBlock = getFaceBlock();
			if (!hasBuildPermission(targetBlock)) {
				return SpellResult.INSUFFICIENT_PERMISSION;
			}
			if (targetBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				targetBlock.setType(Material.WALL_SIGN);
				switch(target.getBlock().getFace(targetBlock))
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
				float yaw = getPlayer().getLocation().getYaw();
				yaw = yaw < 180 ? yaw + 180 : yaw - 180;
				targetBlock.setData((byte)(yaw * 15 / 360));
			}
			if (targetBlock.getState() instanceof Sign)
			{
				Sign sign = (Sign)targetBlock.getState();
				String playerName = getPlayer().getName();
				playerName = mage.getController().getMessagePrefix() + playerName;
				sign.setLine(0, playerName);
				sign.setLine(1, "was here");
				Date currentDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				sign.setLine(2, dateFormat.format(currentDate));
				sign.setLine(3, timeFormat.format(currentDate));
				sign.update();
				castMessage("You leave a tag");
				
				BlockList signBlocks = new BlockList();
				signBlocks.add(targetBlock);
				mage.registerForUndo(signBlocks);
				controller.updateBlock(targetBlock);
				
				return SpellResult.CAST;
			}
			else
			{
				sendMessage("Sign placement failed!");
				return SpellResult.FAIL;
			}
		}
		else if (target.isEntity() && target.getEntity() instanceof Player)
		{
			Player targetPlayer = (Player)target.getEntity();
			targetPlayer.sendMessage(getPlayer().getName() + " says hi!");
			sendMessage("You send " + targetPlayer.getName() + " a message");
			return SpellResult.CAST;
		}

		return SpellResult.NO_TARGET;
	}
}
