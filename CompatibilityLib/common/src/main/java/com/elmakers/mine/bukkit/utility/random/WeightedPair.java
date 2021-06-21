package com.elmakers.mine.bukkit.utility.random;

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

    public WeightedPair(Float threshold, Float rawThreshold, String value, ValueParser<T> parser) {
        this.threshold = threshold;
        this.rawThreshold = rawThreshold;
        this.value = (parser == null) ? null : parser.parse(value);
    }

    public WeightedPair(T value) {
        this(1.0f, 1.0f, value);
    }

    public WeightedPair(Float threshold, T value) {
        this(threshold, threshold, value);
    }

    public WeightedPair(Float threshold, Float rawThreshold, T value) {
        this.threshold = threshold;
        this.value = value;
        this.rawThreshold = rawThreshold;
    }

    public WeightedPair(WeightedPair<?> weights, T value) {
        this.threshold = weights.threshold;
        this.value = value;
        this.rawThreshold = weights.rawThreshold;
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

    @Override
    public String toString() {
        return value.toString();
    }
}
