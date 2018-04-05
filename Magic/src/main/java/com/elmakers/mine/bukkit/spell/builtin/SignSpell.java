package com.elmakers.mine.bukkit.spell.builtin;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class SignSpell extends BlockSpell
{
    @SuppressWarnings("deprecation")
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        String typeString = parameters.getString("type", "");
        boolean autoGive = parameters.getBoolean("auto_give", false);
        boolean editSign = parameters.getBoolean("edit", false);
        boolean displayName = parameters.getBoolean("display_name", true);
        String prefix = ChatColor.translateAlternateColorCodes('&', parameters.getString("prefix", ""));

        Entity sourceEntity = mage.getEntity();
        if (sourceEntity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }

        if (typeString.equals("give") || (autoGive && isLookingUp()))
        {
            Player player = mage.getPlayer();
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
            registerForUndo(targetBlock);
            registerForUndo();
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
                targetBlock.setType(Material.SIGN);
                // This is -180 + 22.5, the number of degrees between
                // increments, effectively rounding up.
                float yaw = getLocation().getYaw() - 157.5f;
                while (yaw < 0) yaw += 360;
                while (yaw >= 360) yaw -= 360;
                targetBlock.setData((byte)(yaw * 15 / 360));
            }
            if (targetBlock.getState() instanceof Sign)
            {
                Sign sign = (Sign)targetBlock.getState();
                String playerName = displayName ? controller.getEntityDisplayName(sourceEntity) :
                        controller.getEntityName(sourceEntity);
                playerName = prefix + playerName;

                sign.setLine(0, playerName);
                sign.setLine(1, getMessage("sign_message"));
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                sign.setLine(2, dateFormat.format(currentDate));
                sign.setLine(3, timeFormat.format(currentDate));
                sign.update();

                controller.updateBlock(targetBlock);

                if (editSign && sourceEntity instanceof Player) {
                    Player player = (Player)sourceEntity;
                    InventoryUtils.openSign(player, targetBlock.getLocation());
                }

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
