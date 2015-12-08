package com.elmakers.mine.bukkit.math.equations;

public class SinEquation {
	private Double a;
	private Double b;
	private Double c;
	
	public SinEquation(Double sinA, Double sinB, Double sinC) {
		a = sinA;
		b = sinB;
		c = sinC;
	}
	//This returns the value, or velocity, of a sin equation at a specific step.
	
	//For a sin function:
	//f(x) = a*sin(b(x+c)) + d
	//f'(x) = a*b*cos(b(x+c))
	public Double doDerivativeMath(Double step) {
		Double value;
		value = a*b*Math.cos(b*(step+c));
		return value;
	}
}
