package com.firesoftitan.play.titanbox.titanmachines.runnables;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TPSMonitorRunnable extends BukkitRunnable {
    public static TPSMonitorRunnable instance;
    private long lastTime;
    private float currentTick;
    private final List<Float> last60 = new ArrayList<Float>();
    private int counter = 0;

    public TPSMonitorRunnable() {
        this.lastTime = System.currentTimeMillis();
        instance = this;
    }

    @Override
    public void run() {
        counter++;
        long passed = System.currentTimeMillis() - this.lastTime;
        if (passed <= 0) passed = 50;
        currentTick = (1000f / (float)passed) * 20;
        last60.add(currentTick);
        if (last60.size() > 59) last60.remove(0);
        lastTime = System.currentTimeMillis();
    }

    public float getAverageTPS() {
        return getAverage(this.last60);
    }

    public float getCurrentTPS() {
        return currentTick;
    }
    public float getTickOff() {
        return 20 - currentTick;
    }
    public float getMaximumTPS()
    {
        float max = Float.MIN_VALUE;
        for (float f : last60) {
            if (f > max) {
                max = f;
            }
        }
        return max;
    }
    public float getMinimumTPS()
    {
        float min = Float.MAX_VALUE;
        for (float f : last60) {
            if (f < min) {
                min = f;
            }
        }
        if (min < 0) min = 0;
        return min;
    }
    private float getAverage(List<Float> list) {
        float sum = 0;
        for(float f : list) {
            sum += f;
        }

        return sum / list.size();
    }
}
