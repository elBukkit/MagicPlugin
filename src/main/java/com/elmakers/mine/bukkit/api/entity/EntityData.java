package com.elmakers.mine.bukkit.api.entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface EntityData {
    public String getKey();
    public Location getLocation();
    public EntityType getType();
    public String getName();
    public Art getArt();
    public BlockFace getFacing();
    public ItemStack getItem();
    public double getHealth();
    public void setHasMoved(boolean hasMoved);
    public Entity spawn();
    public Entity spawn(Location location);
    public Entity spawn(MageController controller, Location location);
    public Entity undo();
    public boolean modify(MageController controller, Entity entity);
    public boolean modify(Entity entity);
    public EntityData getRelativeTo(Location center);
    public String describe();
}
