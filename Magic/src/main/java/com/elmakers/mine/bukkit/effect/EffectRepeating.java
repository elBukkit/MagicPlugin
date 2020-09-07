package com.elmakers.mine.bukkit.effect;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public abstract class EffectRepeating extends EffectPlayer implements Runnable {
    private static final Random random = new Random();
    protected int iterations = 4;
    protected int period = 1;
    protected double probability = 1;
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

        String durationString = configuration.getString("duration", "");
        if (configuration.contains("delay")) {
            period = configuration.getInt("delay") * 20 / 1000;
        } else {
            period = configuration.getInt("period", period);
        }
        if (period < 1) {
            period = 1;
        }
        if (configuration.contains("duration")) {
            if (durationString.equals("infinite") || durationString.equals("forever") || durationString.equals("infinity")) {
                iterations = Integer.MAX_VALUE;
            } else {
                iterations = (int)Math.ceil(configuration.getDouble("duration") / period / 50);
            }
        } else {
             iterations = configuration.getInt("iterations", iterations);
        }
        probability = configuration.getDouble("probability", 1);
        reverse = configuration.getBoolean("reverse", reverse);
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

    @Override
    public void cancel() {
        stop();
        super.cancel();
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
        if (probability >= 1 || random.nextDouble() <= probability) {
            iterate();
        }

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
