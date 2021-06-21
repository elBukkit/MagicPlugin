package com.elmakers.mine.bukkit.utility.random;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.ConfigUtils;

/**
 * Contains some general Randomization utilities, including
 * computing WeightedPair - based probability decisions.
 */
public class RandomUtils {
    private static final Random random = new Random();

    public static float lerp(String[] list, int levelIndex, int nextLevelIndex, float distance) {
        if (list.length == 0) return 0;
        if (levelIndex >= list.length) return Float.parseFloat(list[list.length - 1]);
        if (nextLevelIndex >= list.length) return Float.parseFloat(list[list.length - 1]);
        float previousValue = Float.parseFloat(list[levelIndex]);
        float nextValue = Float.parseFloat(list[nextLevelIndex]);
        return lerp(previousValue, nextValue, distance);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T lerp(T previousValue, T nextValue, float distance) {
        return (T)(Number)((Float)previousValue + distance * ((Float)nextValue - (Float)previousValue));
    }

    /**
     * This method is for backwards-compatibility of plugins that relied on the LinkedList variant, notably MagicWorlds.
     */
    @Nullable
    @Deprecated
    public static <T> T weightedRandom(LinkedList<WeightedPair<T>> weightList) {
        return weightedRandom((Deque<WeightedPair<T>>) weightList);
    }

    @Nullable
    public static <T extends Object> T weightedRandom(Deque<WeightedPair<T>> weightList) {
        if (weightList == null || weightList.size() == 0) return null;

        Float maxWeight = weightList.getLast().getThreshold();
        Float selectedWeight = random.nextFloat() * maxWeight;
        for (WeightedPair<T> weight : weightList) {
            if (selectedWeight < weight.getThreshold()) {
                return weight.getValue();
            }
        }

        return weightList.getFirst().getValue();
    }

    @Nonnull
    public static <T extends Object> Collection<T> getValues(Deque<WeightedPair<T>> weightList) {
        List<T> list = new ArrayList<>();
        for (WeightedPair<T> pair : weightList) {
            list.add(pair.getValue());
        }
        return list;
    }

    public static void populateIntegerProbabilityMap(Deque<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.INTEGER, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateIntegerProbabilityMap(Deque<WeightedPair<Integer>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.INTEGER, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static void populateIntegerProbabilityMap(Deque<WeightedPair<Integer>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.INTEGER, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else if (parent.isInt(key)) {
            populateProbabilityConstant(ValueParser.INTEGER, probabilityMap, parent.getString(key));
        } else {
            populateProbabilityList(ValueParser.INTEGER, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateStringProbabilityMap(Deque<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.STRING, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateStringProbabilityMap(Deque<WeightedPair<String>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.STRING, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else {
            populateProbabilityList(ValueParser.STRING, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateStringProbabilityMap(Deque<WeightedPair<String>> probabilityMap, ConfigurationSection parent, String key) {
        populateStringProbabilityMap(probabilityMap, parent, key, 0, 0,0);
    }

    public static void populateStringProbabilityMap(Deque<WeightedPair<String>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.STRING, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static void populateStringProbabilityList(Deque<WeightedPair<String>> probabilityMap, List<String> stringList) {
        populateProbabilityList(ValueParser.STRING, probabilityMap, stringList);
    }

    public static void populateFloatProbabilityMap(Deque<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
        RandomUtils.populateProbabilityMap(ValueParser.FLOAT, probabilityMap, nodeMap, levelIndex, nextLevelIndex, distance);
    }

    public static void populateFloatProbabilityMap(Deque<WeightedPair<Float>> probabilityMap, ConfigurationSection parent, String key, int levelIndex, int nextLevelIndex, float distance) {
        if (parent.isConfigurationSection(key)) {
            populateProbabilityMap(ValueParser.FLOAT, probabilityMap, parent.getConfigurationSection(key), levelIndex, nextLevelIndex, distance);
        } else if (parent.isDouble(key) || parent.isInt(key)) {
            populateProbabilityConstant(ValueParser.FLOAT, probabilityMap, parent.getString(key));
        } else {
            populateProbabilityList(ValueParser.FLOAT, probabilityMap, ConfigUtils.getStringList(parent, key));
        }
    }

    public static void populateFloatProbabilityMap(Deque<WeightedPair<Float>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(ValueParser.FLOAT, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static <T extends Object> void populateProbabilityConstant(ValueParser<T> parser, Deque<WeightedPair<T>> probabilityMap, String value) {
        probabilityMap.add(new WeightedPair<>(1.0f, 1.0f, value, parser));
    }

    public static <T extends Object> void populateProbabilityList(ValueParser<T> parser, Deque<WeightedPair<T>> probabilityMap, List<String> keys) {
        if (keys != null) {
            float currentThreshold = 0;
            for (String key : keys) {
                currentThreshold++;
                probabilityMap.add(new WeightedPair<>(currentThreshold, currentThreshold, key, parser));
            }
        }
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, Deque<WeightedPair<T>> probabilityMap, ConfigurationSection nodeMap) {
        RandomUtils.populateProbabilityMap(parser, probabilityMap, nodeMap, 0, 0, 0);
    }

    public static <T extends Object> void populateProbabilityMap(ValueParser<T> parser, Deque<WeightedPair<T>> probabilityMap, ConfigurationSection nodeMap, int levelIndex, int nextLevelIndex, float distance) {
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

    public static <T extends Object> Deque<WeightedPair<T>> merge(Deque<WeightedPair<T>> base, Deque<WeightedPair<T>> inherit) {
        if (inherit == null || inherit.size() == 0) {
            return base;
        }
        if (base == null) {
            base = new ArrayDeque<>();
        }
        if (base.size() == 0) {
            base.addAll(inherit);
        } else {
            WeightedPair<T> lastPair = base.getLast();
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
        RandomUtils.extrapolateList(list);
    }

    public static void extrapolateIntegerList(List<AscendingPair<Integer>> list)
    {
        RandomUtils.extrapolateList(list);
    }

    public static <T extends Number> void extrapolateList(List<AscendingPair<T>> list)
    {
        Collections.sort(list);
        int index = 0;
        while (index < list.size() - 1) {
            AscendingPair<T> current = list.get(index);
            AscendingPair<T> next = list.get(index + 1);
            long currentIndex = current.getIndex();
            long nextIndex = next.getIndex();

            index++;
            if (nextIndex > currentIndex + 1) {
                float distance = 1f / (nextIndex - currentIndex);
                AscendingPair<T> inserted = new AscendingPair<>(currentIndex + 1, lerp(current.getValue(), next.getValue(), distance));
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
        return list.get(random.nextInt(list.size()));
    }

    public static Random getRandom() {
        return random;
    }
}
