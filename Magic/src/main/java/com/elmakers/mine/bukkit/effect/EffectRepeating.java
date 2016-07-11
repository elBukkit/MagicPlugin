package com.elmakers.mine.bukkit.effect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;


public abstract class EffectRepeating extends EffectPlayer implements Runnable {
    protected int iterations = 4;
    protected int period = 1;
    protected boolean reverse = false;

    protected Integer taskId;

    public EffectRepeating() {
        super();
    }

    public EffectRepeating(Plugin plugin) {
        super(plugin);
    }

    // State
    protected int iteration = 0;

    @Override
    public void load(Plugin plugin, ConfigurationSection configuration) {
        super.load(plugin, configuration);

        iterations = configuration.getInt("iterations", iterations);
        period = configuration.getInt("period", period);
        if (period < 1) {
            period = 1;
        }
        reverse = configuration.getBoolean("reverse", reverse);
        if (configuration.contains("duration")) {
            iterations = (int)Math.ceil(configuration.getDouble("duration") / period / 50);
        }
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public void play() {
        stop();
        iteration = 0;
        run();
    }

    public void stop() {
        iteration = iterations;
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = null;
        }
    }

    @Override
    public void run() {
        taskId = null;
        iterate();

        if (++iteration < iterations) {
            schedule();
        }
    }

    protected void schedule() {
        if (plugin == null) return;
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, period);
    }

    public float scale(float maxValue) {
        return (float)scale((double)maxValue);
    }

    public double scale(double maxValue) {
        int maxIteration = iterations - 1;
        if (maxIteration <= 0) return maxValue;
        double i = reverse ? (maxIteration - iteration) : iteration;
        // Favor more iterations closer to the origin.
        return (maxValue * (i / maxIteration) * (i / maxIteration));
    }

    public abstract void iterate();
}
