package com.elmakers.mine.bukkit.utility;

public abstract class TimedRunnable implements Runnable {
    private int count = 0;
    private long totalTime = 0;
    private final String name;

    public TimedRunnable(String name) {
        this.name = name;
    }

    public abstract void onRun();

    @Override
    public final void run() {
        long start = System.nanoTime();
        onRun();
        count++;
        totalTime += System.nanoTime() - start;
    }

    public int getCount() {
        return count;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public String getName() {
        return name;
    }
}
