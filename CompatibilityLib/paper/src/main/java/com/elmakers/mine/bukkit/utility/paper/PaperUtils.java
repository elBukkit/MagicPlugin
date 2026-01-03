package com.elmakers.mine.bukkit.utility.paper;

import java.util.function.Consumer;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class PaperUtils implements com.elmakers.mine.bukkit.utility.platform.PaperUtils {
    @Override
    public void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer) {
        world.getChunkAtAsync(x, z, generate, consumer);
    }

    @Override
    public void registerEvents(MageController controller, PluginManager pm) {
        pm.registerEvents(new PaperPlayerListener(controller), controller.getPlugin());
    }

    @Override
    public void setSwingAnimation(ItemStack itemStack) {
        itemStack.setData(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation()
                .type(SwingAnimation.Animation.STAB)
                .duration(18)
                .build());
    }
}
