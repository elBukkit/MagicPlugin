package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.SafetyUtils;

@Deprecated
public class GrenadeSpell extends BlockSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        int size = parameters.getInt("size", 6);
        int count = parameters.getInt("count", 1);
        size = (int)(mage.getRadiusMultiplier() * size);
        int fuse = parameters.getInt("fuse", 80);
        boolean useFire = parameters.getBoolean("fire", false);
        boolean breakBlocks = parameters.getBoolean("break_blocks", true);

        Block target = getTarget().getBlock();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        if (!hasBreakPermission(target)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        Location loc = getEyeLocation();
        final Random rand = new Random();
        for (int i = 0; i < count; i++)
        {
            Location targetLoc = loc.clone();
            if (count > 1)
            {
                targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * count) - count);
                targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * count) - count);
            }
            TNTPrimed grenade = (TNTPrimed)getWorld().spawnEntity(targetLoc, EntityType.PRIMED_TNT);
            if (grenade == null) {
                return SpellResult.FAIL;
            }
            Vector aim = getDirection();
            SafetyUtils.setVelocity(grenade, aim);
            grenade.setYield(size);
            grenade.setFuseTicks(fuse);
            grenade.setIsIncendiary(useFire);
            registerForUndo(grenade);
            if (!breakBlocks) {
                CompatibilityLib.getEntityMetadataUtils().setBoolean(grenade, MagicMetaKeys.CANCEL_EXPLOSION_BLOCKS, true);
            }
        }

        registerForUndo();
        return SpellResult.CAST;
    }
}
