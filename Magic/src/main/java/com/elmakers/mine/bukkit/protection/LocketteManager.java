package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LocketteManager implements BlockBuildManager, BlockBreakManager {
    private boolean enabled = false;
    private Method isOwnerMethod = null;
    private Method isProtectedMethod = null;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && isOwnerMethod != null && isProtectedMethod != null;
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            Plugin lockettePlugin = plugin.getServer().getPluginManager().getPlugin("Lockette");
            if (lockettePlugin != null)
            {
                try {
                    Class<?> locketteClass = Class.forName("org.yi.acru.bukkit.Lockette.Lockette");
                    isOwnerMethod = locketteClass.getMethod("isOwner", Block.class, String.class);
                    isProtectedMethod = locketteClass.getMethod("isProtected", Block.class);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

                if (isOwnerMethod == null || isProtectedMethod == null) {
                    plugin.getLogger().info("Lockette integration failed, will not integrate.");
                } else {
                    plugin.getLogger().info("Lockette found, will check block protection.");
                }
            } else {
                plugin.getLogger().info("Lockette not found, will not integrate.");
            }
        } else {
            plugin.getLogger().info("Lockette integration disabled");
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
                if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN)
                {
                    return !(Boolean)isProtectedMethod.invoke(null, block);
                }

                return (Boolean)isOwnerMethod.invoke(null, block, player.getName());
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
