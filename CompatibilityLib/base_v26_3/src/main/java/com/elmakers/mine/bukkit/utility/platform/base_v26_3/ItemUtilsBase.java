package com.elmakers.mine.bukkit.utility.platform.base_v26_3;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class ItemUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_1.ItemUtilsBase {
    protected Method toBukkitMethod;

    protected ItemUtilsBase(final Platform platform) {
        super(platform);
        try {
            toBukkitMethod = CraftItemStack.class.getDeclaredMethod("asBukkitMirror", net.minecraft.world.item.ItemStack.class);
            platform.getLogger().info("Found Paper version of asBukkitMirror, will use via reflection");
        } catch (Exception ignore) {
            toBukkitMethod = null;
        }
    }

    protected ItemStack asCraftMirror(net.minecraft.world.item.ItemStack original) {
        if (toBukkitMethod != null) {
            try {
                return (ItemStack)toBukkitMethod.invoke(null, original);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Failed to call asBukkitMirror", ex);
                return null;
            }
        }
        return CraftItemStack.asCraftMirror(original);
    }
}
