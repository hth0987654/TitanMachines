package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PipeRunnable extends BukkitRunnable {
    private List<UUID> que_Process = null;
    private PipeSubRunnable[] pipeSubRunnable = null;
    private int index;
    private long pausedTime;
    public PipeRunnable() {
        que_Process = new ArrayList<UUID>();
        int min = Math.min(PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups().size(), 20);
        if (min < 1) min = 1;
        pipeSubRunnable = new PipeSubRunnable[min];
        index = pipeSubRunnable.length;
        que_Process.addAll(PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups());
        pausedTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        //if (System.currentTimeMillis() - pausedTime < 500) return;

        if (!TitanMachines.pipedEnabled) return;
        index++;
        List<UUID> groups = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups();
        int min = Math.min(pipeSubRunnable.length, groups.size());
        if (min == 0) return;
        if (index >= min) index = 0;


        if (pipeSubRunnable[index] == null || pipeSubRunnable[index].isDone()) {
            long startTIme = System.currentTimeMillis();
            if (que_Process.isEmpty())
            {
                pausedTime = System.currentTimeMillis();
                que_Process.addAll(groups);
            }
            if (que_Process.isEmpty()) return;
            UUID uuid = que_Process.get(0);
            que_Process.remove(0);
            pipeSubRunnable[index] = new PipeSubRunnable(uuid, PipeTypeEnum.COPPER);
            pipeSubRunnable[index].runTaskTimer(TitanMachines.instants, 1, 1);
            long doneTime = System.currentTimeMillis() - startTIme;
            if (doneTime > 200) TitanMachines.messageTool.sendMessageSystem("Pipe main task took to long uuid:" + uuid + ":" + doneTime + " ms");
        }
    }
}
