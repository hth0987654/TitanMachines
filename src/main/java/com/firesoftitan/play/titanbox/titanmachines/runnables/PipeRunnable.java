package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeRunnableInfo;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PipeRunnable extends BukkitRunnable {
    private List<UUID> que_Process = null;
    private PipeSubRunnable[] pipeSubRunnable = null;
    private final HashMap<UUID, PipeRunnableInfo> info = new HashMap<UUID, PipeRunnableInfo>();
    private final HashMap<UUID, PipeSubRunnable> fastQ = new HashMap<UUID, PipeSubRunnable>();
    private int index;
    public PipeRunnable() {
        que_Process = new ArrayList<UUID>();
        int min = Math.min(PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups().size(), 50);
        if (min < 1) min = 1;
        pipeSubRunnable = new PipeSubRunnable[min];
        index = pipeSubRunnable.length;
        que_Process.addAll(PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups());
    }

    @Override
    public void run() {

        if (!TitanMachines.pipedEnabled || TitanMachines.pipedEnabled) return;
        index++;
        List<UUID> groups = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups();
        int min = Math.min(pipeSubRunnable.length, groups.size());
        if (min == 0) return;
        if (index >= min) index = 0;
        if (pipeSubRunnable[index] == null || pipeSubRunnable[index].isDone()) {
            if (pipeSubRunnable[index] != null && pipeSubRunnable[index].isDone())
            {
                if (!info.containsKey(pipeSubRunnable[index].getUuid())) {
                    info.put(pipeSubRunnable[index].getUuid(), new PipeRunnableInfo(pipeSubRunnable[index].getUuid()));
                }
                PipeRunnableInfo pipeRunnableInfo = info.get(pipeSubRunnable[index].getUuid());
                pipeRunnableInfo.setRuntime(pipeSubRunnable[index].getRunTime());
            }
            long startTIme = System.currentTimeMillis();
            if (que_Process.isEmpty())
            {
                System.out.println("Starting....");
                que_Process.addAll(groups);
            }
            if (que_Process.isEmpty()) return;
            UUID uuid = que_Process.get(0);
            que_Process.remove(0);
            pipeSubRunnable[index] = new PipeSubRunnable(uuid, PipeTypeEnum.COPPER);
            pipeSubRunnable[index].runTaskTimer(TitanMachines.instants, 1, 1);
  //          int size = info.size() - fastQ.size();
//            System.out.println(fastQ.size() + ", " + size + ": " + info.size());
            long doneTime = System.currentTimeMillis() - startTIme;
            if (doneTime > 10) TitanMachines.messageTool.sendMessageSystem("Pipe main task took to long uuid:" + uuid + ":" + doneTime + " ms");
        }
    }
}
