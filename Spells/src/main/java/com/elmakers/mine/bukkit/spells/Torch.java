package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Torch extends Spell
{
    private boolean allowLightstone = true;
 
    @Override
    public String getDescription()
    {
        return "Place a torch at your target";
    }

    @Override
    public String getName()
    {
        return "torch";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block target = targeting.getTargetBlock();
        Block face = targeting.getLastBlock();

        if (target == null || face == null)
        {
            castMessage(player, "No target");
            return false;
        }

        boolean isAir = face.getType() == Material.AIR;
        boolean isAttachmentSlippery = target.getType() == Material.GLASS || target.getType() == Material.ICE;
        boolean replaceAttachment = target.getType() == Material.SNOW || target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
        boolean isWater = target.getType() == Material.STATIONARY_WATER || target.getType() == Material.WATER;
        boolean isNether = target.getType() == Material.NETHERRACK || target.getType() == Material.SOUL_SAND;
        Material targetMaterial = Material.TORCH;

        if (isWater || isAttachmentSlippery || isNether)
        {
            targetMaterial = Material.GLOWSTONE;
        }

        if (face == null || !isAir && !isWater || targetMaterial == Material.GLOWSTONE && !allowLightstone)
        {
            player.sendMessage("Can't put a torch there");
            return false;
        }

        if (!replaceAttachment)
        {
            target = face;
        }

        castMessage(player, "Flame on!");
        BlockList torchBlock = new BlockList();
        target.setType(targetMaterial);
        torchBlock.add(target);
        magic.addToUndoQueue(player, torchBlock);

        return true;
    }

    @Override
    public void onLoad()
    {
        //allowLightstone = properties.getBoolean("spells-torch-allow-lightstone", allowLightstone);
    }
}
