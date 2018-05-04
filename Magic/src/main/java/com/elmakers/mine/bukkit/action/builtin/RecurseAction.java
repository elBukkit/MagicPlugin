package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.BlockFace;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.DirectionUtils;

public class RecurseAction extends CompoundAction {
    protected int recursionDepth;
    protected List<BlockFace> directions;
    protected Set<MaterialAndData> replaceable = null;
    protected boolean checker;
    protected boolean replace;

    private static class StackEntry {
        public Block block;
        public int face;

        public StackEntry(Block block) {
            this.block = block;
            this.face = 0;
        }

        public StackEntry(Block block, int face) {
            this.block = block;
            this.face = face;
        }
    }

    protected Deque<StackEntry> stack;
    protected Set<Long> touched;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        touched.clear();
        stack.clear();
        if (checker) {
            stack.add(new StackEntry(context.getTargetBlock(), 0));
        } else {
            stack.add(new StackEntry(context.getTargetBlock(), -1));
        }
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        touched.clear();
        stack.clear();
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        directions = DirectionUtils.getDirections(parameters, "faces");
        replaceable = null;
        touched = new HashSet<>();
        stack = new ArrayDeque<>();
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        checker = parameters.getBoolean("checkered", false);
        replace = parameters.getBoolean("replace", false);
        recursionDepth = parameters.getInt("size", 32);
        recursionDepth = parameters.getInt("depth", recursionDepth);

        if (replace) {
            if (replaceable == null) {
                replaceable = new HashSet<>();
            }
            Block targetBlock = context.getTargetBlock();
            replaceable.clear();
            if (targetBlock == null) {
                return;
            }
            MaterialAndData targetMaterialAndData = new MaterialAndData(targetBlock);
            if (targetMaterialAndData.isValid()) {
                replaceable.add(targetMaterialAndData);
            }
            Material targetMaterial = targetBlock.getType();
            if (parameters.getBoolean("auto_water", true))
            {
                if (DefaultMaterials.isWater(targetMaterial))
                {
                    for (Material material : DefaultMaterials.getWater()) {
                        for (byte i = 0; i < 15; i++) {
                            replaceable.add(new MaterialAndData(material, i));
                        }
                    }
                }
            }
            if (parameters.getBoolean("auto_lava", true))
            {
                if (DefaultMaterials.isLava(targetMaterial))
                {
                    for (Material material : DefaultMaterials.getLava()) {
                        for (byte i = 0; i < 15; i++) {
                            replaceable.add(new MaterialAndData(material, i));
                        }
                    }
                }
            }
            if (parameters.getBoolean("auto_snow", true))
            {
                if (targetMaterial == Material.SNOW) {
                    for (byte i = 0; i < 15; i++) {
                        replaceable.add(new MaterialAndData(Material.SNOW, i));
                    }
                }
            }
        } else {
            replaceable = null;
        }
    }

    @Override
    public boolean next(CastContext context) {
        StackEntry current = stack.peek();
        while (!stack.isEmpty() && current.face >= directions.size()) {
            stack.pop();
            current = stack.peek();
        }
        return !stack.isEmpty();
    }

    @Override
    public SpellResult step(CastContext context)
    {
        StackEntry current = stack.peek();
        Block block = current.block;
        int faceIndex = current.face++;
        if (faceIndex >= 0) {
            block = directions.get(faceIndex).getRelative(block);
        }

        long id = BlockData.getBlockId(block);
        if (touched.contains(id))
        {
            return SpellResult.NO_TARGET;
        }
        if (!context.isDestructible(block))
        {
            return SpellResult.NO_TARGET;
        }
        if (replaceable != null && !replaceable.contains(new MaterialAndData(block)))
        {
            return SpellResult.NO_TARGET;
        }
        if (faceIndex >= 0 && stack.size() <= recursionDepth) {
            if (checker) {
                BlockFace direction = directions.get(faceIndex);
                block = direction.getRelative(block);
            }
            stack.push(new StackEntry(block));
        }

        touched.add(id);
        actionContext.setTargetLocation(block.getLocation());
        actionContext.playEffects("recurse");
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("depth");
        parameters.add("size");
        parameters.add("faces");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("faces")) {
            examples.addAll(Arrays.asList((DirectionUtils.EXAMPLE_DIRECTIONS)));
        } else if (parameterKey.equals("depth") || parameterKey.equals("size")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public int getActionCount() {
        return recursionDepth * super.getActionCount();
    }
}