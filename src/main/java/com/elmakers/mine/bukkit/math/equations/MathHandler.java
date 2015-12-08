package com.elmakers.mine.bukkit.math.equations;

public class MathHandler {
	private double a;
	private double b;
	private double c;
	
	public MathHandler(double mathA, double mathB, double mathC) {
		a = mathA;
		b = mathB;
		c = mathC;
	}
	
	public double returnDerivative(Equation equation, double time) {
		Double derivativeValue;
		time = time / 1000;
		
		switch(equation) {
			case LINEAR:
				derivativeValue = new LinearEquation(a).doDerivativeMath();
				break;
			case QUADRATIC:
				derivativeValue = new QuadraticEquation(a, b, c).doDerivativeMath(time);
				break;
			case SIN:
				derivativeValue = new SinEquation(a, b, c).doDerivativeMath(time);
				break;
			default:
				derivativeValue = 0.0;
				break;
			}
		return derivativeValue;
	}
}
