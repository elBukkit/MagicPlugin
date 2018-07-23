package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.maps.MapController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class CameraSpell extends TargetingSpell
{
    @SuppressWarnings("deprecation")
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        ItemStack newMapItem = null;
        Integer priority = ConfigurationUtils.getInteger(parameters, "priority", null);
        boolean selfie = false;

        // Check for special case id
        if (parameters.contains("id"))
        {
            newMapItem = new ItemStack(DefaultMaterials.getFilledMap(), 1, (short)parameters.getInt("id", 0));
            String mapName = parameters.getString("name", "Image");
            ItemMeta meta = newMapItem.getItemMeta();
            // TODO: How to handle names with spaces in them?
            meta.setDisplayName(mapName);
            newMapItem.setItemMeta(meta);
        }

        MapController maps = controller.getMaps();

        // Check for special case url
        if (newMapItem == null) {
            String url = parameters.getString("url");
            if (url != null) {
                int x = parameters.getInt("x", 0);
                int y = parameters.getInt("y", 0);
                int width = parameters.getInt("width", 0);
                int height = parameters.getInt("height", 0);
                String mapName = parameters.getString("name", "Photo");
                newMapItem = maps.getURLItem(getWorld().getName(), url, mapName, x, y, width, height, priority);
            }
        }

        if (newMapItem == null) {
            Target target = getTarget();
            String playerName = parameters.getString("name");
            String metaName = null;
            if (playerName == null)
            {
                if (target != null)
                {
                    if (target.hasEntity()) {
                        Entity targetEntity = target.getEntity();
                        selfie = (targetEntity == mage.getEntity());
                        if (targetEntity instanceof Player) {
                            playerName = ((Player)targetEntity).getName();
                        } else {
                            playerName = getMobSkin(targetEntity.getType());
                            if (playerName != null) {
                                metaName = targetEntity.getType().getName();
                            }
                        }
                    } else {
                        Block targetBlock = target.getBlock();
                        if (targetBlock == null) {
                            return SpellResult.NO_TARGET;
                        }
                        playerName = getBlockSkin(targetBlock.getType());
                        if (playerName != null) {
                            metaName = target.getBlock().getType().name().toLowerCase();
                        }
                    }
                }
            }
            if (playerName == null)
            {
                Player player = mage.getPlayer();
                if (player == null) {
                    return SpellResult.NO_TARGET;
                }
                playerName = player.getName();
                selfie = true;
            }
            if (parameters.contains("reload")) {
                maps.forceReloadPlayerPortrait(getWorld().getName(), playerName);
            }
            metaName = (metaName == null) ? playerName : metaName;
            newMapItem = maps.getPlayerPortrait(getWorld().getName(), playerName, priority, "Photo of " + metaName);
        }
        if (newMapItem == null) {
            return SpellResult.FAIL;
        }
        getWorld().dropItemNaturally(getLocation(), newMapItem);

        return selfie ? SpellResult.CAST_SELF : SpellResult.CAST_TARGET;
    }
}
