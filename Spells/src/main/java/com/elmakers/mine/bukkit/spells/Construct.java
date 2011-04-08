package com.elmakers.mine.bukkit.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Construct extends Spell
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
    
    public void constructBlock(int x, int y, int z, MaterialData material, BlockList constructedBlocks, MaterialList destructable)
    {
        Block block = player.getWorld().getBlockAt(x, y, z);
        if (!destructable.contains(block.getType()))
        {
            return;
        }
        constructedBlocks.add(block);
        block.setType(material.getType());
        block.setData(material.getData());
    }

    public void fillArea(ConstructionType type, BoundingBox area, MaterialData material, boolean fill, MaterialList destructable)
    {
        BlockList constructedBlocks = new BlockList();
        BlockVector center = area.getCenter();
        int midX = center.getBlockX();
        int midY = center.getBlockY();
        int midZ = center.getBlockZ();
        int radius = area.getSizeX() / 2;
  
        for (int x = area.getMin().getBlockX(); x < area.getMax().getBlockX(); ++x)
        {
            for (int y = area.getMin().getBlockY(); x < area.getMax().getBlockY(); ++y)
            {
                for (int z = area.getMin().getBlockZ(); x < area.getMax().getBlockZ(); ++z)
                {
                    boolean fillBlock = false;

                    if (type == ConstructionType.SPHERE)
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
                        constructBlock(x, y, z, material, constructedBlocks, destructable);
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
    public boolean onCast(ParameterMap parameters)
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

        MaterialData material = getBuildingMaterial(parameters, target);
        
        ConstructionType conType = ConstructionType.parseString(parameters.getString("type", "unknown"), defaultConstructionType);       
        int radius = parameters.getInteger("radius", defaultRadius);
        boolean hollow = parameters.hasFlag("hollow");
        boolean selection = parameters.hasFlag("selection");
        ParameterData destructibleList = parameters.get("destroy");
        
        if (radius > maxRadius && maxRadius > 0)
        {
            radius = maxRadius;
        }

        MaterialList destructables = null;
        if (destructibleList != null)
        {
            destructables = destructibleList.getMaterialList();
        }
        else
        {
            destructables = new MaterialList();
            destructables.addAll(destructibleMaterials);
        }

        BoundingBox area = null;
        if (selection)
        {
            if (selectionStart == null)
            {
                castMessage(player,  "Cast again to complete construction");
                selectionStart = target;
                return true;
            }
            
            Location endLocation = target.getLocation();
            Location startLocation = selectionStart.getLocation();
            BlockVector min = new BlockVector(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
            BlockVector max = new BlockVector(endLocation.getBlockX(), endLocation.getBlockY(), endLocation.getBlockZ());
            area = new BoundingBox(min, max);
            
            if (maxDimension > 0 && (area.getSizeX() > maxDimension || area.getSizeY() > maxDimension || area.getSizeZ() > maxDimension))
            {
                player.sendMessage("Dimension is too big!");
                return false;
            }

            if (maxVolume > 0 && area.getSizeX() * area.getSizeY() * area.getSizeZ() > maxVolume)
            {
                player.sendMessage("Volume is too big!");
                return false;
            }
        }
        else
        {
            Location location = target.getLocation();
            BlockVector min = new BlockVector(location.getBlockX() - radius, location.getBlockY() - radius, location.getBlockZ() - radius);
            BlockVector max = new BlockVector(location.getBlockX() + radius, location.getBlockY() + radius, location.getBlockZ() + radius);
            area = new BoundingBox(min, max);
        }

        fillArea(conType, area, material, !hollow, destructables);

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
        //maxDimension = properties.getInteger("spells-fill-max-dimension", maxDimension);
        //maxVolume = properties.getInteger("spells-fill-max-volume", maxVolume);
     }

}
