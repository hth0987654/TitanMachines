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
/*        if (counter > 29)
        {
            System.out.println(getMinimumTick() + "/" + getMaximumTick() + "/" + getAverageMinute());
            counter = 0;
        }*/
        last60.add(currentTick);
        if (last60.size() > 59) last60.remove(0);
        lastTime = System.currentTimeMillis();
    }

    public float getAverageMinute() {
        return getAverage(this.last60);
    }

    public float getCurrentTick() {
        return currentTick;
    }
    public float getTickOff() {
        return 20 - currentTick;
    }
    public float getMaximumTick()
    {
        float max = Float.MIN_VALUE;
        for (float f : last60) {
            if (f > max) {
                max = f;
            }
        }
        return max;
    }
    public float getMinimumTick()
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
