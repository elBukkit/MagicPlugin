package com.elmakers.mine.bukkit.utility.random;

public abstract class ValueParser<T> {
    public static final ValueParser<Integer> INTEGER = new ValueParser<Integer>() {
        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }
    };

    public static final ValueParser<Float> FLOAT = new ValueParser<Float>() {
        @Override
        public Float parse(String value) {
            return Float.parseFloat(value);
        }
    };

    public static final ValueParser<Double> DOUBLE = new ValueParser<Double>() {
        @Override
        public Double parse(String value) {
            return Double.parseDouble(value);
        }
    };

    public static final ValueParser<String> STRING = new ValueParser<String>() {
        @Override
        public String parse(String value) {
            return value;
        }
    };

    public abstract T parse(String value);
}
