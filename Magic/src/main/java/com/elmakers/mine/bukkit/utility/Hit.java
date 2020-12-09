package com.elmakers.mine.bukkit.utility;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Hit {
    private final WeakReference<Entity> entity;
    private final Block block;

    public Hit(Entity entity) {
        this.entity = new WeakReference<>(entity);
        this.block = null;
    }

    public Hit(Block block) {
        this.entity = null;
        this.block = block;
    }

    @Nullable
    public Entity getEntity() {
        return entity == null ? null : entity.get();
    }

    @Nullable
    public Block getBlock() {
        return block;
    }
}
