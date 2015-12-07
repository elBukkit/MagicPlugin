package com.elmakers.mine.bukkit.math.equations;

public class SinEquation {
	private double a;
	private double b;
	private double c;
	
	public SinEquation(double sinA, double sinB, double sinC) {
		a = sinA;
		b = sinB;
		c = sinC;
	}
	//This returns the value, or velocity, of a sin equation at a specific step.
	
	//For a sin function:
	//f(x) = a*sin(b(x+c)) + d
	//f'(x) = a*b*cos(b(x+c))
	public double doDerivativeMath(double step) {
		double value;
		value = a*b*Math.cos(b*(step+c));
		return value;
	}
}
