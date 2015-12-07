package com.elmakers.mine.bukkit.math.equations;

public class LinearEquation {
	private double a;
	
	public LinearEquation(double linearA) {
		a = linearA;
	}
	//This returns the value, or velocity, of a linear equation at a specific step.
	
	//For a linear function:
	//f(x) = a(x+b) + c
	//f'(x) = a
	public double doDerivativeMath() {
		double value;
		value = a;
		return value;
	}
}
