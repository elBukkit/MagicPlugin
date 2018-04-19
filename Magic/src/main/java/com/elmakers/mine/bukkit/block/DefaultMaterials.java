package com.elmakers.mine.bukkit.block;

import java.util.Collection;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;

public class DefaultMaterials {
    private static DefaultMaterials instance;

    private MaterialSetManager manager;
    private MaterialSet commandBlocks;
    private MaterialSet halfBlocks;
    private MaterialSet water;
    private MaterialSet lava;

    private DefaultMaterials() {
    }

    public static DefaultMaterials getInstance() {
        if (instance == null) {
            instance = new DefaultMaterials();
        }
        return instance;
    }

    public void initialize(MaterialSetManager manager) {
        this.manager = manager;
        commandBlocks = manager.getMaterialSet("commands");
        water = manager.getMaterialSet("all_water");
        lava = manager.getMaterialSet("all_lava");
        halfBlocks = manager.getMaterialSet("half");
    }

    public static boolean isCommand(Material material) {
        return getInstance().commandBlocks.testMaterial(material);
    }

    public static boolean isHalfBlock(Material material) {
        return getInstance().halfBlocks.testMaterial(material);
    }

    public static boolean isWater(Material material) {
        return getInstance().water.testMaterial(material);
    }

    public static boolean isLava(Material material) {
        return getInstance().lava.testMaterial(material);
    }

    public static Collection<Material> getWater() {
        return getInstance().water.getMaterials();
    }

    public static MaterialSet getWaterSet() {
        return getInstance().water;
    }

    public static Collection<Material> getLava() {
        return getInstance().lava.getMaterials();
    }
}
