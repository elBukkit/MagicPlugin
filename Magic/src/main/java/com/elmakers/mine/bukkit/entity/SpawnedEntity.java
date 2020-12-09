package com.elmakers.mine.bukkit.entity;

import java.lang.ref.WeakReference;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class SpawnedEntity {
    private final String worldName;
    private final BlockVector location;
    private final UUID id;
    private final WeakReference<Entity> entity;

    public SpawnedEntity(Entity entity) {
        this.entity = new WeakReference<>(entity);
        worldName = entity.getWorld().getName();
        location = new BlockVector(entity.getLocation().toVector());
        id = entity.getUniqueId();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpawnedEntity)) return false;
        SpawnedEntity other = (SpawnedEntity)o;
        return other.id.equals(id);
    }

    @Nullable
    public Entity getEntity() {
        return this.entity.get();
    }

    public void despawn(MageController controller, CastContext context) {
        Entity entity = this.getEntity();
        if (entity == null || !entity.isValid()) {
            World world = controller.getPlugin().getServer().getWorld(worldName);

            // Not force-loading worlds for this, so this entity gets to live to fight another day
            if (world == null) {
                return;
            }
            Location entityLocation = new Location(world, location.getX(), location.getY(), location.getZ());
            if (!entityLocation.getChunk().isLoaded()) {
                entityLocation.getChunk().load();
            }
            entity = CompatibilityUtils.getEntity(world, id);
        }
        if (entity != null && entity.isValid()) {
            if (context != null && context.hasEffects("undo_entity")) {
                context.playEffects("undo_entity", 1.0f, null, null, entity.getLocation(), entity, null);
            }
            UndoList.setUndoList(entity, null);
            entity.remove();
        }
    }
}
