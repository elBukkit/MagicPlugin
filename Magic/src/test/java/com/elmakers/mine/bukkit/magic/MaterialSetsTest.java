package com.elmakers.mine.bukkit.magic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.Test;
import org.mockito.Mockito;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MaterialSetsTest {
    @Test
    public void testTestBlock() {
        MaterialSet set = MaterialSets.unionBuilder()
                .add(Material.STONE)
                .build();

        assertTrue(set.testBlock(stone()));
        assertFalse(set.testBlock(dirt()));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFromConfig() {
        MaterialAndData value = ConfigurationUtils.toMaterialAndData("stone|2");
        MaterialSet set = MaterialSets.unionBuilder()
                .add(value)
                .build();

        assertFalse(set.testBlock(stone()));
        assertFalse(set.testBlock(dirt()));

        Block correct = stone();
        Mockito.when(correct.getData()).thenReturn((byte) 2);
        assertTrue(set.testBlock(correct));
    }

    private Block dirt() {
        Block dirt = Mockito.mock(Block.class);
        Mockito.when(dirt.getType()).thenReturn(Material.DIRT);
        return dirt;
    }

    private Block stone() {
        Block dirt = Mockito.mock(Block.class);
        Mockito.when(dirt.getType()).thenReturn(Material.STONE);
        return dirt;
    }
}
