package com.elmakers.mine.bukkit.api.entity;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.Cost;
import com.elmakers.mine.bukkit.api.magic.MageController;

public interface EntityData {
    enum TargetType {
        NONE,
        PLAYER,
        MOB
    }

    enum SourceType {
        PLAYER,
        MOB,
        CONSOLE,
        OPPED_PLAYER
    }

    String getKey();
    Location getLocation();
    EntityType getType();
    String getName();
    Art getArt();
    BlockFace getFacing();
    ItemStack getItem();
    double getHealth();
    void setHasMoved(boolean hasMoved);
    void setDamaged(boolean damaged);
    boolean isDocile();
    boolean isTransformable();
    @Nullable
    Entity spawn();
    @Nullable
    Entity spawn(MageController controller);
    @Nullable
    Entity spawn(Location location);
    @Nullable
    Entity spawn(MageController controller, Location location);
    @Nullable
    Entity spawn(MageController controller, Location location, CreatureSpawnEvent.SpawnReason reason);
    @Nullable
    Entity undo();
    boolean modify(MageController controller, Entity entity);
    boolean modify(Entity entity);
    @Nullable
    EntityData getRelativeTo(Location center);
    String describe();
    @Nullable
    String getInteractSpell();
    SourceType getInteractSpellSource();
    TargetType getInteractSpellTarget();
    @Nullable
    ConfigurationSection getInteractSpellParameters();
    SourceType getInteractCommandSource();
    List<String> getInteractCommands();
    @Nullable
    Collection<Cost> getInteractCosts();
    void setMaterial(@Nonnull MaterialAndData material);
    @Nullable
    String getInteractPermission();
    @Nullable
    MaterialAndData getMaterial();

    /**
     * Attach this mob to an existing entity. This does not modify the entity, and only has an effect
     * if this mob has mage data (e.g. is a spellcaster)
     *
     * @param controller The MageController, used to create a Mage
     * @param entity The entity to attach to
     */
    void attach(@Nonnull MageController controller, @Nonnull Entity entity);
}
