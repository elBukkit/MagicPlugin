package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;

public class DefaultMaterials {
    private static DefaultMaterials instance;

    private MaterialSetManager manager;
    private MaterialSet commandBlocks;
    private MaterialSet halfBlocks;
    private MaterialSet water;
    private MaterialSet lava;

    private static Map<Material, Map<DyeColor, MaterialAndData>> materialColors = new HashMap<>();
    private static Map<Material, Material> colorMap = new HashMap<>();

    private DefaultMaterials() {
    }

    public static DefaultMaterials getInstance() {
        if (instance == null) {
            instance = new DefaultMaterials();
        }
        return instance;
    }

    public void initialize(MaterialSetManager manager, Collection<ConfigurationSection> colors) {
        this.manager = manager;
        commandBlocks = manager.getMaterialSet("commands");
        water = manager.getMaterialSet("all_water");
        lava = manager.getMaterialSet("all_lava");
        halfBlocks = manager.getMaterialSet("half");

        for (ConfigurationSection colorSection : colors) {
            Material keyColor = null;
            Map<DyeColor, MaterialAndData> newColors = new HashMap<>();
            for (DyeColor color : DyeColor.values()) {
                String materialName = colorSection.getString(color.name().toLowerCase());
                if (materialName == null || materialName.isEmpty()) break;
                MaterialAndData parsed = null;
                parsed = new MaterialAndData(materialName.toUpperCase());
                if (!parsed.isValid()) {
                    break;
                }

                newColors.put(color, parsed);
                if (keyColor == null) {
                    keyColor = parsed.getMaterial();
                }
            }
            if (newColors.size() != DyeColor.values().length) continue;

            materialColors.put(keyColor, newColors);
            for (MaterialAndData mat : newColors.values()) {
                colorMap.put(mat.getMaterial(), keyColor);
            }
        }
    }

    @Nullable
    public Material getBaseMaterial(@Nullable Material material) {
        material = material == null ? null : colorMap.get(material);
        if (material == null && colorMap.size() > 0) {
            for (Material m : colorMap.values()) {
                material = m;
                break;
            }
        }
        return material;
    }

    public void colorize(@Nonnull MaterialAndData materialAndData, @Nonnull DyeColor color) {
        Material material = colorMap.get(materialAndData.getMaterial());
        if (material == null) {
            return;
        }
        Map<DyeColor, MaterialAndData> materialMap = materialColors.get(material);
        if (materialMap == null) {
            return;
        }
        MaterialAndData colored = materialMap.get(color);
        if (colored != null) {
            materialAndData.material = colored.material;
            materialAndData.data = colored.data;
        }
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
