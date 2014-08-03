package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import org.bukkit.util.Vector;

public interface MaterialBrush extends MaterialAndData {
    public void prepare();
    public boolean isReady();
    public Collection<EntityData> getEntities();
    public Collection<Entity> getTargetEntities();
    public boolean hasEntities();
    public boolean update(final Mage mage, final Location location);
    public void update(String activeMaterial);
    public void activate(final Location location, final String material);
    public void setTarget(Location target);
    public void setTarget(Location target, Location center);
    public Vector getSize();
    public BrushMode getMode();
    public boolean isEraseModifierActive();
}
