package com.elmakers.mine.bukkit.spell;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class BlockSpell extends UndoableSpell {

    private Set<Material>	indestructible;
    private Set<Material>	destructible;
    protected boolean 		checkDestructible 		= true;

    public final static String[] BLOCK_PARAMETERS = {
        "indestructible", "destructible", "check_destructible", "bypass_undo", "undo"
    };

    public boolean isIndestructible(Block block)
    {
        if (mage.isSuperPowered()) return false;
        Player player = mage.getPlayer();
        if (player != null && player.hasPermission("Magic.bypass")) return false;
        if (controller.isLocked(block)) return true;
        if (indestructible == null) {
            return mage.isIndestructible(block);
        }
        return indestructible.contains(block.getType()) || mage.isIndestructible(block);
    }

    public boolean isDestructible(Block block)
    {
        if (isIndestructible(block)) return false;

        if (!checkDestructible) return true;
        if (targetBreakables > 0 && currentCast.isBreakable(block)) return true;
        if (destructible == null) {
            return mage.isDestructible(block);
        }
        return destructible.contains(block.getType());
    }

    public Set<Material> getDestructible() {
        if (destructible != null) return destructible;
        return controller.getDestructibleMaterials();
    }

    public boolean areAnyDestructible(Block block)
    {
        if (isIndestructible(block)) return false;

        if (!checkDestructible) return true;
        if (targetBreakables > 0 && currentCast.isBreakable(block)) return true;
        Set<Material> allDestructible = destructible;
        if (allDestructible == null) {
            allDestructible = controller.getDestructibleMaterials();
        }
        if (allDestructible == null) {
            return true;
        }
        if (allDestructible.contains(block.getType())) return true;
        com.elmakers.mine.bukkit.api.block.BlockData blockData = UndoList.getBlockData(block.getLocation());
        if (blockData == null || !blockData.containsAny(allDestructible)) {
            return false;
        }
        return true;
    }

    protected void setDestructible(Set<Material> materials) {
        checkDestructible = true;
        destructible = materials;
    }

    protected void addDestructible(Material material) {
        if (destructible == null) {
            destructible = new HashSet<Material>(controller.getDestructibleMaterials());
        }
        destructible.add(material);
    }

    @Override
    public void processParameters(ConfigurationSection parameters) {
        super.processParameters(parameters);
        indestructible = null;
        if (parameters.contains("indestructible")) {
            indestructible = controller.getMaterialSet(parameters.getString("indestructible"));
        }
        if (parameters.contains("id")) {
            indestructible = controller.getMaterialSet(parameters.getString("id"));
        }
        destructible = null;
        if (parameters.contains("destructible")) {
            // This always needs to be a copy since it can be modified by addDestructible
            // Kind of a hack for Automata.
            destructible = new HashSet<Material>(controller.getMaterialSet(parameters.getString("destructible")));
        }

        if (parameters.getBoolean("destructible_override", false)) {
            String destructibleKey = controller.getDestructibleMaterials(mage, mage.getLocation());
            if (destructibleKey != null) {
                if (destructible == null) {
                    destructible = new HashSet<Material>();
                }
                destructible.addAll(controller.getMaterialSet(destructibleKey));
            }
        }
        checkDestructible = parameters.getBoolean("check_destructible", true);
        checkDestructible = parameters.getBoolean("cd", checkDestructible);
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
            examples.addAll(controller.getMaterialSets());
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
