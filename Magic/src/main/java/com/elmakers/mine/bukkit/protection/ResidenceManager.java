package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;

public class ResidenceManager implements PVPManager, BlockBreakManager, BlockBuildManager, EntityTargetingManager {
    private final MageController controller;
    private final Plugin residencePlugin;

    private final Class<Enum> enum_Flags;
    private final Class<Enum> enum_FlagCombo;

    private final Class<?> class_PlayerManager;
    private final Class<?> class_ResidencePlayer;
    private final Class<?> class_FlagPermissions;

    private final Method method_getPlayerManager;
    private final Method method_getResidencePlayer;
    private final Method method_getPermsByLoc;
    private final Method method_hasPermission;
    private final Method method_canPlaceBlock;
    private final Method method_canBreakBlock;
    private final Method method_canDamageEntity;

    private final Enum<?> enum_Flags_pvp;
    private final Enum<?> enum_FlagCombo_TrueOrNone;

    public ResidenceManager(Plugin residencePlugin, MageController controller, ConfigurationSection configuration)
            throws ClassNotFoundException, NoSuchMethodException {
        this.controller = controller;
        this.residencePlugin = residencePlugin;
        ClassLoader classLoader = ResidenceManager.class.getClassLoader();

        enum_Flags = (Class<Enum>)classLoader.loadClass("com.bekvon.bukkit.residence.containers.Flags");
        enum_Flags_pvp = Enum.valueOf(enum_Flags, "pvp");

        enum_FlagCombo = (Class<Enum>)classLoader.loadClass("com.bekvon.bukkit.residence.protection.FlagPermissions$FlagCombo");
        enum_FlagCombo_TrueOrNone = Enum.valueOf(enum_FlagCombo, "TrueOrNone");

        class_FlagPermissions = classLoader.loadClass("com.bekvon.bukkit.residence.protection.FlagPermissions");
        method_getPermsByLoc = residencePlugin.getClass().getMethod("getPermsByLoc", Location.class);
        method_hasPermission = class_FlagPermissions.getMethod("has", enum_Flags, enum_FlagCombo);

        class_PlayerManager = classLoader.loadClass("com.bekvon.bukkit.residence.api.ResidencePlayerInterface");
        class_ResidencePlayer = classLoader.loadClass("com.bekvon.bukkit.residence.containers.ResidencePlayer");

        method_getPlayerManager = residencePlugin.getClass().getMethod("getPlayerManager");
        method_getResidencePlayer = class_PlayerManager.getMethod("getResidencePlayer", Player.class);

        method_canBreakBlock = class_ResidencePlayer.getMethod("canBreakBlock", Block.class, Boolean.TYPE);
        method_canPlaceBlock = class_ResidencePlayer.getMethod("canBreakBlock", Block.class, Boolean.TYPE);
        method_canDamageEntity = class_ResidencePlayer.getMethod("canDamageEntity", Entity.class, Boolean.TYPE);
    }

    @Nullable
    protected Object getResidencePlayer(Player player) throws InvocationTargetException, IllegalAccessException {
        Object playerManager = method_getPlayerManager.invoke(residencePlugin);
        if (playerManager == null) {
            return null;
        }
        return method_getResidencePlayer.invoke(playerManager, player);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        try {
            Object permissions = method_getPermsByLoc.invoke(residencePlugin, location);
            if (permissions == null) {
                return true;
            }
            return (boolean)method_hasPermission.invoke(permissions, enum_Flags_pvp, enum_FlagCombo_TrueOrNone);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence pvp checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        try {
            Object residencePlayer = getResidencePlayer(player);
            if (residencePlayer == null) {
                return true;
            }
            return (boolean)method_canPlaceBlock.invoke(residencePlayer, block, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence build checks", ex);
        }

        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        try {
            Object residencePlayer = getResidencePlayer(player);
            if (residencePlayer == null) {
                return true;
            }
            return (boolean)method_canBreakBlock.invoke(residencePlayer, block, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence break checks", ex);
        }

        return true;
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        if (!(source instanceof Player)) {
            return true;
        }
        try {
            Object residencePlayer = getResidencePlayer((Player)source);
            if (residencePlayer == null) {
                return true;
            }
            return (boolean)method_canDamageEntity.invoke(residencePlayer, target, false);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Something is going wrong with Residence entity targeting checks", ex);
        }

        return true;
    }
}
