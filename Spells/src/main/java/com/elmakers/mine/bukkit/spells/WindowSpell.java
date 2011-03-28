package com.elmakers.mine.bukkit.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.utilities.BlockRequestListener;

public class WindowSpell extends Spell implements BlockRequestListener
{
    static final String    DEFAULT_PEEKABLES     = "1,2,3,10,11,12,13";

    private int            defaultRadius         = 3;
    private int            defaultSearchDistance = 32;
    private int            maxRadius             = 32;
    private NetherManager  nether                = null;

    private List<Material> peekableMaterials     = new ArrayList<Material>();
    private int            radius                = defaultRadius;
    private Block          targetBlock           = null;

    public WindowSpell(NetherManager nether)
    {
        this.nether = nether;
    }

    public int checkPosition(int x, int y, int z, int R)
    {
        return x * x + y * y + z * z - R * R;
    }

    @Override
    public String getCategory()
    {
        return "nether";
    }

    @Override
    public String getDescription()
    {
        return "Create a temporary window into other worlds";
    }

    @Override
    public Material getMaterial()
    {
        return Material.MOB_SPAWNER;
    }

    @Override
    protected String getName()
    {
        return "window";
    }

    public boolean isWindowable(Block block)
    {
        if (block.getType() == Material.AIR)
        {
            return true;
        }

        if (block.getType() == Material.GLASS)
        {
            return false;
        }

        return peekableMaterials.contains(block.getType());
    }

    public void onBlockListLoaded(List<Block> blocks)
    {
        BlockList peekedBlocks = peek(targetBlock, radius, blocks);
        targetBlock = null;
        if (peekedBlocks == null)
        {
            return;
        }
        spells.scheduleCleanup(peekedBlocks);

        castMessage(player, "Windowed through  " + peekedBlocks.size() + "blocks");
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        targetThrough(Material.GLASS);
        Block target = getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
        {
            castMessage(player, "Can't peek that far away");
            return false;
        }

        String worldName = null;
        radius = defaultRadius;

        for (int i = 0; i < parameters.length; i++)
        {
            String parameter = parameters[i];
            if (parameter.equalsIgnoreCase("world"))
            {
                if (i < parameters.length - 1)
                {
                    worldName = parameters[i + 1];
                }
            }
            // Try for number
            try
            {
                int r = Integer.parseInt(parameter);
                radius = r;
                if (radius > maxRadius && maxRadius > 0)
                {
                    radius = maxRadius;
                }
            }
            catch (NumberFormatException ex)
            {
            }
        }

        if (targetBlock == null)
        {
            targetBlock = target;

            nether.requestBlockList(player.getWorld(), worldName, new BlockVector(target.getX(), target.getY(), target.getZ()), radius, this);
        }
        else
        {
            sendMessage(player, "You must wait for your previous window");
            return false;
        }

        return true;
    }

    @Override
    public void onLoad(PluginProperties properties)
    {
        peekableMaterials = properties.getMaterials("spells-peek-peekable", DEFAULT_PEEKABLES);
        defaultRadius = properties.getInteger("spells-peek-radius", defaultRadius);
        maxRadius = properties.getInteger("spells-peek-max-radius", maxRadius);
        defaultSearchDistance = properties.getInteger("spells-peek-search-distance", defaultSearchDistance);
    }

    protected BlockList peek(Block target, int radius, List<Block> blocks)
    {
        BlockList peekedBlocks = new BlockList();
        int diameter = radius * 2;

        // Sanity check
        if (blocks.size() != diameter * diameter * diameter)
        {
            return null;
        }

        for (int x = 0; x < diameter; ++x)
        {
            for (int y = 0; y < diameter; ++y)
            {
                for (int z = 0; z < diameter; ++z)
                {
                    Material mat = Material.GLASS;
                    if (blocks != null)
                    {
                        mat = blocks.get(x + y * diameter + z * diameter * diameter).getType();
                    }

                    windowBlock(x, y, z, target, radius, peekedBlocks, mat);
                }
            }

        }

        peekedBlocks.setTimeToLive(8000);

        return peekedBlocks;
    }

    public void windowBlock(int dx, int dy, int dz, Block centerPoint,
            int radius, BlockList blocks, Material mat)
    {
        int x = centerPoint.getX() + dx - radius;
        int y = centerPoint.getY() + dy - radius;
        int z = centerPoint.getZ() + dz - radius;
        Block block = player.getWorld().getBlockAt(x, y, z);
        if (!isWindowable(block))
        {
            return;
        }
        blocks.add(block);
        block.setType(mat);
    }

}
