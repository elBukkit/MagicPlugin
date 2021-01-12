package com.elmakers.mine.bukkit.world.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.BlockResult;

public abstract class BlockRule {
    protected MagicController controller;
    protected List<Material> blockTypes;
    protected boolean isGlobal;
    protected String worldName;
    protected String action;
    protected BlockResult result;

    protected void initialize(MagicController controller, String worldName, String action) {
        this.controller = controller;
        this.worldName = worldName;
        this.action = action;
    }

    public boolean load(ConfigurationSection config, MagicController controller, String worldName, String action) {
        initialize(controller, worldName, action);
        String resultType = config.getString("result");
        if (resultType != null && !resultType.isEmpty()) {
            try {
                result = BlockResult.valueOf(resultType.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid block rule result type: " + resultType);
            }
        }
        isGlobal = !config.contains("type") && !config.contains("types");
        if (!isGlobal) {
            blockTypes = new ArrayList<>();
            String oneType = config.getString("type");
            if (oneType != null && !oneType.isEmpty()) {
                try {
                    Material material = Material.valueOf(oneType.toUpperCase());
                    blockTypes.add(material);
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid material in block rule type: " + oneType);
                }
            }
            List<String> typeList = ConfigurationUtils.getStringList(config, "types");
            if (typeList != null && !typeList.isEmpty()) {
                for (String typeKey : typeList) {
                    try {
                        Material material = Material.valueOf(typeKey.toUpperCase());
                        blockTypes.add(material);
                    } catch (Exception ex) {
                        controller.getLogger().warning("Invalid material in block rule types list: " + typeKey);
                    }
                }
            }
        }
        return onLoad(config);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public List<Material> getBlockTypes() {
        return blockTypes;
    }

    public String getTargetBlockTypeNames() {
        if (isGlobal) {
            return "all block types";
        }
        return StringUtils.join(blockTypes, ",");
    }

    protected void logBlockRule(String message) {
        message = message + " on " + action + " of " + getTargetBlockTypeNames() + " in " + worldName;
        controller.info(message);
    }

    public BlockResult handle(Block block, Random random, Player cause) {
        BlockResult result = onHandle(block, random, cause);
        return this.result != null ? this.result : result;
    }

    public abstract boolean onLoad(ConfigurationSection config);
    public abstract BlockResult onHandle(Block block, Random random, Player cause);
}
