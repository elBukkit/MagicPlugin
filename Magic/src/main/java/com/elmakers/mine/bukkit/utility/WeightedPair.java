package com.elmakers.mine.bukkit.utility;

/**
 * Used by RandomUtils to create a randomly-selectable set of objects, based on each
 * category having a weight.
 *
 * <p>This class may be used with any base type, so that a String may be randomly selected,
 * or a numeric value.
 *
 * <p>RandomUtils also supports return linearly interpolated (lerp'd) values for numeric-based
 * weighted pairs, for a smoothly sliding scale of weighted values.
 *
 * @param <T> The type of Object to randomly select
 */
public class WeightedPair<T extends Object> implements Comparable<WeightedPair<? extends Object>> {
    private final Float threshold;
    private final Float rawThreshold;
    private final T value;

    @SuppressWarnings("unchecked")
    public WeightedPair(Float threshold, Float rawThreshold, String value, Class<T> parseAs) {
        this.threshold = threshold;
        this.rawThreshold = rawThreshold;
        // This is pretty ugly, but not as ugly as trying to
        // infer the generic type argument.
        if (parseAs == Integer.class) {
            this.value = (T)(Integer)Integer.parseInt(value);
        } else if (parseAs == Float.class) {
            this.value = (T)(Float)Float.parseFloat(value);
        } else if (parseAs == Double.class) {
            this.value = (T)(Double)Double.parseDouble(value);
        } else if (parseAs == String.class) {
            this.value = (T)value;
        } else {
            this.value = null;
        }
    }

    public WeightedPair(Float threshold, T value) {
        this(threshold, threshold, value);
    }

    public WeightedPair(Float threshold, Float rawThreshold, T value) {
        this.threshold = threshold;
        this.value = value;
        this.rawThreshold = rawThreshold;
    }

    public Float getThreshold() {
        return threshold;
    }

    public Float getRawThreshold() {
        return rawThreshold;
    }

    public T getValue() {
        return value;
    }

    @Override
    public int compareTo(WeightedPair<? extends Object> other) {
        return this.threshold.compareTo(other.threshold);
    }
}
