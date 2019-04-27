package com.elmakers.mine.bukkit.utility;

public class Mercator {
    private static final double R_MAJOR = 6378137.0;
    private static final double R_MINOR = 6356752.3142;

    public static double[] merc(double x, double y) {
        return new double[] {mercX(x), mercY(y)};
    }

    public static double  mercX(double lon) {
        return R_MAJOR * Math.toRadians(lon);
    }

    public static double mercY(double lat) {
        if (lat > 89.5) {
            lat = 89.5;
        }
        if (lat < -89.5) {
            lat = -89.5;
        }
        double temp = R_MINOR / R_MAJOR;
        double es = 1.0 - (temp * temp);
        double eccent = Math.sqrt(es);
        double phi = Math.toRadians(lat);
        double sinphi = Math.sin(phi);
        double con = eccent * sinphi;
        double com = 0.5 * eccent;
        con = Math.pow(((1.0 - con) / (1.0 + con)), com);
        double ts = Math.tan(0.5 * ((Math.PI * 0.5) - phi)) / con;
        double y = 0 - R_MAJOR * Math.log(ts);
        return y;
    }

    public static double y2lat(double aY) {
        return Math.toDegrees(2 * Math.atan(Math.exp(Math.toRadians(aY))) - Math.PI / 2);
    }

    public static double lat2y(double aLat) {
        return Math.toDegrees(Math.log(Math.tan(Math.PI / 4 + Math.toRadians(aLat) / 2)));
    }

    public static double x2lon(double aX) {
        return Math.toDegrees(2 * Math.atan(Math.exp(Math.toRadians(aX))) - Math.PI / 2);
    }

    public static double lon2x(double aLon) {
        return Math.toDegrees(Math.log(Math.tan(Math.PI / 4 + Math.toRadians(aLon) / 2)));
    }
}
