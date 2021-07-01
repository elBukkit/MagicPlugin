package com.elmakers.mine.bukkit.world.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.world.BlockResult;

public class MagicBlockHandler {
    public static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.world.block.builtin";

    protected String worldName;
    protected String action;
    private final MagicController controller;
    private final Map<Material, List<BlockRule>> blockRules = new HashMap<>();
    private final List<BlockRule> globalBlockRules = new ArrayList<>();
    private Random random;

    public MagicBlockHandler(MagicController controller) {
        this.controller = controller;
    }

    public void load(String worldName, String action, ConfigurationSection config) {
        this.worldName = worldName;
        this.action = action;
        if (config == null) return;
        for (String key : config.getKeys(false)) {
            ConfigurationSection handlerConfig = config.getConfigurationSection(key);
            if (handlerConfig == null) {
                controller.getLogger().warning("Was expecting a properties section in world block event config for key '" + worldName + "', but got: " + config.get(key));
                continue;
            }
            if (!handlerConfig.getBoolean("enabled", true)) {
                continue;
            }

            String className = handlerConfig.getString("class");
            BlockRule rule = createBlockRule(className);
            if (rule != null) {
                if (rule.load(handlerConfig, controller, worldName, action)) {
                    if (rule.isGlobal()) {
                        globalBlockRules.add(rule);
                    } else {
                        for (Material targetBlock : rule.getBlockTypes()) {
                            List<BlockRule> rules = blockRules.get(targetBlock);
                            if (rules == null) {
                                rules = new ArrayList<>();
                                blockRules.put(targetBlock, rules);
                            }
                            rules.add(rule);
                        }
                    }
                } else {
                    controller.getLogger().warning("Skipping invalid " + key + " block rule for " + worldName);
                }
            } else {
                controller.getLogger().warning("Skipping invalid " + key + " block rule for " + worldName);
            }
        }
    }

    private Random getRandom(World world) {
        if (random == null) {
            long seed = world == null ? System.currentTimeMillis() : world.getSeed();
            random = new Random(seed);
        }
        return random;
    }

    public BlockResult handleBlock(Block block, Player player) {
        Random random = getRandom(block.getWorld());
        for (BlockRule blockRule : globalBlockRules) {
            BlockResult result = blockRule.handle(block, random, player);
            if (result != BlockResult.SKIP) {
                return result;
            }
        }
        List<BlockRule> rules = blockRules.get(block.getType());
        if (rules != null) {
            for (BlockRule blockRule : rules) {
                BlockResult result = blockRule.handle(block, random, player);
                if (result != BlockResult.SKIP) {
                return result;
            }
            }
        }
        return BlockResult.SKIP;
    }

    @Nullable
    protected BlockRule createBlockRule(String className) {
        if (className == null) return null;

        if (className.indexOf('.') <= 0) {
            className = BUILTIN_CLASSPATH + "." + className;
            if (!className.endsWith("Rule")) {
                className += "Rule";
            }
        }

        Class<?> handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading block rule: " + className, ex);
            return null;
        }

        Object newObject;
        try {
            newObject = handlerClass.getDeclaredConstructor().newInstance();
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error loading block rule: " + className, ex);
            return null;
        }

        if (newObject == null || !(newObject instanceof BlockRule)) {
            controller.getLogger().warning("Error loading block rule " + className + ", does it extend BlockRule?");
            return null;
        }

        return (BlockRule)newObject;
    }

    public boolean isEmpty() {
        return globalBlockRules.size() == 0;
    }
}
