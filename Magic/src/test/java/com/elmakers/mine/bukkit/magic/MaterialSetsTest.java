package com.elmakers.mine.bukkit.magic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
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

    @Test
    public void testComplexSet() {
        SimpleMaterialSetManager manager = new SimpleMaterialSetManager();
        ConfigurationSection materialConfigs = new MemoryConfiguration();
        materialConfigs.set("transparent", "air,water,lava,glass");
        materialConfigs.set("liquid", "water,lava");
        manager.loadMaterials(materialConfigs);

        // Should contain liquid but not air or glass
        MaterialSet set = manager.fromConfig("liquid,!transparent");
        assertFalse(set.testMaterial(Material.AIR));
        assertTrue(set.testMaterial(Material.WATER));

        // Should contain none of the materials in either set
        set = manager.fromConfig("!transparent,liquid");
        assertFalse(set.testMaterial(Material.AIR));
        assertFalse(set.testMaterial(Material.WATER));
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
