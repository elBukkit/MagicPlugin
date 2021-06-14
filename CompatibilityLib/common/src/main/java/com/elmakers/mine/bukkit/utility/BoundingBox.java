package com.elmakers.mine.bukkit.utility;

import javax.annotation.Nullable;

import org.bukkit.util.Vector;

public class BoundingBox
{
    private final Vector min;
    private final Vector max;

    public BoundingBox(Vector min, Vector max)
    {
        this.min = new Vector(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
        this.max = new Vector(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
    }

    public BoundingBox(double dMinX, double dMaxX, double dMinY, double dMaxY, double dMinZ, double dMaxZ)
    {
        this.min = new Vector(dMinX, dMinY, dMinZ);
        this.max = new Vector(dMaxX, dMaxY, dMaxZ);
    }

    public BoundingBox(Vector center, double dMinX, double dMaxX, double dMinY, double dMaxY, double dMinZ, double dMaxZ)
    {
        this.min = new Vector(center.getX() + dMinX, center.getY() + dMinY, center.getZ() + dMinZ);
        this.max = new Vector(center.getX() + dMaxX, center.getY() + dMaxY, center.getZ() + dMaxZ);
    }

    public BoundingBox center(Vector center)
    {
        BoundingBox results = new BoundingBox(min, max);
        results.min.add(center);
        results.max.add(center);
        return results;
    }

    public Vector center() {
        return this.max.clone().add(this.min).multiply(0.5);
    }

    public boolean contains(Vector point) {
        return this.min.getX() <= point.getX() && point.getX() <= this.max.getX()
                && this.min.getY() <= point.getY() && point.getY() <= this.max.getY()
                && this.min.getZ() <= point.getZ() && point.getZ() <= this.max.getZ();
    }

    public BoundingBox scale(double scale)
    {
        if (scale <= 0 || scale == 1) return this;
        Vector center = this.center();

        this.min.setX((this.min.getX() - center.getX()) * scale + center.getX());
        this.min.setY((this.min.getY() - center.getY()) * scale + center.getY());
        this.min.setZ((this.min.getZ() - center.getZ()) * scale + center.getZ());
        this.max.setX((this.max.getX() - center.getX()) * scale + center.getX());
        this.max.setY((this.max.getY() - center.getY()) * scale + center.getY());
        this.max.setZ((this.max.getZ() - center.getZ()) * scale + center.getZ());
        return this;
    }

    /**
     * Scale this BoundingBox, but keep the min-Y value constant.
     *
     * <p>Useful for scaling entity AABB's.
     *
     * @return the scaled BB (this object)
     */
    public BoundingBox scaleFromBase(double scale, double scaleY)
    {
        if (scale <= 0 || scale == 1) return this;
        Vector center = this.center();

        this.min.setX((this.min.getX() - center.getX()) * scale + center.getX());
        // We just skip setting minY, scaling Y only upward
        this.min.setZ((this.min.getZ() - center.getZ()) * scale + center.getZ());
        this.max.setX((this.max.getX() - center.getX()) * scale + center.getX());
        this.max.setY((this.max.getY() - center.getY()) * scaleY + center.getY());
        this.max.setZ((this.max.getZ() - center.getZ()) * scale + center.getZ());
        return this;
    }

    public BoundingBox expand(double size) {
        this.min.setX(this.min.getX() - size);
        this.min.setY(this.min.getY() - size);
        this.min.setZ(this.min.getZ() - size);
        this.max.setX(this.max.getX() + size);
        this.max.setY(this.max.getY() + size);
        this.max.setZ(this.max.getZ() + size);
        return this;
    }

    // Source:
    // [url]http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/[/url]
    public boolean intersectsLine(Vector p1, Vector p2)
    {
        final double epsilon = 0.0001f;

        p1 = p1.clone();
        p2 = p2.clone();
        Vector d = p2.subtract(p1).multiply(0.5);
        Vector e = max.clone().subtract(min).multiply(0.5);
        Vector c = p1.add(d).subtract(min.clone().add(max).multiply(0.5));
        Vector ad = new Vector(Math.abs(d.getX()), Math.abs(d.getY()), Math.abs(d.getZ()));

        if (Math.abs(c.getX()) > e.getX() + ad.getX())
            return false;
        if (Math.abs(c.getY()) > e.getY() + ad.getY())
            return false;
        if (Math.abs(c.getZ()) > e.getZ() + ad.getZ())
            return false;

        if (Math.abs(d.getY() * c.getZ() - d.getZ() * c.getY()) > e.getY() * ad.getZ() + e.getZ() * ad.getY() + epsilon)
            return false;
        if (Math.abs(d.getZ() * c.getX() - d.getX() * c.getZ()) > e.getZ() * ad.getX() + e.getX() * ad.getZ() + epsilon)
            return false;
        if (Math.abs(d.getX() * c.getY() - d.getY() * c.getX()) > e.getX() * ad.getY() + e.getY() * ad.getX() + epsilon)
            return false;

        return true;
    }

    @Nullable
    protected Vector getIntersection(double fDst1, double fDst2, Vector p1, Vector p2, int side) {
        if ((fDst1 * fDst2) >= 0.0f) return null;
        if (fDst1 == fDst2) return null;
        Vector p2Clone = p2.clone();
        p2Clone = p1.clone().add(p2Clone.subtract(p1).multiply(-fDst1 / (fDst2 - fDst1)));
        return inBox(p2Clone, side) ? p2Clone : null;
    }

    @Nullable
    public Vector getIntersection(Vector p1, Vector p2) {
        Vector currentHit = getIntersection(p1.getX() - min.getX(), p2.getX() - min.getX(), p1, p2, 1);
        Vector hit = getIntersection(p1.getY() - min.getY(), p2.getY() - min.getY(), p1, p2, 2);
        if (currentHit != null && hit != null) {
            if (currentHit.distanceSquared(p1) < hit.distanceSquared(p1)) {
                return currentHit;
            } else {
                return hit;
            }
        } else if (currentHit == null) {
            currentHit = hit;
        }

        hit = getIntersection(p1.getZ() - min.getZ(), p2.getZ() - min.getZ(), p1, p2, 3);
        if (currentHit != null && hit != null) {
            if (currentHit.distanceSquared(p1) < hit.distanceSquared(p1)) {
                return currentHit;
            } else {
                return hit;
            }
        } else if (currentHit == null) {
            currentHit = hit;
        }

        hit = getIntersection(p1.getX() - max.getX(), p2.getX() - max.getX(), p1, p2, 1);
        if (currentHit != null && hit != null) {
            if (currentHit.distanceSquared(p1) < hit.distanceSquared(p1)) {
                return currentHit;
            } else {
                return hit;
            }
        } else if (currentHit == null) {
            currentHit = hit;
        }

        hit = getIntersection(p1.getY() - max.getY(), p2.getY() - max.getY(), p1, p2, 2);
        if (currentHit != null && hit != null) {
            if (currentHit.distanceSquared(p1) < hit.distanceSquared(p1)) {
                return currentHit;
            } else {
                return hit;
            }
        } else if (currentHit == null) {
            currentHit = hit;
        }

        hit = getIntersection(p1.getZ() - max.getZ(), p2.getZ() - max.getZ(), p1, p2, 3);
        if (currentHit != null && hit != null) {
            if (currentHit.distanceSquared(p1) < hit.distanceSquared(p1)) {
                return currentHit;
            } else {
                return hit;
            }
        } else if (hit != null) {
            return hit;
        }
        return currentHit;
    }


    protected boolean inBox(Vector hit, int axis) {
        if (axis == 1 && hit.getZ() > min.getZ() && hit.getZ() < max.getZ() && hit.getY() > min.getY() && hit.getY() < max.getY()) return true;
        if (axis == 2 && hit.getZ() > min.getZ() && hit.getZ() < max.getZ() && hit.getX() > min.getX() && hit.getX() < max.getX()) return true;
        if (axis == 3 && hit.getX() > min.getX() && hit.getX() < max.getX() && hit.getY() > min.getY() && hit.getY() < max.getY()) return true;
        return false;
    }

    public Vector size()
    {
        return this.max.clone().subtract(this.min);
    }

    @Override
    public String toString()
    {
        return "[" + min.toString() + " - " + max.toString() + "] (" + (max.getX() - min.getX()) + "x" + (max.getY() - min.getY()) + "x" + (max.getZ() - min.getZ()) + ")";
    }
}
