package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.wand.Wand;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;

public class PathPerkReward extends PerkReward {
    private final MageController controller;
    private final String classKey;
    private ProgressionPath path;

    public PathPerkReward(MageController controller, String classKey, String name) {
        super(name);
        this.controller = controller;
        this.classKey = classKey;
    }

    @Override
    public void apply(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null && path != null) {
            // Seems like Valhalla will call this async
            controller.getPlugin().getServer().getScheduler().runTask(controller.getPlugin(), () -> {
                Wand wand = mage.getActiveWand();
                ProgressionPath currentPath = mageClass.getPath();

                // First make sure this path can be reached
                ProgressionPath checkPath = currentPath;
                while (checkPath != null && !checkPath.getKey().equals(path.getKey())) {
                    checkPath = checkPath.getNextPath();
                }

                if (checkPath != null) {
                    while (currentPath != null && !currentPath.getKey().equals(path.getKey())) {
                        currentPath.upgrade(mage, wand);
                        currentPath = currentPath.getNextPath();
                        if (currentPath == null) break;
                    }
                }
            });
        }
    }

    @Override
    public void remove(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null) {
            mageClass.setPath(null);
        }
    }

    @Override
    public void parseArgument(Object argument) {
        String pathKey = parseString(argument);
        path = controller.getPath(pathKey);
    }

    @Override
    public String rewardPlaceholder() {
        if (path == null) {
            return "";
        }
        return "Rank up to " + path.getName();
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING;
    }
}
