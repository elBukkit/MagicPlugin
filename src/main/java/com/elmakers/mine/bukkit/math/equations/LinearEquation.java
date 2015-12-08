package com.elmakers.mine.bukkit.math.equations;

public class LinearEquation {
	private Double a;
	
	public LinearEquation(Double linearA) {
		a = linearA;
	}
	//This returns the value, or velocity, of a linear equation at a specific step.
	
	//For a linear function:
	//f(x) = a(x+b) + c
	//f'(x) = a
	public Double doDerivativeMath() {
		Double value;
		value = a;
		return value;
	}
}
