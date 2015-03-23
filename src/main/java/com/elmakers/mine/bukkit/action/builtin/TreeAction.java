package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class TreeAction extends BaseSpellAction
{
	private TreeType treeType = null;

    private boolean requireSapling;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        treeType = null;
        requireSapling = parameters.getBoolean("require_sapling", false);
        String typeString = parameters.getString("type", "");
        treeType = parseTreeString(typeString, null);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
		Block target = context.getTargetBlock();
		if (requireSapling && target.getType() != Material.SAPLING)
		{
			return SpellResult.NO_TARGET;
		}
		if (!context.hasBuildPermission(target))
        {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

        World world = context.getWorld();
		Location treeLoc = new Location(world, target.getX(), target.getY() + 1, target.getZ(), 0, 0);

		if (treeType == null)
		{
			treeType = TreeType.values()[(int)(Math.random() * TreeType.values().length)];
		}
		boolean result = world.generateTree(treeLoc, treeType);
		return result ? SpellResult.CAST : SpellResult.FAIL;
	}

	public String getTreeName(TreeType treeType)
	{
		if (treeType == null || treeType.name() == null) return "Tree";
		return treeType.name().toLowerCase();
	}

	public static TreeType parseTreeString(String s, TreeType defaultTreeType)
	{
		if (s.equalsIgnoreCase("big")) return TreeType.BIG_TREE;
		if (s.equalsIgnoreCase("tall")) return TreeType.TALL_REDWOOD;

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

    @Override
    public String transformMessage(String message) {
        return message.replace("$tree", getTreeName(treeType));
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }
}
