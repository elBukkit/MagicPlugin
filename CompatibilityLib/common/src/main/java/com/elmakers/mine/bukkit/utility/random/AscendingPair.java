package com.elmakers.mine.bukkit.utility.random;

/**
 * Used by RandomUtils to extrapolate values from an ascending list that may contain gaps.
 *
 * <p>This class may be used with any Numeric base type. The ascension index is always a Long.
 *
 * <p>RandomUtils can take a list of these and use linear interpolation to return a complete set
 * with a single value for each integral between the given min and max values.
 *
 * @param <T> The type of Numeric to randomly select
 */
public class AscendingPair<T extends Number> implements Comparable<AscendingPair<?>> {
    private final long index;
    private final T value;

    public AscendingPair(long index, T value) {
        this.index = index;
        this.value = value;
    }

    public long getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }

    @Override
    public int compareTo(AscendingPair<? extends Object> other) {
        return Long.compare(this.index, other.index);
    }
}
