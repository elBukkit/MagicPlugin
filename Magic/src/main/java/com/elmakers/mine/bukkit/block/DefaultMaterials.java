package com.elmakers.mine.bukkit.block;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;

public class DefaultMaterials {
    private static DefaultMaterials instance;

    private MaterialSetManager manager;
    private MaterialSet commandBlocks;

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
    }

    public static boolean isCommand(Material material) {
        return getInstance().commandBlocks.testMaterial(material);
    }
}
