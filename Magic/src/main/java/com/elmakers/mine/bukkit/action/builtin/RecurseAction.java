package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    protected int limit;
    protected List<BlockFace> directions;
    protected Set<BlockFace> priority;
    protected Set<MaterialAndData> replaceable = null;
    protected boolean checker;
    protected boolean replace;
    protected boolean depthFirst;
    protected boolean debugDepth;
    protected List<MaterialAndData> debugMaterials;

    private static class StackEntry {
        public Block block;
        public int face;
        public int depth;

        public StackEntry(Block block, int depth) {
            this.block = block;
            this.face = 0;
            this.depth = depth;
        }

        public StackEntry(Block block, int face, int depth) {
            this.block = block;
            this.face = face;
            this.depth = depth;
        }
    }

    protected Deque<StackEntry> stack;
    protected Deque<StackEntry> prioritized;
    protected StackEntry current;
    protected Set<Long> touched;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        touched.clear();
        stack.clear();
        if (prioritized != null) {
            prioritized.clear();
        }
        if (checker) {
            current = new StackEntry(context.getTargetBlock(), 0, 0);
        } else {
            current = new StackEntry(context.getTargetBlock(), -1, 0);;
        }
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        touched.clear();
        stack.clear();
        if (prioritized != null) {
            prioritized.clear();
        }
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        // Parsing this parameter here to pre-prime the stack
        recursionDepth = parameters.getInt("size", 32);
        recursionDepth = parameters.getInt("depth", recursionDepth);

        directions = DirectionUtils.getDirections(parameters, "faces");
        if (parameters.contains("priority_faces")) {
            priority = new HashSet<>(DirectionUtils.getDirections(parameters, "priority_faces"));
            prioritized = new ArrayDeque<>(recursionDepth + 10);
        }

        replaceable = null;
        touched = new HashSet<>();
        stack = new ArrayDeque<>(recursionDepth + 10);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        checker = parameters.getBoolean("checkered", false);
        replace = parameters.getBoolean("replace", false);
        depthFirst = parameters.getBoolean("depth_first", false);
        recursionDepth = parameters.getInt("size", 32);
        recursionDepth = parameters.getInt("depth", recursionDepth);
        limit = parameters.getInt("limit", 0);

        debugDepth = parameters.getBoolean("debug_depth", true);
        String debugKey = parameters.getString("debug_material");
        if (debugKey != null && !debugKey.isEmpty()) {
            Material baseMaterial = Material.getMaterial(debugKey.toUpperCase());
            if (baseMaterial != null) {
                debugMaterials = new ArrayList<>(DefaultMaterials.getColorBlocks(baseMaterial));
                Collections.sort(debugMaterials, new Comparator<MaterialAndData>() {
                    @Override
                    public int compare(MaterialAndData o1, MaterialAndData o2) {
                        if (o1.getMaterial() == o2.getMaterial()) {
                            return o1.getData() - o2.getData();
                        }
                        return o1.getMaterial().name().compareTo(o2.getMaterial().name());
                    }
                });
            }
        }

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
        current.face++;

        if (current.face >= directions.size()) {
            if (stack.isEmpty()) {
                if (prioritized != null && !prioritized.isEmpty()) {
                    current = prioritized.pop();
                } else {
                    return false;
                }
            } else {
                current = stack.pop();
            }
        }
        return limit == 0 || touched.size() < limit;
    }

    @Override
    public SpellResult step(CastContext context)
    {
        Block block = current.block;
        Block originalBlock = block;
        int faceIndex = current.face;
        BlockFace direction = null;
        if (faceIndex >= 0) {
            direction = directions.get(faceIndex);
            block = direction.getRelative(block);
        }
        boolean prioritize = !depthFirst && priority != null && direction != null && priority.contains(direction);
        Deque<StackEntry> queue = prioritize ? prioritized : stack;

        if (!context.isDestructible(block))
        {
            return SpellResult.NO_TARGET;
        }
        long id = BlockData.getBlockId(block);
        if (debugMaterials != null && !debugDepth) {
            context.registerForUndo(originalBlock);
            debugMaterials.get(faceIndex + 1).modify(originalBlock);
        }
        if (touched.contains(id))
        {
            return SpellResult.NO_TARGET;
        }
        if (current.depth > recursionDepth) {
            // Prevent blocks that get isolated due to not quite being reached from all 4 directions
            Block nextBlock = direction == null ? null : direction.getRelative(block);
            if (nextBlock != null && touched.contains(BlockData.getBlockId(nextBlock))) {
                if (debugMaterials != null && !debugDepth) {
                    context.registerForUndo(block);
                    debugMaterials.get(debugMaterials.size() - 2).modify(block);
                }

                return startActions();
            }
            if (debugMaterials != null && !debugDepth) {
                context.registerForUndo(block);
                debugMaterials.get(debugMaterials.size() - 1).modify(block);
            }
            return SpellResult.NO_TARGET;
        }
        if (debugMaterials != null) {
            context.registerForUndo(block);
            if (debugDepth) {
                debugMaterials.get(current.depth % debugMaterials.size()).modify(block);
            } else {
                debugMaterials.get(0).modify(block);
            }
        }
        if (replaceable != null && !replaceable.contains(new MaterialAndData(block)))
        {
            return SpellResult.NO_TARGET;
        }
        if (faceIndex >= 0) {
            if (checker && direction != null) {
                block = direction.getRelative(block);
            }
            if (prioritize) {
                queue.push(current);
                queue.addAll(stack);
                stack.clear();
                current = new StackEntry(block, -1, current.depth + 1);
            } else if (depthFirst) {
                queue.push(current);
                current = new StackEntry(block, -1, current.depth + 1);
            } else {
                queue.add(new StackEntry(block, current.depth + 1));
            }
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
        parameters.add("depth_first");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("faces")) {
            examples.addAll(Arrays.asList(DirectionUtils.EXAMPLE_DIRECTIONS));
        } else if (parameterKey.equals("depth") || parameterKey.equals("size")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("depth_first")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public int getActionCount() {
        return recursionDepth * super.getActionCount();
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}