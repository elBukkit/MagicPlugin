package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.magic.MaterialSets;

public class DefaultMaterials {
    private static DefaultMaterials instance;

    private MaterialSet commandBlocks = MaterialSets.empty();
    private MaterialSet halfBlocks = MaterialSets.empty();
    private MaterialSet water = MaterialSets.empty();
    private MaterialSet lava = MaterialSets.empty();
    private MaterialSet skulls = MaterialSets.empty();
    private MaterialSet playerSkulls = MaterialSets.empty();
    private MaterialSet banners = MaterialSets.empty();
    private MaterialSet signs = MaterialSets.empty();
    private MaterialSet saplings = MaterialSets.empty();

    private MaterialAndData playerSkullItem = null;
    private Material groundSignBlock = null;
    private Material firework = null;
    private MaterialAndData wallTorch = null;
    private MaterialAndData redstoneTorchOn = null;
    private MaterialAndData redstoneTorchOff = null;
    private MaterialAndData redstoneWallTorchOn = null;
    private MaterialAndData redstoneWallTorchOff = null;

    private Map<Material, Map<DyeColor, MaterialAndData>> materialColors = new HashMap<>();
    private Map<Material, Material> colorMap = new HashMap<>();
    private Map<Material, Material> blockItems = new HashMap<>();

    private DefaultMaterials() {
    }

    public static DefaultMaterials getInstance() {
        if (instance == null) {
            instance = new DefaultMaterials();
        }
        return instance;
    }

    public void initialize(MaterialSetManager manager) {
        commandBlocks = manager.getMaterialSet("commands");
        water = manager.getMaterialSet("all_water");
        lava = manager.getMaterialSet("all_lava");
        halfBlocks = manager.getMaterialSet("half");
        skulls = manager.getMaterialSet("skulls");
        playerSkulls = manager.getMaterialSet("player_skulls");
        banners = manager.getMaterialSet("banners");
        signs = manager.getMaterialSet("signs");
        saplings = manager.getMaterialSet("saplings");
    }

    public void loadColors(Collection<ConfigurationSection> colors) {
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

    public void setGroundSignBlock(Material material) {
        this.groundSignBlock = material;
    }

    @Nullable
    public static Material getGroundSignBlock() {
        return getInstance().groundSignBlock;
    }

    public void setFirework(Material material) {
        this.firework = material;
    }

    @Nullable
    public static Material getFirework() {
        return getInstance().firework;
    }

    public void setPlayerSkullItem(MaterialAndData item) {
        playerSkullItem = item;
    }

    @Nullable
    public static MaterialAndData getPlayerSkullItem() {
        return getInstance().playerSkullItem;
    }

    public void loadBlockItems(ConfigurationSection blocks) {
        Set<String> blockKeys = blocks.getKeys(false);
        for (String blockKey : blockKeys) {
            try {
                Material blockMaterial = Material.getMaterial(blockKey.toUpperCase());
                String itemKey = blocks.getString(blockKey);
                Material itemMaterial = Material.getMaterial(itemKey.toUpperCase());
                blockItems.put(blockMaterial, itemMaterial);
            } catch (Exception ignore) {

            }
        }
    }

    @Nullable
    public static Material getBaseColor(@Nullable Material material) {
        return material == null ? null : getInstance().colorMap.get(material);
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

    public Collection<Map<DyeColor, MaterialAndData>> getAllColorBlocks() {
        return materialColors.values();
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

    public static boolean isPlayerSkull(MaterialAndData materialAndData) {
        return getInstance().playerSkulls.testMaterialAndData(materialAndData);
    }

    public static boolean isSkull(Material material) {
        return getInstance().skulls.testMaterial(material);
    }

    public static boolean isBanner(Material material) {
        return getInstance().banners.testMaterial(material);
    }

    public static boolean isSign(Material material) {
        return getInstance().signs.testMaterial(material);
    }

    public static boolean isSapling(Material material) {
        return getInstance().saplings.testMaterial(material);
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

    public static Material blockToItem(Material block) {
        Material item = getInstance().blockItems.get(block);
        return item == null ? block : item;
    }

    public static MaterialAndData getWallTorch() {
        return getInstance().wallTorch;
    }

    public void setWallTorch(MaterialAndData wallTorch) {
        this.wallTorch = wallTorch;
    }

    public static MaterialAndData getRedstoneTorchOn() {
        return getInstance().redstoneTorchOn;
    }

    public void setRedstoneTorchOn(MaterialAndData redstoneTorchOn) {
        this.redstoneTorchOn = redstoneTorchOn;
    }

    public static MaterialAndData getRedstoneTorchOff() {
        return getInstance().redstoneTorchOff;
    }

    public void setRedstoneTorchOff(MaterialAndData redstoneTorchOff) {
        this.redstoneTorchOff = redstoneTorchOff;
    }

    public static MaterialAndData getRedstoneWallTorchOn() {
        return getInstance().redstoneWallTorchOn;
    }

    public void setRedstoneWallTorchOn(MaterialAndData redstoneWallTorchOn) {
        this.redstoneWallTorchOn = redstoneWallTorchOn;
    }

    public static MaterialAndData getRedstoneWallTorchOff() {
        return getInstance().redstoneWallTorchOff;
    }

    public void setRedstoneWallTorchOff(MaterialAndData redstoneWallTorchOff) {
        this.redstoneWallTorchOff = redstoneWallTorchOff;
    }
}
