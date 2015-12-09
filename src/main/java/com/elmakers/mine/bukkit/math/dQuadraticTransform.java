package com.elmakers.mine.bukkit.math;

import com.elmakers.mine.bukkit.api.math.Transform;
import org.bukkit.configuration.ConfigurationSection;

public class dQuadraticTransform implements Transform {
	private Transform a;
	private Transform b;
	private Transform c;

	@Override
	public void load(ConfigurationSection parameters) {
		a = Transforms.loadTransform(parameters, "a");
		b = Transforms.loadTransform(parameters, "b");
		c = Transforms.loadTransform(parameters, "c");
	}

	/**
	 * This returns the derivative, or velocity, of a quadratic equation at a specific step.
	 * For a quadratic function:
	 * f(x) = a(x+b)^2 + c(x+b) + d
	 * f'(x) = 2a(x+b) + c
	 * @param t
	 * @return
     */
	@Override
	public double get(double t) {
		return  2 * a.get(t) * (t + b.get(t)) + c.get(t);
	}
}
