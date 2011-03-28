package com.elmakers.mine.bukkit.magic;

import org.bukkit.Material;

public class PlayerSpells
{
    private byte     data     = 0;
    private Material material = Material.AIR;
    private boolean  usingMaterial;

    public Material finishMaterialUse()
    {
        usingMaterial = false;
        return material;
    }

    public byte getData()
    {
        return data;
    }

    public Material getMaterial()
    {
        return material;
    }

    public boolean isUsingMaterial()
    {
        return usingMaterial;
    }

    public void setData(byte d)
    {
        data = d;
    }

    public void setMaterial(Material mat)
    {
        material = mat;
    }

    public void startMaterialUse(Material mat, byte data)
    {
        setMaterial(mat);
        setData(data);
        usingMaterial = true;
    }

}
