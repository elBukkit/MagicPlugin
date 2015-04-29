package com.elmakers.mine.bukkit.utility;

import org.bukkit.util.Vector;

public class BoundingBox
{
    private final Vector min;
    private final Vector max;

    public BoundingBox(Vector min, Vector max)
    {
        this.min = min.clone();
        this.max = max.clone();
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

    protected Vector getIntersection(double fDst1, double fDst2, Vector P1, Vector P2) {
        if ((fDst1 * fDst2) >= 0.0f) return null;
        if (fDst1 == fDst2) return null;
        Vector P2_clone = P2.clone();
        return P1.clone().add(P2_clone.subtract(P1).multiply(-fDst1 / (fDst2 - fDst1)));
    }

    protected boolean inBox(Vector hit, int axis) {
    if (axis==1 && hit.getZ() > min.getZ() && hit.getZ() < max.getZ() && hit.getY() > min.getY() && hit.getY() < max.getY()) return true;
    if (axis==2 && hit.getZ() > min.getZ() && hit.getZ() < max.getZ() && hit.getX() > min.getX() && hit.getX() < max.getX()) return true;
    if (axis==3 && hit.getX() > min.getX() && hit.getX() < max.getX() && hit.getY() > min.getY() && hit.getY() < max.getY()) return true;
    return false;
}

    public Vector getIntersection(Vector p1, Vector p2)
    {
        Vector hit = getIntersection(p1.getX() - min.getX(), p2.getX() - min.getX(), p1, p2);
        if (hit != null && inBox(hit, 1)) return hit;
        hit = getIntersection(p1.getY() - min.getY(), p2.getY() - min.getY(), p1, p2);
        if (hit != null && inBox(hit, 2)) return hit;
        hit = getIntersection(p1.getZ() - min.getZ(), p2.getZ() - min.getZ(), p1, p2);
        if (hit != null && inBox(hit, 3)) return hit;
        hit = getIntersection(p1.getX() - max.getX(), p2.getX() - max.getX(), p1, p2);
        if (hit != null && inBox(hit, 1)) return hit;
        hit = getIntersection(p1.getY() - max.getY(), p2.getY() - max.getY(), p1, p2);
        if (hit != null && inBox(hit, 2)) return hit;
        hit = getIntersection(p1.getZ() - max.getZ(), p2.getZ() - max.getZ(), p1, p2);
        if (hit != null && inBox(hit, 3)) return hit;
        return null;
    }

    @Override
    public String toString()
    {
        return "[" + min.toString() + " - " + max.toString() + "]";
    }
}
