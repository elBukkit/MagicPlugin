package com.elmakers.mine.bukkit.api.utility;

import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;


public class RandomUtils {
	private final static Random random = new Random();
	
	public static float lerp(String[] list, int levelIndex, int nextLevelIndex, float distance) {
		float previousValue = Float.parseFloat(list[levelIndex]);
		float nextValue = Float.parseFloat(list[nextLevelIndex]);
		return previousValue + distance * (nextValue - previousValue);
	}
	
	public static <T extends Object> T weightedRandom(LinkedList<WeightedPair<T>> weightList) {
		if (weightList.size() == 0) return null;
		
		Float maxWeight = weightList.getLast().getThreshold();
		Float selectedWeight = random.nextFloat() * maxWeight;
		for (WeightedPair<T> weight : weightList) {
			if (selectedWeight < weight.getThreshold()) {
				return weight.getValue();
			}
		}
		
		return weightList.getFirst().getValue();
	}
	
    /**
     * This is shamelessly copied from org.bukkit.Location.setDirection.
     * 
     * It's only here for 1.6 backwards compatibility. It could and should
     * be removed eventually, favoring use of the Bukkit API version.
     * 
     * Yay, open source!
     * 
     * Sets the {@link #getYaw() yaw} and {@link #getPitch() pitch} to point
     * in the direction of the vector.
     */
    public static Location setDirection(Location location, Vector vector) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();

        if (x == 0 && z == 0) {
            location.setPitch(vector.getY() > 0 ? -90 : 90);
            return location;
        }

        double theta = Math.atan2(-x, z);
        location.setYaw((float) Math.toDegrees((theta + _2PI) % _2PI));

        double x2 = NumberConversions.square(x);
        double z2 = NumberConversions.square(z);
        double xz = Math.sqrt(x2 + z2);
        location.setPitch((float) Math.toDegrees(Math.atan(-vector.getY() / xz)));

        return location;
    }
}
