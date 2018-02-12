package com.elmakers.mine.bukkit.spell;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.magic.MaterialSets;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class BlockSpell extends UndoableSpell {

    private @Nonnull MaterialSet     indestructible = MaterialSets.empty();
    private MaterialSet     destructible;
    private MaterialSet     destructibleOverride;
    protected boolean       checkDestructible       = true;
    protected float         destructibleDurability  = 0.0f;

    public final static String[] BLOCK_PARAMETERS = {
        "indestructible", "destructible", "check_destructible", "bypass_undo", "undo", "destructible_durability"
    };

    public boolean isIndestructible(Block block)
    {
        if (mage.isSuperPowered()) return false;
        Player player = mage.getPlayer();
        if (player != null && player.hasPermission("Magic.bypass")) return false;
        if (controller.isLocked(block)) return true;
        return indestructible.testBlock(block) || mage.isIndestructible(block);
    }

    public boolean isDestructible(Block block)
    {
        if (isIndestructible(block)) return false;

        if (!checkDestructible) return true;
        if (destructibleOverride != null && destructibleOverride.testBlock(block)) return true;
        if (destructibleDurability > 0 && CompatibilityUtils.getDurability(block.getType()) > destructibleDurability) return false;
        if (targetBreakables > 0 && currentCast.isBreakable(block)) return true;
        if (destructible == null) {
            return mage.isDestructible(block);
        }
        return destructible.testBlock(block);
    }

    @Nonnull
    public MaterialSet getDestructible() {
        if (destructible != null) return destructible;
        return controller.getDestructibleMaterialSet();
    }

    public boolean areAnyDestructible(Block block)
    {
        if (isIndestructible(block)) return false;

        if (!checkDestructible) return true;
        if (targetBreakables > 0 && currentCast.isBreakable(block)) return true;
        MaterialSet allDestructible = destructible;
        if (allDestructible == null) {
            allDestructible = controller.getDestructibleMaterialSet();
        }
        if (allDestructible == null) {
            return true;
        }
        if (allDestructible.testBlock(block)) return true;
        com.elmakers.mine.bukkit.api.block.BlockData blockData = UndoList.getBlockData(block.getLocation());
        if (blockData == null || !blockData.containsAny(allDestructible)) {
            return false;
        }
        return true;
    }

    protected void addDestructible(MaterialAndData material) {
        MaterialSet current = getDestructible();
        destructible = MaterialSets.union(current, material);
    }

    @Override
    public void processParameters(ConfigurationSection parameters) {
        super.processParameters(parameters);

        MaterialSetManager materials = controller.getMaterialSetManager();
        indestructible = MaterialSets.empty();
        indestructible = materials.fromConfig( // Legacy
                parameters.getString("id"),
                indestructible);
        indestructible = materials.fromConfig(
                parameters.getString("indestructible"),
                indestructible);

        destructible = materials.fromConfig(parameters.getString("destructible"));

        if (parameters.getBoolean("destructible_override", false)) {
            String destructibleKey = controller.getDestructibleMaterials(mage, mage.getLocation());
            destructibleOverride = destructibleKey == null ? null : materials.fromConfig(destructibleKey);
        } else {
            destructibleOverride = null;
        }

        checkDestructible = parameters.getBoolean("check_destructible", true);
        checkDestructible = parameters.getBoolean("cd", checkDestructible);
        destructibleDurability = (float)parameters.getDouble("destructible_durability", 0.0);
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        return message.replace("$count", Integer.toString(getModifiedCount()));
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        super.getParameters(parameters);
        parameters.addAll(Arrays.asList(BLOCK_PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("undo")) {
            examples.addAll(Arrays.asList(EXAMPLE_DURATIONS));
        }
        else if (parameterKey.equals("indestructible") || parameterKey.equals("destructible")) {
            examples.addAll(controller.getMaterialSetManager().getMaterialSets());
        } else if (parameterKey.equals("check_destructible") || parameterKey.equals("bypass_undo")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        }
    }

    /**
     * A helper function to go change a given direction to the direction "to the right".
     *
     * There's probably some better matrix-y, math-y way to do this.
     * It'd be nice if this was in BlockFace.
     *
     * @param direction The current direction
     * @return The direction to the left
     */
    public static BlockFace goLeft(BlockFace direction)
    {
        switch (direction)
        {
        case EAST:
            return BlockFace.NORTH;
        case NORTH:
            return BlockFace.WEST;
        case WEST:
            return BlockFace.SOUTH;
        case SOUTH:
            return BlockFace.EAST;
        default:
            return direction;
        }
    }

    /**
     * A helper function to go change a given direction to the direction "to the right".
     *
     * There's probably some better matrix-y, math-y way to do this.
     * It'd be nice if this was in BlockFace.
     *
     * @param direction The current direction
     * @return The direction to the right
     */
    public static BlockFace goRight(BlockFace direction)
    {
        switch (direction)
        {
        case EAST:
            return BlockFace.SOUTH;
        case SOUTH:
            return BlockFace.WEST;
        case WEST:
            return BlockFace.NORTH;
        case NORTH:
            return BlockFace.EAST;
        default:
            return direction;
        }
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean requiresBreakPermission() {
        return false;
    }
}
