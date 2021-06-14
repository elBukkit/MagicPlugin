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
import com.elmakers.mine.bukkit.materials.MaterialSets;
import com.elmakers.mine.bukkit.materials.SimpleMaterialSetManager;
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
        MaterialAndData value = ConfigurationUtils.toMaterialAndData("obsidian");
        MaterialSet set = MaterialSets.unionBuilder()
                .add(value)
                .build();

        assertFalse(set.testBlock(stone()));
        assertFalse(set.testBlock(dirt()));

        Block correct = obsidian();
        assertTrue(set.testBlock(correct));
    }

    @Test
    public void testNegatedSet() {
        SimpleMaterialSetManager manager = new SimpleMaterialSetManager();
        ConfigurationSection materialConfigs = new MemoryConfiguration();
        materialConfigs.set("solid", "!air,water,lava,glass");
        manager.loadMaterials(materialConfigs);

        // Should contain none of the materials in the negated solid list
        MaterialSet set = manager.getMaterialSet("solid");
        assertFalse(set.testMaterial(Material.AIR));
        assertFalse(set.testMaterial(Material.WATER));
    }

    @Test
    public void testWildcardSets() {
        SimpleMaterialSetManager manager = new SimpleMaterialSetManager();
        ConfigurationSection materialConfigs = new MemoryConfiguration();
        materialConfigs.set("all", "*");
        materialConfigs.set("alsoall", "*,stone");
        materialConfigs.set("alwaysall", "stone,*,!all");
        materialConfigs.set("nothing", "!*");
        materialConfigs.set("alsonothing", "!all");
        manager.loadMaterials(materialConfigs);

        // Should contain everything, just testing for air.
        MaterialSet set = manager.getMaterialSet("all");
        assertTrue(set.testMaterial(Material.AIR));
        set = manager.getMaterialSet("alsoall");
        assertTrue(set.testMaterial(Material.AIR));
        set = manager.getMaterialSet("alwaysall");
        assertTrue(set.testMaterial(Material.AIR));

        // Should contain nothing
        set = manager.getMaterialSet("nothing");
        assertFalse(set.testMaterial(Material.AIR));
        set = manager.getMaterialSet("alsonothing");
        assertFalse(set.testMaterial(Material.AIR));
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
        Block stone = Mockito.mock(Block.class);
        Mockito.when(stone.getType()).thenReturn(Material.STONE);
        return stone;
    }

    private Block obsidian() {
        Block stone = Mockito.mock(Block.class);
        Mockito.when(stone.getType()).thenReturn(Material.OBSIDIAN);
        return stone;
    }
}
