package com.elmakers.mine.bukkit.math.equations;

import org.bukkit.util.Vector;

public class MathHandler {
	private double a;
	private double b;
	private double c;
	
	public MathHandler(double mathA, double mathB, double mathC) {
		a = mathA;
		b = mathB;
		c = mathC;
	}
	
	public double returnDerivative(Equation equation, int step) {
		double derivativeValue;
		
		switch(equation) {
			case LINEAR:
				derivativeValue = new LinearEquation(a).doDerivativeMath();
				break;
			case QUADRATIC:
				derivativeValue = new QuadraticEquation(a, b, c).doDerivativeMath(step);
				break;
			case SIN:
				derivativeValue = new SinEquation(a, b, c).doDerivativeMath(step);
				break;
			default:
				derivativeValue = 0;
				break;
			}
		return derivativeValue;
	}
}
