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
    private static final boolean DEBUG_TARGETING = false;

    protected int    maxDistanceSquared = 128 * 128;
    protected int    minDistanceSquared = 0;
    protected double maxAngle           = 0.3;
    protected boolean useHitbox         = false;

    protected double closeDistanceSquared = 1;
    protected double closeAngle = Math.PI / 2;

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
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, boolean hitbox)
    {
        this.maxDistanceSquared = range * range;
        this.source = sourceLocation;
        this.useHitbox = hitbox;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, double closeRange, double closeAngle)
    {
        this.closeDistanceSquared = closeRange * closeRange;
        this.closeAngle = closeAngle;
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
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
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
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
        if (mage != null) this.location = mage.getEyeLocation();
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (entity != null) this.location = CompatibilityUtils.getEyeLocation(entity);
    }

    public Target(Location sourceLocation, Entity entity, Block block)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this._entity = new WeakReference<Entity>(entity);
        if (block != null) {
            this.location = block.getLocation();
        } else if (entity != null) {
            this.location = CompatibilityUtils.getEyeLocation(entity);
        }
    }

    public int getScore()
    {
        return score;
    }

    protected void calculateScore()
    {
        score = 0;
        if (source == null) return;

        Vector playerFacing = source.getDirection();
        Vector playerLoc = new Vector(source.getX(), source.getY(), source.getZ());

        Location targetLocation = getLocation();
        if (targetLocation == null) return;

        Vector targetLoc = new Vector(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
        Vector targetDirection = new Vector(targetLoc.getX() - playerLoc.getX(), targetLoc.getY() - playerLoc.getY(), targetLoc.getZ() - playerLoc.getZ());
        distanceSquared = targetDirection.lengthSquared();

        if (maxDistanceSquared > 0 && distanceSquared > maxDistanceSquared) return;
        if (distanceSquared < minDistanceSquared) return;

        Entity entity = getEntity();
        if (useHitbox)
        {
            Vector playerMaxRange = playerLoc.clone().add(playerFacing.multiply(maxDistanceSquared));
            BoundingBox hitbox = null;
            if (entity == null)
            {
                hitbox =  new BoundingBox(targetLoc, -0.5, 0.5, 0, 1, -0.5, 0.5);
            }
            else
            {
                hitbox = CompatibilityUtils.getHitbox(entity);

                if (DEBUG_TARGETING)
                {
                    org.bukkit.Bukkit.getLogger().info("CHECKING " + getEntity().getType() + ": " + hitbox + ", " + playerLoc + " - " + playerMaxRange + ": " + hitbox.intersectsLine(playerLoc, playerMaxRange));
                }
            }
            if (!hitbox.intersectsLine(playerLoc, playerMaxRange))
            {
                return;
            }
        }
        else
        {
            angle = targetDirection.angle(playerFacing);

            double checkAngle = maxAngle;
            if (closeDistanceSquared > 0 && maxDistanceSquared > closeDistanceSquared)
            {
                if (distanceSquared <= closeDistanceSquared) {
                    checkAngle = closeAngle;
                } else {
                    double ratio = (distanceSquared - closeDistanceSquared) / (maxDistanceSquared - closeDistanceSquared);
                    checkAngle = closeAngle - ratio * (closeAngle - maxAngle);
                }
            }

            if (DEBUG_TARGETING && hasEntity())
            {
                org.bukkit.Bukkit.getLogger().info("CHECKING " + getEntity().getType() + " (" + closeDistanceSquared + ") " +
                        " angle = " + angle + " against " + checkAngle + " at distance " + distanceSquared
                        + " rangeA = (" + closeAngle + " to " + maxAngle + "), rangeD = (" + closeDistanceSquared + " to " + maxDistanceSquared + ")"
                        + " ... from " + playerLoc + " to " + targetLoc);
            }


            if (checkAngle > 0 && angle > checkAngle) return;
        }

        if (reverseDistance) {
            distanceSquared = maxDistanceSquared - distanceSquared;
        }

        score = 1;
        if (maxDistanceSquared > 0) score += (maxDistanceSquared - distanceSquared);
        if (!useHitbox && angle > 0) score += (3 - angle) * 4;

        // Favor targeting players, a bit
        // TODO: Make this configurable? Offensive spells should prefer mobs, maybe?
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