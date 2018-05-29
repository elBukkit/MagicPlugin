package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.block.DefaultMaterials;

public class LocketteManager implements BlockBuildManager, BlockBreakManager {
    private boolean enabled = false;
    private Method isOwnerMethod = null;
    private Method isProtectedMethod = null;
    private boolean isPro = false;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && isOwnerMethod != null && isProtectedMethod != null;
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            String pluginName = "Lockette";
            String apiClass = "org.yi.acru.bukkit.Lockette.Lockette";
            Plugin lockettePlugin = plugin.getServer().getPluginManager().getPlugin("Lockette");
            if (lockettePlugin == null) {
                lockettePlugin = plugin.getServer().getPluginManager().getPlugin("LockettePro");
                if (lockettePlugin != null) {
                    pluginName = "LockettePro";
                    apiClass = "me.crafter.mc.lockettepro.LocketteProAPI";
                    isPro = true;
                }
            }
            if (lockettePlugin != null)
            {
                try {
                    Class<?> locketteClass = Class.forName(apiClass);
                    isOwnerMethod = locketteClass.getMethod("isOwner", Block.class, isPro ? Player.class : String.class);
                    isProtectedMethod = locketteClass.getMethod("isProtected", Block.class);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

                if (isOwnerMethod == null || isProtectedMethod == null) {
                    plugin.getLogger().info(pluginName + " integration failed, will not integrate.");
                } else {
                    plugin.getLogger().info(pluginName + " found, will check block protection.");
                }
            } else {
                plugin.getLogger().info("Lockette nor LockettePro found, will not integrate.");
            }
        } else {
            plugin.getLogger().info("Lockette/LockettePro integration disabled");
        }
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (enabled && block != null && isOwnerMethod != null && isProtectedMethod != null) {
            try {
                // Handle command blocks or console spells
                if (player == null)
                {
                    return !(Boolean)isProtectedMethod.invoke(null, block);
                }

                // Lockette doesn't check the sign itself on an isOwner check ..
                // So we just wont' allow breaking the signs, ever.
                if (DefaultMaterials.isSign(block.getType()))
                {
                    return !(Boolean)isProtectedMethod.invoke(null, block);
                }

                return (Boolean)isOwnerMethod.invoke(null, block, isPro ? player : player.getName());
            } catch (Throwable ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }
}
