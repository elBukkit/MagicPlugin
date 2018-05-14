package com.elmakers.mine.bukkit.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.bukkit.entity.Entity;

public final class GenericMetadataNPCSupplier implements NPCSupplier {
    private final @Nonnull String metaKey;

    public GenericMetadataNPCSupplier(@Nonnull String metaKey) {
        this.metaKey = checkNotNull(metaKey);
    }

    @Override
    public boolean isNPC(Entity entity) {
        return entity.hasMetadata(metaKey);
    }

    @Override
    public boolean isStaticNPC(Entity entity) {
        return entity.hasMetadata(metaKey);
    }
}
