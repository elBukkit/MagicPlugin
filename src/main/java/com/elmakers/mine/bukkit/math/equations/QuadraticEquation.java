package com.elmakers.mine.bukkit.math.equations;

public class QuadraticEquation {
	private double a;
	private double b;
	private double c;
	
	public QuadraticEquation(double quadraticA, double quadraticB, double quadraticC) {
		a = quadraticA;
		b = quadraticB;
		c = quadraticC;
	}
	//This returns the value, or velocity, of a quadratic equation at a specific step.
	
	//For a quadratic function:
	//f(x) = a(x+b)^2 + c(x+b) + d
	//f'(x) = 2a(x+b) + c
	public double doDerivativeMath(double step) {
		double value;
		value = 2*a*(step+b) + c;
		return value;
	}

}
