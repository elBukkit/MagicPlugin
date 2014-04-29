package com.elmakers.mine.bukkit.spell.builtin;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class SignSpell extends BlockSpell
{  
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		String typeString = parameters.getString("type", "");
		boolean autoAscend = parameters.getBoolean("auto_give", true);

		if (typeString.equals("give") || (autoAscend && isLookingUp()))
		{
			Player player = getPlayer();
			if (player == null) {
				return SpellResult.PLAYER_REQUIRED;
			}
			castMessage(getMessage("cast_give"));
			controller.giveItemToPlayer(player, new ItemStack(Material.SIGN, 4));
			return SpellResult.CAST;
		}

		Target target = getTarget();
		Block block = target.getBlock();
		if (target.isValid() && block != null)
		{
			Block targetBlock = getPreviousBlock();
			if (targetBlock == null || !hasBuildPermission(targetBlock)) {
				return SpellResult.INSUFFICIENT_PERMISSION;
			}
			if (targetBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				targetBlock.setType(Material.WALL_SIGN);
				switch (block.getFace(targetBlock))
				{
				case NORTH:
					targetBlock.setData((byte)2);
					break;
				case SOUTH:
					targetBlock.setData((byte)3);
					break;
				case WEST:
					targetBlock.setData((byte)4);
					break;
				case EAST:
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
				
				registerForUndo(targetBlock);
				registerForUndo();
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
