package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

@Deprecated
public class TreeSpell extends TargetingSpell
{
	private TreeType lastTreeType = null;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block target = getTargetBlock();
		lastTreeType = null;
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

		boolean requireSapling = parameters.getBoolean("require_sapling", false);
		if (requireSapling && target.getType() != Material.SAPLING)
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Location treeLoc = new Location(getWorld(), target.getX(), target.getY() + 1, target.getZ(), 0, 0);
		TreeType treeType;
		String typeString = parameters.getString("type", "");
		treeType = parseTreeString(typeString, null);

		if (treeType == null)
		{
			treeType = TreeType.values()[(int)(Math.random() * TreeType.values().length)];
		}
		boolean result = getWorld().generateTree(treeLoc, treeType);

		if (result)
		{
			controller.updateBlock(target);
			lastTreeType = treeType;
		}
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
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		return message.replace("$tree", getTreeName(lastTreeType));
	}
}
