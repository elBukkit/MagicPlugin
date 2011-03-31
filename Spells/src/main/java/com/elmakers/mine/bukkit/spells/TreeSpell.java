package com.elmakers.mine.bukkit.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class TreeSpell extends Spell
{
    public static TreeType parseTreeString(String s, TreeType defaultTreeType)
    {
        if (s.equalsIgnoreCase("big"))
        {
            return TreeType.BIG_TREE;
        }
        if (s.equalsIgnoreCase("tall"))
        {
            return TreeType.TALL_REDWOOD;
        }

        TreeType tree = defaultTreeType;
        for (TreeType t : TreeType.values())
        {
            if (t.name().equalsIgnoreCase(s))
            {
                tree = t;
            }
        }
        return tree;
    }

    private final TreeType defaultTreeType = TreeType.TREE;

    private boolean        requireSapling  = false;

    @Override
    public String getDescription()
    {
        return "Creates a tree, or a big tree";
    }

    @Override
    public String getName()
    {
        return "tree";
    }

    public String getTreeName(TreeType treeType)
    {
        return treeType.name().toLowerCase();
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block target = targeting.getTargetBlock();

        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }

        if (requireSapling && target.getType() != Material.SAPLING)
        {
            castMessage(player, "Plant a sapling first");
            return false;
        }

        Location treeLoc = new Location(player.getWorld(), target.getX(), target.getY() + 1, target.getZ(), 0, 0);
        TreeType treeType = defaultTreeType;
        
        if (parameters.hasParameter("type"))
        {
            treeType = parseTreeString(parameters.getString("type", ""), defaultTreeType);
        }

        boolean result = player.getWorld().generateTree(treeLoc, treeType);

        if (result)
        {
            castMessage(player, "You grow a " + getTreeName(treeType) + " tree");
        }
        else
        {
            castMessage(player, "Your tree didn't grow");
        }
        return result;
    }

    @Override
    public void onLoad()
    {
        //requireSapling = properties.getBoolean("spells-tree-require-sapling", requireSapling);
    }
}
