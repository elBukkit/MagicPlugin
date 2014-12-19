package com.elmakers.mine.bukkit.utility;

import org.bukkit.util.Vector;

public class BoundingBox
{
    private final Vector min;
    private final Vector max;

    public BoundingBox(Vector min, Vector max)
    {
        this.min = min;
        this.max = max;
    }

    public BoundingBox(Vector center, double dMinX, double dMaxX, double dMinY, double dMaxY, double dMinZ, double dMaxZ)
    {
        this.min = new Vector(center.getX() + dMinX, center.getY() + dMinY, center.getZ() + dMinZ);
        this.max = new Vector(center.getX() + dMaxX, center.getY() + dMaxY, center.getZ() + dMaxZ);
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

    @Override
    public String toString()
    {
        return "[" + min.toString() + " - " + max.toString() + "]";
    }
}
