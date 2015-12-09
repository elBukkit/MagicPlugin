package com.elmakers.mine.bukkit.math;

import com.elmakers.mine.bukkit.api.math.Transform;
import org.bukkit.configuration.ConfigurationSection;

public class dSinTransform implements Transform {
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
	 * This returns the derivative, or velocity, of a sin equation at a specific step.
	 * For a sin function:
	 * f(x) = a*sin(b(x+c)) + d
	 * f'(x) = a*b*cos(b(x+c))
	 * @param t
	 * @return
     */
	@Override
	public double get(double t) {
		double bValue = b.get(t);
		return a.get(t) * bValue * Math.cos(bValue * (t + c.get(t)));
	}
}
