package com.elmakers.mine.bukkit.spells;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class ManifestSpell extends Spell
{
    private int          defaultAmount = 1;
    private final Random rand          = new Random();

    @Override
    public String getDescription()
    {
        return "Give yourself some of a material";
    }

    @Override
    public String getName()
    {
        return "manifest";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Material material = parameters.getMaterial("type", Material.AIR);
        int amount = parameters.getInteger("amount", defaultAmount);
        List<Material> buildingMaterials = magic.getBuildingMaterials();

        if (material == Material.AIR)
        {
            material = buildingMaterials.get(rand.nextInt(buildingMaterials.size()));
        }

        byte data = 0;
        castMessage(player, "Manifesting some " + material.name().toLowerCase());
        return giveMaterial(material, amount, (short) 0, data);
    }

    @Override
    public void onLoad()
    {
        //defaultAmount = properties.getInteger("spells-manifest-amount", defaultAmount);
    }

}
