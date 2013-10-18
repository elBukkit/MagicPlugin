package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.MaterialList;

public class ReplaceMaterialAction extends SimpleBlockAction
{
    protected Material replace;
    protected byte replaceData;
    protected MaterialList replaceable = new MaterialList();

    public ReplaceMaterialAction(Block targetBlock, Material replaceMaterial, byte replaceData)
    {
        replaceable.add(targetBlock.getType());
        replace = replaceMaterial;
        this.replaceData = replaceData;
    }

    public ReplaceMaterialAction(Material replaceMaterial, byte replaceData)
    {
        replace = replaceMaterial;
        this.replaceData = replaceData;
    }

    public void addReplaceable(Material material)
    {
        replaceable.add(material);
    }

    @SuppressWarnings("deprecation")
	public boolean perform(Block block)
    {
        if (replace == null)
        {
            return false;
        }

        if (replaceable == null || replaceable.contains(block.getType()))
        {
            block.setType(replace);
            block.setData(replaceData);
            super.perform(block);
            return true;
        }

        return false;
    }
}
