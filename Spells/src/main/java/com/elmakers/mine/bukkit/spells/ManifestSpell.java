package com.elmakers.mine.bukkit.spells;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;

public class ManifestSpell extends Spell
{
    private int          defaultAmount = 1;
    private final Random rand          = new Random();

    @Override
    public String getCategory()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return "Give yourself some of a material";
    }

    @Override
    public Material getMaterial()
    {
        return Material.DIAMOND;
    }

    @Override
    protected String getName()
    {
        return "manifest";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        Material material = Material.AIR;
        List<Material> buildingMaterials = spells.getBuildingMaterials();

        if (parameters.length > 0)
        {
            String matName = "";
            for (String parameter : parameters)
            {
                matName = matName + parameter;
            }
            material = getMaterial(matName, buildingMaterials);
            if (material == Material.AIR)
            {
                castMessage(player, "Uknown material '" + matName + "'");
                return false;
            }
        }

        if (material == Material.AIR)
        {
            material = buildingMaterials.get(rand.nextInt(buildingMaterials.size()));
        }

        int amount = defaultAmount;
        byte data = 0;
        castMessage(player, "Manifesting some " + material.name().toLowerCase());
        return giveMaterial(material, amount, (short) 0, data);
    }

    @Override
    public void onLoad(PluginProperties properties)
    {
        defaultAmount = properties.getInteger("spells-manifest-amount", defaultAmount);
    }

}
