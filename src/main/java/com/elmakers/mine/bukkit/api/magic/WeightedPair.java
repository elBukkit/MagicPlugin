package com.elmakers.mine.bukkit.api.magic;


public class WeightedPair<T extends Object> {
	private Float threshold;
	private T value;

	@SuppressWarnings("unchecked")
	public WeightedPair(Float threshold, String value, Class<T> parseAs) {
		this.threshold = threshold;
		// This is pretty ugly, but not as ugly as trying to
		// infer the generic type argument.
		if (parseAs == Integer.class) {
			this.value = (T)(Integer)Integer.parseInt(value);
		} else if (parseAs == Float.class) {
			this.value = (T)(Float)Float.parseFloat(value);
		} else if (parseAs == Double.class) {
			this.value = (T)(Double)Double.parseDouble(value);
		}else if (parseAs == String.class) {
			this.value = (T)value;
		}
	}
	
	public WeightedPair(Float threshold, T value) {
		this.threshold = threshold;
		this.value = value;
	}
	
	public Float getThreshold() {
		return threshold;
	}
	
	public T getValue() {
		return value;
	}
}