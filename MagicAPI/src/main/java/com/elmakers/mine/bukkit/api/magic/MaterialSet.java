package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

/**
 * A set of materials.
 *
 * Magic does not distinguish between sets that need to be enumerable and sets
 * that serve as a predicate.
 */
public interface MaterialSet {
    /**
     * @return Inverts the predicate functionality of this set. This does not
     *         affect the iterators.
     */
    MaterialSet not();

    /**
     * @return An iterable that can be used to list all materials specified.
     *         This will only contain the materials that were specified without
     *         any additional data.
     */
    Collection<Material> getMaterials();

    /**
     * Tests whether the specified material matches.
     *
     * @param material
     *            The material to test.
     * @return True if it matches.
     */
    boolean testMaterial(Material material);

    boolean testBlock(Block testBlock);

    boolean testItem(ItemStack item);

    boolean testMaterialAndData(MaterialAndData targetMaterial);
}
