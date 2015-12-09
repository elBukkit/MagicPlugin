package com.elmakers.mine.bukkit.math;

import com.elmakers.mine.bukkit.api.math.Transform;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public class MultiplyTransform implements Transform {
	private Collection<Transform> inputs;

	@Override
	public void load(ConfigurationSection parameters) {
		inputs = Transforms.loadTransformList(parameters, "inputs");
	}

	@Override
	public double get(double t) {
		double value = 1;
		for (Transform transform : inputs) {
			value *= transform.get(t);
		}
		return value;
	}
}
