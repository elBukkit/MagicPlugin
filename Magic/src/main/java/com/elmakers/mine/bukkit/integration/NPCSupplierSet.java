package com.elmakers.mine.bukkit.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.entity.Entity;

import com.google.common.collect.Sets;

public final class NPCSupplierSet implements NPCSupplier {
    private final Set<NPCSupplier> suppliers = Sets.newIdentityHashSet();

    @Override
    public boolean isNPC(Entity entity) {
        for (NPCSupplier supplier : suppliers) {
            if (supplier.isNPC(entity)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isStaticNPC(Entity entity) {
        for (NPCSupplier supplier : suppliers) {
            if (supplier.isStaticNPC(entity)) {
                return true;
            }
        }

        return false;
    }

    public void register(@Nonnull NPCSupplier supplier) {
        suppliers.add(checkNotNull(supplier));
    }
}
