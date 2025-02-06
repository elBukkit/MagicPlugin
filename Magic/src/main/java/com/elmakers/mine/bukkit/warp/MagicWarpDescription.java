package com.elmakers.mine.bukkit.warp;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.warp.WarpDescription;

public class MagicWarpDescription implements WarpDescription {
    private final String key;
    private final boolean maintainDirection;
    private final MageController controller;
    @Nullable
    private Collection<EffectPlayer> effects;
    private String permission;

    public MagicWarpDescription(MageController controller, String key, boolean maintainDirection) {
        this.controller = controller;
        this.key = key;
        this.maintainDirection = maintainDirection;
    }

    public MagicWarpDescription(MageController controller, String key) {
        this(controller, key, true);
    }

    @Nonnull
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isMaintainDirection() {
        return maintainDirection;
    }

    @Override
    public Collection<EffectPlayer> getEffects() {
        return effects;
    }

    public void setEffects(Collection<EffectPlayer> effects) {
        this.effects = effects;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean hasPermission(Entity entity) {
        return permission == null || entity == null || controller.hasPermission(entity, permission);
    }
}
