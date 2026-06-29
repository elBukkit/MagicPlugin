package com.elmakers.mine.bukkit.utility.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SplittableRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

/**
 * Contains some general Randomization utilities, including
 * computing WeightedPair - based probability decisions.
 */
public class RandomUtils {
    private static final Random random = new Random();

    public static float lerp(String[] list, int levelIndex, int nextLevelIndex, double distance) {
        if (list.length == 0) return 0;
        if (levelIndex >= list.length) return Float.parseFloat(list[list.length - 1]);
        if (nextLevelIndex >= list.length) return Float.parseFloat(list[list.length - 1]);
        float previousValue = Float.parseFloat(list[levelIndex]);
        float nextValue = Float.parseFloat(list[nextLevelIndex]);
        return lerp(previousValue, nextValue, distance);
    }

    public static int lerp(int min, int max, double value) {
        if (max == min) return min;
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return (int)Math.round((double)min + value * (double) (max - min));
    }

    public static long lerp(long min, long max, double value) {
        if (max == min) return min;
        if (min > max) {
            long temp = min;
            min = max;
            max = temp;
        }
        return (long)Math.round((double)min + value * (double) (max - min));
    }

    public static double lerp(double min, double max, double value) {
        if (max == min) return min;
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return min + value * (max - min);
    }

    public static float lerp(float min, float max, double value) {
        return (float)(min + value * (max - min));
    }

    @Nullable
    public static <T extends Object> T weightedRandom(List<WeightedPair<T>> weightList) {
        return weightedRandom(random, weightList);
    }

    @Nullable
    public static <T extends Object> T weightedRandom(Random random, List<WeightedPair<T>> weightList) {
        if (weightList == null || weightList.size() == 0) return null;

        Float maxWeight = weightList.get(weightList.size() - 1).getThreshold();
        Float selectedWeight = random.nextFloat() * maxWeight;
        for (WeightedPair<T> weight : weightList) {
            if (selectedWeight < weight.getThreshold()) {
                return weight.getValue();
            }
        }

        return weightList.get(0).getValue();
    }

    @Nonnull
    public static <T extends Object> Collection<T> getValues(List<WeightedPair<T>> weightList) {
        List<T> list = new ArrayList<>();
        for (WeightedPair<T> pair : weightList) {
            list.add(pair.getValue());
        }
        return list;
    }

