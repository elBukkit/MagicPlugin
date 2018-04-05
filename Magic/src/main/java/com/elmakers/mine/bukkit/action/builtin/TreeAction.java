package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class TreeAction extends BaseSpellAction
{
    private TreeType treeType = null;

    private boolean requireSapling;
    private Map<Biome, List<TreeType>> biomeMap = null;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        if (parameters.contains("biomes"))
        {
            ConfigurationSection biomeConfig = ConfigurationUtils.getConfigurationSection(parameters, "biomes");
            biomeMap = new HashMap<>();
            Collection<String> biomeKeys = biomeConfig.getKeys(false);
            for (String biomeKey : biomeKeys)
            {
                try {
                    Biome biome = Biome.valueOf(biomeKey.toUpperCase());
                    List<String> treeTypes = ConfigurationUtils.getStringList(biomeConfig, biomeKey);
                    for (String typeKey : treeTypes)
                    {
                        try {
                            TreeType treeType = TreeType.valueOf(typeKey.toUpperCase());
                            List<TreeType> biomeTypes = biomeMap.get(biome);
                            if (biomeTypes == null) {
                                biomeTypes = new ArrayList<>();
                                biomeMap.put(biome, biomeTypes);
                            }
                            biomeTypes.add(treeType);
                        } catch (Exception treeEx) {
                            Bukkit.getLogger().warning("Invalid tree type: " + typeKey);
                        }
                    }
                } catch (Exception biomeEx) {
                    Bukkit.getLogger().warning("Invalid biome: " + biomeKey);
                }
            }
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        treeType = null;
        requireSapling = parameters.getBoolean("require_sapling", false);
        String typeString = parameters.getString("type", "");
        treeType = parseTreeString(typeString, null);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block target = context.getTargetBlock();
        TreeType useType = null;
        if (requireSapling)
        {
            switch (target.getType()) {
                case SPRUCE_SAPLING:
                    useType = TreeType.REDWOOD;
                    break;
                case ACACIA_SAPLING:
                    useType = TreeType.ACACIA;
                    break;
                case BIRCH_SAPLING:
                    useType = TreeType.BIRCH;
                    break;
                case DARK_OAK_SAPLING:
                    useType = TreeType.DARK_OAK;
                    break;
                case JUNGLE_SAPLING:
                    useType = TreeType.JUNGLE;
                    break;
                case OAK_SAPLING:
                    useType = TreeType.TREE;
                    break;
                default:
                    return SpellResult.NO_TARGET;
            }
        }
        if (!context.hasBuildPermission(target))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        World world = context.getWorld();
        Location treeLoc = new Location(world, target.getX(), target.getY() + 1, target.getZ(), 0, 0);

        Random random = context.getRandom();
        if (treeType != null)
        {
            useType = treeType;
        }
        else
        if (biomeMap != null)
        {
            Biome biome = treeLoc.getWorld().getBiome(treeLoc.getBlockX(), treeLoc.getBlockZ());
            List<TreeType> types = biomeMap.get(biome);
            if (types != null)
            {
                useType = types.get(random.nextInt(types.size()));
            }
        }
        if (useType == null)
        {
            useType = TreeType.values()[random.nextInt(TreeType.values().length)];
        }
        UndoList restoreOnFail = new UndoList(context.getMage(), context.getSpell().getName());
        Block treeBlock = treeLoc.getBlock();
        if (!context.isDestructible(treeBlock))
        {
            return SpellResult.NO_TARGET;
        }
        restoreOnFail.add(treeBlock);
        treeLoc.getBlock().setType(Material.AIR);
        boolean result = world.generateTree(treeLoc, useType);
        if (!result) {
            UndoList undoList = new UndoList(context.getMage(), context.getSpell().getName());
            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {
                    Block clearBlock = treeBlock.getRelative(x, 0, z);
                    Block lowerBlock = clearBlock.getRelative(BlockFace.DOWN);
                    if (context.isDestructible(clearBlock) && lowerBlock.getType() != target.getType())
                    {
                        undoList.add(lowerBlock);
                        lowerBlock.setType(target.getType());
                    }
                    if (x == 0 && z == 0) continue;
                    if (!context.isDestructible(clearBlock)) continue;
                    restoreOnFail.add(clearBlock);
                    clearBlock.setType(Material.AIR);
                }
            }
            result = world.generateTree(treeLoc, useType);
            context.addWork(100);
            undoList.undo(true);
        }
        if (result) {
            context.addWork(500);
        } else {
            context.addWork(100);
            restoreOnFail.undo(true);
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

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("requires_sapling");
        parameters.add("type");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("type")) {
            for (TreeType type : TreeType.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("requires_sapling")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
