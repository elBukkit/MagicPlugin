package com.elmakers.mine.bukkit.math.equations;

public class QuadraticEquation {
	private Double a;
	private Double b;
	private Double c;
	
	public QuadraticEquation(Double quadraticA, Double quadraticB, Double quadraticC) {
		a = quadraticA;
		b = quadraticB;
		c = quadraticC;
	}
	//This returns the value, or velocity, of a quadratic equation at a specific step.
	
	//For a quadratic function:
	//f(x) = a(x+b)^2 + c(x+b) + d
	//f'(x) = 2a(x+b) + c
	public Double doDerivativeMath(Double step) {
		Double value;
		value = 2*a*(step+b) + c;
		return value;
	}

}
