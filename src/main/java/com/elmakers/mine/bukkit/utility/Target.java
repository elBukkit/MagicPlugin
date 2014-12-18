package com.elmakers.mine.bukkit.utility;

import java.lang.ref.WeakReference;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class Target implements Comparable<Target>
{
    protected int    maxDistanceSquared = 128 * 128;
    protected int    minDistanceSquared = 0;
    protected double maxAngle           = 0.3;

    private Location source;
    private Location location;
    private WeakReference<Entity>   _entity;
    private Mage	 mage;
    private boolean  reverseDistance = false;

    private double   distanceSquared    = 100000;
    private double   angle              = 10000;
    private int      score              = 0;

    private Object	 extraData	 = null;

    public Target(Location sourceLocation)
    {
        this.source = sourceLocation;
    }

    public Target(Location sourceLocation, Block block)
    {
        this.source = sourceLocation;
        if (block != null) this.location = block.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Block block, int range)
    {
        this(sourceLocation, block, range, 0.3, false);
    }

    public Target(Location sourceLocation, Block block, int range, double angle)
    {
        this(sourceLocation, block, range, angle, false);
    }

    public Target(Location sourceLocation, Block block, int range, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        if (block != null) this.location = block.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Block block, int minRange, int maxRange, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        if (block != null) this.location = block.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range)
    {
        this.maxDistanceSquared = range * range;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = entity.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle)
    {
        this(sourceLocation, entity, range, angle, range, angle);
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, float closeRange, double closeAngle)
    {
        this.maxDistanceSquared = range * range;
        if (closeRange < range && this.maxDistanceSquared < closeRange * closeRange)
        {
            this.maxAngle = closeAngle;
        }
        else
        {
            this.maxAngle = angle;
        }
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = entity.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = entity.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int minRange, int maxRange, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = entity.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Mage mage, int minRange, int maxRange, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this.mage = mage;
        if (mage != null) {
            this._entity = new WeakReference<Entity>(mage.getLivingEntity());
        }
        if (mage != null) this.location = mage.getLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = entity.getLocation();
    }

    public Target(Location sourceLocation, Entity entity, Block block)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (block != null) {
            this.location = block.getLocation();
        } else if (entity != null) {
            this.location = entity.getLocation();
        }
    }

    public int getScore()
    {
        return score;
    }

    protected void calculateScore()
    {
        if (source == null) return;

        Vector playerFacing = source.getDirection();
        Vector playerLoc = new Vector(source.getBlockX(), source.getBlockY(), source.getBlockZ());

        Location targetLocation = getLocation();
        if (targetLocation == null) return;

        Vector targetLoc = new Vector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
        Vector targetDirection = new Vector(targetLoc.getBlockX() - playerLoc.getBlockX(), targetLoc.getBlockY() - playerLoc.getBlockY(), targetLoc.getBlockZ() - playerLoc.getBlockZ());
        angle = targetDirection.angle(playerFacing);
        distanceSquared = targetDirection.lengthSquared();

        score = 0;
        if (maxAngle > 0 && angle > maxAngle) return;
        if (maxDistanceSquared > 0 && distanceSquared > maxDistanceSquared) return;
        if (distanceSquared < minDistanceSquared) return;

        if (reverseDistance) {
            distanceSquared = maxDistanceSquared - distanceSquared;
        }

        score = 0;

        if (maxDistanceSquared > 0) score += (maxDistanceSquared - distanceSquared);
        if (angle > 0) score += (3 - angle) * 4;

        // Favor targeting players, a bit
        // TODO: Make this configurable? Offensive spells should prefer mobs, maybe?
        Entity entity = getEntity();
        if (entity != null && mage != null && mage.getController().isNPC(entity))
        {
            score = score - 1;
        }
        else
        if (mage != null)
        {
            score = score + 5;
        }
        else
        if (entity instanceof Player)
        {
            score = score + 3;
        }
        else  if (entity instanceof LivingEntity)
        {
            score = score + 2;
        }
        else
        {
            score = score + 1;
        }
    }

    public int compareTo(Target other)
    {
        return other.score - this.score;
    }

    public boolean hasEntity()
    {
        return getEntity() != null;
    }

    public boolean isValid()
    {
        return location != null;
    }

    public boolean hasTarget()
    {
        return location != null;
    }

    public Entity getEntity()
    {
        return _entity == null ? null : _entity.get();
    }

    public Block getBlock()
    {
        if (location == null)
        {
            return null;
        }

        return location.getBlock();
    }

    public double getDistanceSquared()
    {
        return distanceSquared;
    }

    public Location getLocation()
    {
        return location;
    }

    public void add(Vector offset)
    {
        if (location != null)
        {
            location = location.add(offset);
        }

    }
    
    public void setDirection(Vector direction)
    {
        if (location != null)
        {
            location = location.setDirection(direction);
        }
    }

    public void setWorld(World world)
    {
        if (location != null)
        {
            location.setWorld(world);
        }
    }

    public Object getExtraData()
    {
        return extraData;
    }

    public void setExtraData(Object extraData)
    {
        this.extraData = extraData;
    }

    public void setEntity(Entity entity)
    {
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) {
            this.location = entity.getLocation();
        }
        this.calculateScore();
    }
}