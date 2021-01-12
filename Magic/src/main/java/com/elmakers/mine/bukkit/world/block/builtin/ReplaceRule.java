package com.elmakers.mine.bukkit.world.block.builtin;

import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.block.BlockRule;
import com.elmakers.mine.bukkit.world.tasks.ModifyBlockTask;

public class ReplaceRule extends BlockRule {
    private MaterialAndData replace = null;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        String materialKey = config.getString("replace");
        if (materialKey != null && !materialKey.isEmpty() && !materialKey.equalsIgnoreCase("none")) {
            replace = new MaterialAndData(materialKey);
            if (!replace.isValid()) {
                controller.getLogger().warning("Invalid replace material in block rule: " + materialKey);
            }
        }
        if (replace != null) {
            logBlockRule("Replacing with " + replace);
        }
        return replace != null;
    }

    @Override
    @Nullable
    public BlockResult onHandle(Block block, Random random, Player cause) {
        if (replace == null || !replace.isValid()) {
            return BlockResult.SKIP;
        }
        Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTaskLater(plugin, new ModifyBlockTask(block, replace), 1);
        return BlockResult.STOP;
    }
}
