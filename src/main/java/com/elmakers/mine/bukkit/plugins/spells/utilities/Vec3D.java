package com.elmakers.mine.bukkit.plugins.spells.utilities;


import java.util.ArrayList;
import java.util.List;

public class Vec3D
{

    public static Vec3D createVectorHelper(double d, double d1, double d2)
    {
        return new Vec3D(d, d1, d2);
    }

    public static void initialize()
    {
        nextVector = 0;
    }

    public static Vec3D createVector(double d, double d1, double d2)
    {
        if(nextVector >= vectorList.size())
        {
            vectorList.add(createVectorHelper(0.0D, 0.0D, 0.0D));
        }
        return ((Vec3D)vectorList.get(nextVector++)).setComponents(d, d1, d2);
    }

    private Vec3D(double d, double d1, double d2)
    {
        if(d == -0D)
        {
            d = 0.0D;
        }
        if(d1 == -0D)
        {
            d1 = 0.0D;
        }
        if(d2 == -0D)
        {
            d2 = 0.0D;
        }
        xCoord = d;
        yCoord = d1;
        zCoord = d2;
    }

    private Vec3D setComponents(double d, double d1, double d2)
    {
        xCoord = d;
        yCoord = d1;
        zCoord = d2;
        return this;
    }

    public Vec3D normalize()
    {
        double d = MathHelper.sqrt_double(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
        if(d < 0.0001D)
        {
            return createVector(0.0D, 0.0D, 0.0D);
        } else
        {
            return createVector(xCoord / d, yCoord / d, zCoord / d);
        }
    }

    public Vec3D addVector(double d, double d1, double d2)
    {
        return createVector(xCoord + d, yCoord + d1, zCoord + d2);
    }

    public double distanceTo(Vec3D vec3d)
    {
        double d = vec3d.xCoord - xCoord;
        double d1 = vec3d.yCoord - yCoord;
        double d2 = vec3d.zCoord - zCoord;
        return (double)MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
    }

    public double squareDistanceTo(Vec3D vec3d)
    {
        double d = vec3d.xCoord - xCoord;
        double d1 = vec3d.yCoord - yCoord;
        double d2 = vec3d.zCoord - zCoord;
        return d * d + d1 * d1 + d2 * d2;
    }

    public double squareDistanceTo(double d, double d1, double d2)
    {
        double d3 = d - xCoord;
        double d4 = d1 - yCoord;
        double d5 = d2 - zCoord;
        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double lengthVector()
    {
        return (double)MathHelper.sqrt_double(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
    }

    public Vec3D getIntermediateWithXValue(Vec3D vec3d, double d)
    {
        double d1 = vec3d.xCoord - xCoord;
        double d2 = vec3d.yCoord - yCoord;
        double d3 = vec3d.zCoord - zCoord;
        if(d1 * d1 < 1.0000000116860974E-007D)
        {
            return null;
        }
        double d4 = (d - xCoord) / d1;
        if(d4 < 0.0D || d4 > 1.0D)
        {
            return null;
        } else
        {
            return createVector(xCoord + d1 * d4, yCoord + d2 * d4, zCoord + d3 * d4);
        }
    }

    public Vec3D getIntermediateWithYValue(Vec3D vec3d, double d)
    {
        double d1 = vec3d.xCoord - xCoord;
        double d2 = vec3d.yCoord - yCoord;
        double d3 = vec3d.zCoord - zCoord;
        if(d2 * d2 < 1.0000000116860974E-007D)
        {
            return null;
        }
        double d4 = (d - yCoord) / d2;
        if(d4 < 0.0D || d4 > 1.0D)
        {
            return null;
        } else
        {
            return createVector(xCoord + d1 * d4, yCoord + d2 * d4, zCoord + d3 * d4);
        }
    }

    public Vec3D getIntermediateWithZValue(Vec3D vec3d, double d)
    {
        double d1 = vec3d.xCoord - xCoord;
        double d2 = vec3d.yCoord - yCoord;
        double d3 = vec3d.zCoord - zCoord;
        if(d3 * d3 < 1.0000000116860974E-007D)
        {
            return null;
        }
        double d4 = (d - zCoord) / d3;
        if(d4 < 0.0D || d4 > 1.0D)
        {
            return null;
        } else
        {
            return createVector(xCoord + d1 * d4, yCoord + d2 * d4, zCoord + d3 * d4);
        }
    }

    public String toString()
    {
        return (new StringBuilder()).append("(").append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord).append(")").toString();
    }

    private static List<Vec3D> vectorList = new ArrayList<Vec3D>();
    private static int nextVector = 0;
    public double xCoord;
    public double yCoord;
    public double zCoord;

}
