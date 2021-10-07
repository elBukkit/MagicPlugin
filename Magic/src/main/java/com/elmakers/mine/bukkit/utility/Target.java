package com.elmakers.mine.bukkit.utility;

import java.lang.ref.WeakReference;
import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class Target implements Comparable<Target>
{
    public static boolean DEBUG_TARGETING = false;
    private static final double BLOCK_HIT_PADDING = 0.00000001;

    protected int    maxDistanceSquared = 128 * 128;
    protected int    minDistanceSquared = 0;
    protected double maxAngle           = 0.3;
    protected boolean useHitbox         = false;
    protected double hitboxPadding      = 0;

    protected float distanceWeight = 1;
    protected float fovWeight = 4;
    protected int npcWeight = -1;
    protected int playerWeight = 4;
    protected int livingEntityWeight = 3;
    protected int mageWeight = 5;

    protected double closeDistanceSquared = 0;
    protected double closeAngle = Math.PI / 2;

    private Location source;
    private Location location;
    private MaterialAndData locationMaterial;
    private WeakReference<Entity> entityRef;
    private WeakReference<Mage>   mageRef;
    private boolean reverseDistance = false;
    private boolean atMaxRange = false;

    private double   distanceSquared    = 100000;
    private double   angle              = 10000;
    private int      score              = 0;

    private Object     extraData     = null;

    public Target(Location sourceLocation)
    {
        this.source = sourceLocation;
    }

    public void setBlock(Block block)
    {
        if (block != null) {
            this.location = block.getLocation();
            this.location.add(0.5, 0.5, 0.5);
        }
    }

    public Target(Location sourceLocation, Block block)
    {
        this.source = sourceLocation;
        getSourceMaterial(block);
        this.setBlock(block);
        calculateScore();
    }

    public Target(Location sourceLocation, Block block, boolean hitbox, double hitboxPadding)
    {
        this.source = sourceLocation;
        getSourceMaterial(block);
        this.useHitbox = hitbox;
        this.hitboxPadding = hitboxPadding;
        this.setBlock(block);
        calculateScore();
    }

    public Target(Location sourceLocation, Location targetLocation, boolean hitbox, double hitboxPadding)
    {
        this.source = sourceLocation;
        getSourceMaterial(targetLocation.getBlock());
        this.useHitbox = hitbox;
        this.hitboxPadding = hitboxPadding;
        this.location = targetLocation;
        this.atMaxRange = true;
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
        this.setBlock(block);
        getSourceMaterial(block);
        calculateScore();
    }

    public Target(Location sourceLocation, Block block, int minRange, int maxRange, double angle, float fovWeight, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this.fovWeight = fovWeight;
        this.setBlock(block);
        getSourceMaterial(block);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range)
    {
        this.maxDistanceSquared = range * range;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, boolean hitbox, double hitboxPadding)
    {
        this.maxDistanceSquared = range * range;
        this.source = sourceLocation;
        this.useHitbox = hitbox;
        this.hitboxPadding = hitboxPadding;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, double closeRange, double closeAngle,
                  float distanceWeight, float fovWeight, int mageWeight, int npcWeight, int playerWeight, int livingEntityWeight)
    {
        this.closeDistanceSquared = closeRange * closeRange;
        this.closeAngle = closeAngle;
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        this.distanceWeight = distanceWeight;
        this.fovWeight = fovWeight;
        this.mageWeight = mageWeight;
        this.npcWeight = npcWeight;
        this.playerWeight = playerWeight;
        this.livingEntityWeight = livingEntityWeight;

        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, double closeRange, double closeAngle)
    {
        this.closeDistanceSquared = closeRange * closeRange;
        this.closeAngle = closeAngle;
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int range, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = range * range;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int minRange, int maxRange, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity, int minRange, int maxRange, double angle, int playerWeight, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.playerWeight = playerWeight;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        calculateScore();
    }

    public Target(Location sourceLocation, Mage mage, int minRange, int maxRange, double angle, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        if (mage != null) {
            this.mageRef = new WeakReference<>(mage);
            this.entityRef = new WeakReference<>(mage.getLivingEntity());
            this.location = mage.getEyeLocation();
        }
        calculateScore();
    }

    public Target(Location sourceLocation, Mage mage, int minRange, int maxRange, double angle, int playerWeight, boolean reverseDistance)
    {
        this.maxDistanceSquared = maxRange * maxRange;
        this.minDistanceSquared = minRange * minRange;
        this.maxAngle = angle;
        this.reverseDistance = reverseDistance;
        this.source = sourceLocation;
        this.playerWeight = playerWeight;
        if (mage != null) {
            this.mageRef = new WeakReference<>(mage);
            this.entityRef = new WeakReference<>(mage.getLivingEntity());
            this.location = mage.getEyeLocation();
        }
        calculateScore();
    }

    public Target(Location sourceLocation, Entity entity)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
    }

    public Target(Location sourceLocation, Entity entity, Block block)
    {
        this.maxDistanceSquared = 0;
        this.source = sourceLocation;
        this.entityRef = new WeakReference<>(entity);
        if (block != null) {
            this.setBlock(block);
        } else if (entity != null) {
            this.location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        }
    }

    protected void getSourceMaterial(Block block) {
        if (CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) {
            this.locationMaterial = new MaterialAndData(block);
        }
    }

    @Nullable
    protected Mage getMage() {
        return mageRef == null ? null : mageRef.get();
    }

    public int getScore()
    {
        return score;
    }


    public Vector getIntersection(Block block, Vector startPoint, Vector endPoint) {
        return getIntersection(block, startPoint, endPoint, hitboxPadding);
    }

    public static Vector getIntersection(Block block, Vector startPoint, Vector endPoint, double hitboxPadding) {
        Collection<BoundingBox> hitboxes = CompatibilityLib.getCompatibilityUtils().getBoundingBoxes(block);
        for (BoundingBox hitbox : hitboxes) {
            if (hitboxPadding > 0) {
                hitbox.expand(hitboxPadding);
            }

            // This is a more efficient check as a first-pass
            if (!hitbox.intersectsLine(startPoint, endPoint)) {
                continue;
            }

            Vector hit = hitbox.getIntersection(startPoint, endPoint);
            if (hit != null) {

                // Make sure the hit location is inside of the block
                // I tried using Double.MIN_NORMAL but it didn't work.
                hit.subtract(block.getLocation().toVector());
                hit.setX((Math.max(BLOCK_HIT_PADDING, Math.min(1.0 - BLOCK_HIT_PADDING, hit.getX()))));
                hit.setY((Math.max(BLOCK_HIT_PADDING, Math.min(1.0 - BLOCK_HIT_PADDING, hit.getY()))));
                hit.setZ((Math.max(BLOCK_HIT_PADDING, Math.min(1.0 - BLOCK_HIT_PADDING, hit.getZ()))));
                hit.add(block.getLocation().toVector());

                return hit;
            }
        }
        return null;
    }

    protected void calculateScore()
    {
        score = 0;
        if (source == null) return;

        Vector sourceDirection = source.getDirection();
        Vector sourceLocation = source.toVector();

        Location targetLocation = getLocation();
        if (targetLocation == null) return;

        Vector targetLoc = targetLocation.toVector();
        Vector targetDirection = new Vector(targetLoc.getX() - sourceLocation.getX(), targetLoc.getY() - sourceLocation.getY(), targetLoc.getZ() - sourceLocation.getZ());
        distanceSquared = targetDirection.lengthSquared();

        if (maxDistanceSquared > 0 && distanceSquared > maxDistanceSquared) return;
        if (distanceSquared < minDistanceSquared) return;

        Entity entity = getEntity();
        if (useHitbox)
        {
            double checkDistance = Math.max(maxDistanceSquared, 2);
            Vector endPoint = sourceLocation.clone().add(sourceDirection.clone().multiply(checkDistance));
            // Back up just a wee bit
            Vector startPoint = sourceLocation.clone().add(sourceDirection.multiply(-0.1));
            Vector hit = null;
            if (entity != null) {
                BoundingBox hitbox = HitboxUtils.getHitbox(entity);
                if (hitbox == null) {
                    if (DEBUG_TARGETING) {
                        org.bukkit.Bukkit.getLogger().info(" failed to get hitbox for " + entity.getType() + " : " + targetLoc);
                    }
                    // Create a placeholder hitbox assuming from entity eye location
                    hitbox = new BoundingBox(targetLoc, -0.5, 0.5, -1, 0.5, -0.5, 0.5);
                }

                // Compute hit location
                if (hitboxPadding > 0) {
                    hitbox.expand(hitboxPadding);
                }

                if (DEBUG_TARGETING) {
                    org.bukkit.Bukkit.getLogger().info("CHECKING " + entity.getType() + ": " + hitbox + ", " + startPoint + " - " + endPoint + ": " + hitbox.intersectsLine(sourceLocation, endPoint));
                }

                if (hitbox.intersectsLine(startPoint, endPoint)) {
                    hit = hitbox.getIntersection(startPoint, endPoint);
                } else if (DEBUG_TARGETING) {
                    org.bukkit.Bukkit.getLogger().info(" block hitbox test failed from " + sourceLocation);
                }
            } else if (!atMaxRange) {
                hit = getIntersection(getBlock(), startPoint, endPoint);
                if (DEBUG_TARGETING && hit == null) {
                    org.bukkit.Bukkit.getLogger().info(" Failed to get intersection for block " + getBlock().getType()
                        + " from " + startPoint.toBlockVector() + " to " + endPoint.toBlockVector()
                        + " rot: " + sourceDirection);
                }
            }
            if (hit == null && (entity != null || !atMaxRange)) {
                return;
            }
            if (hit != null) {
                location.setX(hit.getX());
                location.setY(hit.getY());
                location.setZ(hit.getZ());
            }

            if (location.getWorld().equals(source.getWorld()))
            {
                distanceSquared = location.distanceSquared(source);
            }
            if (DEBUG_TARGETING)
            {
                if (entity != null) {
                    org.bukkit.Bukkit.getLogger().info("HIT Entity: " + hit);
                } else if (atMaxRange) {
                    org.bukkit.Bukkit.getLogger().info("MAX RANGE: " + hit);
                } else {
                    org.bukkit.Bukkit.getLogger().info("HIT: " + hit);
                }
            }
        }
        else if (maxAngle > 0)
        {
            angle = targetDirection.angle(sourceDirection);

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
                org.bukkit.Bukkit.getLogger().info("CHECKING " + getEntity().getType() + " (" + closeDistanceSquared + ") "
                        + " angle = " + angle + " against " + checkAngle + " at distance " + distanceSquared
                        + " rangeA = (" + closeAngle + " to " + maxAngle + "), rangeD = (" + closeDistanceSquared + " to " + maxDistanceSquared + ")"
                        + " ... from " + sourceLocation + " to " + targetLoc);
            }

            if (checkAngle > 0 && angle > checkAngle) return;
        }

        if (reverseDistance) {
            distanceSquared = maxDistanceSquared - distanceSquared;
        }

        score = 1;

        // Apply scoring weights
        if (maxDistanceSquared > 0) score = (int)(score + (maxDistanceSquared - distanceSquared) * distanceWeight);
        if (!useHitbox && angle > 0 && maxAngle > 0) score = (int)(score + (3 - angle) * fovWeight);

        Mage mage = getMage();
        if (entity != null && mage != null && mage.getController().isNPC(entity))
        {
            score = score + npcWeight;
        }
        else
        if (entity instanceof Player)
        {
            score = score + playerWeight;
        }
        else
        if (mage != null)
        {
            score = score + mageWeight;
        }
        else  if (entity instanceof LivingEntity)
        {
            score = score + livingEntityWeight;
        }

        if (DEBUG_TARGETING && entity != null) {
            org.bukkit.Bukkit.getLogger().info("TARGETED " + entity.getType() + ": r2=" + distanceSquared + " (" + distanceWeight + "), a=" + angle + " (" + fovWeight + "), score: " + score);
        }
    }

    @Override
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

    @Nullable
    public Entity getEntity() {
        return entityRef == null ? null : entityRef.get();
    }

    @Nullable
    public Block getBlock() {
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

    public double getAngle() {
        return angle;
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
        this.entityRef = new WeakReference<>(entity);
        if (entity != null) {
            this.location = entity.getLocation();
        }
        this.calculateScore();
    }

    public MaterialAndData getTargetedMaterial() {
        return locationMaterial;
    }

    public void update() {
        Entity entity = getEntity();
        if (entity != null) {
            location = CompatibilityLib.getCompatibilityUtils().getEyeLocation(entity);
        } else {
            Mage mage = getMage();
            if (mage != null) {
                location = mage.getEyeLocation();
            }
        }
        calculateScore();
    }
}
