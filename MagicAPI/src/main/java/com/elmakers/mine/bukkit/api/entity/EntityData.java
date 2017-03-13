package com.elmakers.mine.bukkit.api.entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

public interface EntityData {
    String getKey();
    Location getLocation();
    EntityType getType();
    String getName();
    Art getArt();
    BlockFace getFacing();
    ItemStack getItem();
    double getHealth();
    void setHasMoved(boolean hasMoved);
    Entity spawn();
    Entity spawn(Location location);
    Entity spawn(MageController controller, Location location);
    Entity spawn(MageController controller, Location location, CreatureSpawnEvent.SpawnReason reason);
    Entity undo();
    boolean modify(MageController controller, Entity entity);
    boolean modify(Entity entity);
    EntityData getRelativeTo(Location center);
    String describe();
}
