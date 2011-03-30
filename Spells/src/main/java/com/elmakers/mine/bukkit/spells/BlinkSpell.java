package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class BlinkSpell extends Spell
{
    private final boolean autoPassthrough        = true;
    private int           maxRange               = 0;
    private final int     verticalSearchDistance = 255;

    @Override
    public String getDescription()
    {
        return "Teleport to your target";
    }

    @Override
    public String getName()
    {
        return "blink";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        if (autoPassthrough)
        {
            Block firstBlock = targeting.getNextBlock();
            if (firstBlock.getType() != Material.AIR)
            {
                targeting.setReverseTargeting(true);
                targeting.setTargetHeightRequired(2);
                targeting.targetThrough(Material.AIR);
            }
            else
            {
                targeting.targetThrough(Material.GLASS);
            }
        }

        Block target = targeting.getTargetBlock();
        Block face = targeting.getLastBlock();

        if (target == null)
        {
            castMessage(player, "Nowhere to blink to");
            return false;
        }
        if (maxRange > 0 && targeting.getDistance(player, target) > maxRange)
        {
            castMessage(player, "Can't blink that far");
            return false;
        }

        World world = player.getWorld();

        // Don't drop the player too far, and make sure there is somewhere to
        // stand
        Block destination = face;
        int distanceUp = 0;
        int distanceDown = 0;
        if (targeting.isReverseTargeting())
        {
            destination = target;
        }
        Block groundBlock = destination.getFace(BlockFace.DOWN);
        while (distanceDown < verticalSearchDistance && !targeting.isOkToStandOn(groundBlock.getType()))
        {
            destination = groundBlock;
            groundBlock = destination.getFace(BlockFace.DOWN);
            distanceDown++;
        }

        Block ledge = null;
        // Also check for a ledge above the target
        if (!targeting.isReverseTargeting())
        {
            ledge = target;
            Block inFront = face;
            Block oneUp = null;
            Block twoUp = null;

            do
            {
                oneUp = ledge.getFace(BlockFace.UP);
                twoUp = oneUp.getFace(BlockFace.UP);
                inFront = inFront.getFace(BlockFace.UP);
                ledge = ledge.getFace(BlockFace.UP);
                distanceUp++;
            }
            while (distanceUp < verticalSearchDistance && targeting.isOkToStandIn(inFront.getType()) && (!targeting.isOkToStandOn(groundBlock.getType()) || !targeting.isOkToStandIn(oneUp.getType()) || !targeting.isOkToStandIn(twoUp.getType())));

        }

        if (ledge != null && distanceUp < distanceDown)
        {
            destination = ledge;
        }

        Block oneUp = destination.getFace(BlockFace.UP);
        Block twoUp = oneUp.getFace(BlockFace.UP);
        if (!targeting.isOkToStandIn(oneUp.getType()) || !targeting.isOkToStandIn(twoUp.getType()))
        {
            castMessage(player, "You can't fit in there!");
            return false;
        }
        castMessage(player, "Blink!");
        player.teleport(new org.bukkit.Location(world, destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch()));
        return true;
    }

    @Override
    public void onLoad()
    {
        // maxRange = properties.getInteger("spells-blink-range", maxRange);
        // autoPassthrough =
        // properties.getBoolean("spells-blink-auto-passthrough",
        // autoPassthrough);
    }
}
