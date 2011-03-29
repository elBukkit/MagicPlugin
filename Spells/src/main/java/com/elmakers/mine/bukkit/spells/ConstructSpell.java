package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;

public class ConstructSpell extends Spell
{
    enum ConstructionType
    {
        CUBOID, SPHERE, UNKNOWN;

        public static ConstructionType parseString(String s, ConstructionType defaultType)
        {
            ConstructionType construct = defaultType;
            for (ConstructionType t : ConstructionType.values())
            {
                if (t.name().equalsIgnoreCase(s))
                {
                    construct = t;
                }
            }
            return construct;
        }
    }

    private final BlockRecurse blockRecurse            = new BlockRecurse();
    private ConstructionType   defaultConstructionType = ConstructionType.SPHERE;
    private int                defaultRadius           = 2;
    private int                defaultSearchDistance   = 32;
    private MaterialList       destructibleMaterials   = new MaterialList();
    private Block              selectionStart          = null;

    private int                maxDimension            = 128;
    private int                maxVolume               = 512;
    private int                maxRadius               = 32;

    @Override
    public void onCancel()
    {
        if (selectionStart != null)
        {
            sendMessage(player, "Cancelled selection");
            selectionStart = null;
        }
    }
    
    public void constructBlock(int dx, int dy, int dz, Block centerPoint, int radius, Material material, byte data, BlockList constructedBlocks, MaterialList destructable)
    {
        int x = centerPoint.getX() + dx - radius;
        int y = centerPoint.getY() + dy - radius;
        int z = centerPoint.getZ() + dz - radius;
        Block block = player.getWorld().getBlockAt(x, y, z);
        if (!destructable.contains(block.getType()))
        {
            return;
        }
        constructedBlocks.add(block);
        block.setType(material);
        block.setData(data);
    }

    public void constructCuboid(Block target, int radius, Material material, byte data, boolean fill, MaterialList destructable)
    {
        fillArea(target, radius, material, data, fill, false, destructable);
    }

    public void constructSphere(Block target, int radius, Material material, byte data, boolean fill, MaterialList destructable)
    {
        fillArea(target, radius, material, data, fill, true, destructable);
    }

    public void fillArea(Block target, int radius, Material material, byte data, boolean fill, boolean sphere, MaterialList destructable)
    {
        BlockList constructedBlocks = new BlockList();
        int diameter = radius * 2;
        int midX = (diameter - 1) / 2;
        int midY = (diameter - 1) / 2;
        int midZ = (diameter - 1) / 2;
        int diameterOffset = diameter - 1;

        for (int x = 0; x < radius; ++x)
        {
            for (int y = 0; y < radius; ++y)
            {
                for (int z = 0; z < radius; ++z)
                {
                    boolean fillBlock = false;

                    if (sphere)
                    {
                        int distance = getDistance(x - midX, y - midY, z - midZ);
                        fillBlock = distance <= radius;
                        if (!fill)
                        {
                            fillBlock = fillBlock && distance >= radius - 1;
                        }
                    }
                    else
                    {
                        fillBlock = fill ? true : x == 0 || y == 0 || z == 0;
                    }
                    if (fillBlock)
                    {
                        constructBlock(x, y, z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(diameterOffset - x, y, z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(x, diameterOffset - y, z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(x, y, diameterOffset - z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(diameterOffset - x, diameterOffset - y, z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(diameterOffset - x, y, diameterOffset - z, target, radius, material, data, constructedBlocks, destructable);
                        constructBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks, destructable);
                    }
                }
            }
        }

        magic.addToUndoQueue(player, constructedBlocks);
        castMessage(player, "Constructed " + constructedBlocks.size() + "blocks");
    }

    @Override
    public String getDescription()
    {
        return "Add some blocks to your target";
    }

    public int getDistance(int x, int y, int z)
    {
        return (int) (Math.sqrt(x * x + y * y + z * z) + 0.5);
    }

    @Override
    public String getName()
    {
        return "blob";
    }

    public boolean isDestructible(Block block)
    {
        if (block.getType() == Material.AIR)
        {
            return true;
        }

        return destructibleMaterials.contains(block.getType());
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        targeting.setMaxRange(defaultSearchDistance, true);
        targeting.targetThrough(Material.GLASS);
        Block target = targeting.getTargetBlock();
        if (target == null)
        {
            targeting.reset();
            targeting.noTargetThrough(Material.GLASS);
            target = targeting.getTargetBlock();
            if (target == null)
            {
                castMessage(player, "No target");
                return false;
            }
        }

        Material material = target.getType();
        byte data = target.getData();

        ItemStack buildWith = getBuildingMaterial();
        if (buildWith != null)
        {
            MaterialData md = new MaterialData(buildWith);
            material = md.getType();
            data = md.getData();
        }

        ConstructionType conType = defaultConstructionType;
        MaterialList destructables = new MaterialList();
        destructables.addAll(destructibleMaterials);
        
        int radius = defaultRadius;
        boolean hollow = false;

        for (ParameterData parameter : parameters)
        {
            if (parameter.isFlag("hollow"))
            {
                hollow = true;
                continue;
            }
            
            if (parameter.isFlag("selection"))
            {
                hollow = true;
                continue;
            }

            if (parameter.isMatch("with"))
            {
                material = parameter.getMaterial();
                continue;
            }
            
            if (parameter.isMatch("radius"))
            {
                radius = parameter.getInteger();
                if (radius > maxRadius && maxRadius > 0)
                {
                    radius = maxRadius;
                }
                continue;
            }

            if (parameter.isMatch("type"))
            {
                ConstructionType testType = ConstructionType.parseString(parameter.getValue(), ConstructionType.UNKNOWN);
                if (testType != ConstructionType.UNKNOWN)
                {
                    conType = testType;
                }
                continue;
            }

            if (parameter.isMatch("destroy"))
            {
                destructables = parameter.getMaterialList();
                continue;
            }
        }

        switch (conType)
        {
            case SPHERE:
                constructSphere(target, radius, material, data, !hollow, destructables);
                break;
            case CUBOID:
                constructCuboid(target, radius, material, data, !hollow, destructables);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onLoad()
    {
        destructibleMaterials = getMaterialList("common");
        //defaultConstructionType = ConstructionType.parseString(properties.getString("spells-construct-default", ""), defaultConstructionType);
        //defaultRadius = properties.getInteger("spells-construct-radius", defaultRadius);
        //maxRadius = properties.getInteger("spells-construct-max-radius", maxRadius);
        //defaultSearchDistance = properties.getInteger("spells-constructs-search-distance", defaultSearchDistance);
    }

}
