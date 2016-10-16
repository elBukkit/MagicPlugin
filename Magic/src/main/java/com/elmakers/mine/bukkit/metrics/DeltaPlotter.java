package com.elmakers.mine.bukkit.metrics;

import org.mcstats.Metrics.Plotter;

public class DeltaPlotter extends Plotter {
	private Plotter plotter;
	private int previousValue;
	
	public DeltaPlotter(Plotter plotter) {
		super(plotter.getColumnName());
		this.plotter = plotter;
		previousValue = plotter.getValue();
	}

	@Override
	public int getValue() {
		int value = plotter.getValue();
		int delta = value - previousValue;
		previousValue = value;
		return delta;
	}	
}