    public static void populateIntegerProbabilityMap(List<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.INTEGER, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateIntegerProbabilityMap(List<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.INTEGER, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static void populateIntegerProbabilityMap(List<WeightedPair<Integer>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.INTEGER, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else if (parent.isInt(key)) {
            populateProbabilityConstant(ValueParser.INTEGER, probabilityMap, parent.getString(key));
        } else {
            populateProbabilityList(ValueParser.INTEGER, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateStringProbabilityMap(List<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.STRING, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateStringProbabilityMap(List<WeightedPair<String>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.STRING, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else {
            populateProbabilityList(ValueParser.STRING, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateStringProbabilityMap(List<WeightedPair<String>> probabilityMap, ConfigurationSection parent, String key) {
        populateStringProbabilityMap(probabilityMap, parent, key, 0, 0,0);
    }

    public static void populateStringProbabilityMap(List<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.STRING, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static List<WeightedPair<String>> createStringProbabilityMap(ConfigurationSection parent, String key) {
        return createProbabilityMap(ValueParser.STRING, parent, key);
    }

    public static void populateStringProbabilityList(List<WeightedPair<String>> probabilityMap, List<String> stringList) {
        populateProbabilityList(ValueParser.STRING, probabilityMap, stringList);
    }

    public static void populateFloatProbabilityMap(List<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.FLOAT, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateFloatProbabilityMap(List<WeightedPair<Float>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.FLOAT, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else if (parent.isDouble(key) || parent.isInt(key)) {
            populateProbabilityConstant(ValueParser.FLOAT, probabilityMap, parent.getString(key));
        } else {
            populateProbabilityList(ValueParser.FLOAT, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateFloatProbabilityMap(List<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.FLOAT, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static <T extends Object> List<WeightedPair<T>> createProbabilityConstant(T value) {
        if (value == null) return null;
        List<WeightedPair<T>> probability = new ArrayList<>();
        probability.add(new WeightedPair<>(value));
        return probability;
    }

    public static <T extends Object> void populateProbabilityConstant(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, String value) {
        probabilityMap.add(new WeightedPair<>(1.0f, 1.0f, value, parser));
    }

    public static <T extends Object> void populateProbabilityList(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, List<String> keys) {
        if (keys != null) {
            float currentThreshold = 0;
            for (String key : keys) {
                currentThreshold++;
                probabilityMap.add(new WeightedPair<>(currentThreshold, currentThreshold, key, parser));
            }
        }
    }

    public static <T extends Object> List<WeightedPair<T>> createProbabilityMap(ValueParser<T> parser, ConfigurationSection parent, String key) {
        if (parent.get(key) == null) return null;
        List<WeightedPair<T>> probability = new ArrayList<>();
        populateProbabilityMap(parser, probability, parent, key);
        return probability;
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(parser, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, ConfigurationSection parent, String key) {
        populateProbabilityMap(parser, probabilityMap, parent, key, 0, 0, 0);
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(parser, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else {
            populateProbabilityList(parser, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, List<WeightedPair<T>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        Float currentThreshold = 0.0f;

        if (nodeMap != null) {
            Set<String> keys = nodeMap.getKeys(false);
            for (String key : keys) {
                // Kind of a hack, but the yaml parser doesn't like "." in a key.
                String value = nodeMap.getString(key);
                key = key.replace("^", ".");

                String[] pieces = StringUtils.split(value, ',');
                float weight = 0;
                if (pieces != null && pieces.length > 1) {
                    weight = lerp(pieces, levelIndex, nextLevelIndex, distance);
                } else {
                    try {
                        weight = Float.parseFloat(value);
                    } catch (Exception ex) {
                        weight = 1;
                    }
                }
                currentThreshold += weight;
                probabilityMap.add(new WeightedPair<>(currentThreshold, weight, key, parser));
            }
        }
    }

    public static <T extends Object> List<WeightedPair<T>> merge(List<WeightedPair<T>> base, List<WeightedPair<T>> inherit) {
        if (inherit == null || inherit.size() == 0) {
            return base;
        }
        if (base == null) {
            base = new ArrayList<>();
        }
        if (base.size() == 0) {
            base.addAll(inherit);
        } else {
            WeightedPair<T> lastPair = base.get(base.size() - 1);
            float threshold = lastPair.getThreshold();
            for (WeightedPair<T> inheritPair : inherit) {
                float weight = inheritPair.getThreshold();
                threshold += weight;
                base.add(new WeightedPair<>(threshold, inheritPair.getRawThreshold(), inheritPair.getValue()));
            }
        }
        return base;
    }

    @Nullable
    public static String getEntry(String csvList, int index) {
        if (csvList == null) return null;
        String[] pieces = StringUtils.split(csvList, ',');
        if (pieces == null || pieces.length <= 1) return csvList;
        if (index < 0 || index >= pieces.length) return null;
        return pieces[index];
    }

    public static void extrapolateFloatList(List<AscendingPair<Float>> list)
    {
        Collections.sort(list);
        int index = 0;
        while (index < list.size() - 1) {
            AscendingPair<Float> current = list.get(index);
            AscendingPair<Float> next = list.get(index + 1);
            long currentIndex = current.getIndex();
            long nextIndex = next.getIndex();

            index++;
            if (nextIndex > currentIndex + 1) {
                float distance = 1f / (nextIndex - currentIndex);
                AscendingPair<Float> inserted = new AscendingPair<>(currentIndex + 1, lerp(current.getValue(), next.getValue(), distance));
                list.add(index, inserted);
            }
        }
    }

    public static Location randomizeLocation(Location origin, Vector range)
    {
        double xRange = range.getX();
        double yRange = range.getY();
        double zRange = range.getZ();
        origin.add(random.nextDouble() * xRange - xRange / 2, random.nextDouble() * yRange - yRange / 2, random.nextDouble() * zRange - zRange / 2);
        return origin;
    }

    public static <T> T getRandom(List<T> list, int startIndex) {
        return list.get(startIndex + random.nextInt(list.size() - 1));
    }

    public static <T> T getRandom(List<T> list) {
        return getRandom(list, random);
    }

    public static <T> T getRandom(List<T> list, Random random) {
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    public static Random getRandom() {
        return random;
    }

    public static boolean checkProbability(double probability) {
        return probability >= random.nextDouble();
    }

    public static double range(Random random, double min, double max) {
        if (min >= max) {
            return min;
        }
        return min + random.nextDouble(max - min);
    }

    public static int range(Random random, int min, int max) {
        if (min >= max) {
            return min;
        }
        // Bounds is exclusive but we want to include it
        return min + random.nextInt(max - min + 1);
    }

    public static long range(Random random, long min, long max) {
        if (min >= max) {
            return min;
        }
        // Bounds is exclusive but we want to include it
        return min + random.nextLong(max - min + 1);
    }

    public static int range(int min, int max) {
        return range(random, min, max);
    }

    public static double range(double min, double max) {
        return range(random, min, max);
    }

    @Nullable
    public static <T> T getDistanceWeighted(List<DistanceWeightedValue<T>> options, long worldSeed, int chunkX, int chunkZ) {
        final long chunkSeed = worldSeed
                ^ (long) chunkX * 0x9E3779B97F4A7C15L
                ^ (long) chunkZ * 0xD1B54A32D192ED03L;

        double totalWeight = 0;
        final int x = chunkX * 16;
        final int z = chunkZ * 16;
        for (DistanceWeightedValue<T> entry : options) {
            totalWeight += entry.getWeight(x, z);
        }
        if (totalWeight == 0) {
            return null;
        }

        double weight = new SplittableRandom(chunkSeed).nextDouble(totalWeight);
        for (DistanceWeightedValue<T> entry : options) {
            double entryWeight = entry.getWeight(x, z);
            if (entryWeight <= 0) {
                continue;
            }
            weight -= entryWeight;
            if (weight <= 0) {
                return entry.getValue();
            }
        }
        // Should never happen
        return null;
    }

    public static float applyRotation(float targetRot, float rot, float rotationSpeed) {
        while (targetRot - rot < -180.0F) {
            rot -= 360.0F;
        }

        while (targetRot - rot >= 180.0F) {
            rot += 360.0F;
        }

        final float rotDelta = targetRot - rot;
        return rot + Math.signum(rotDelta) * Math.min(rotationSpeed, Math.abs(rotDelta));
    }
}
