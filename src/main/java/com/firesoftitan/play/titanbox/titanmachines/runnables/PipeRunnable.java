package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeLookUpInfo;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class PipeRunnable extends BukkitRunnable {
    private final List<UUID> qGroups = new ArrayList<UUID>();
    public static PipeRunnable instance;
    private final int ticks;
    private long lastRan, lastSwitch;
    private final HashMap<UUID, Long> times = new HashMap<UUID, Long>();
    private final HashMap<UUID, Long> lastTimes = new HashMap<UUID, Long>();
    private final HashMap<UUID, Long> runningTimes = new HashMap<UUID, Long>();
    private int size = 0;
    private final int tpsSampleSize = 120;
    public PipeRunnable(int ticks) {
        instance = this;
        this.ticks = ticks;
        this.runTaskTimer(TitanMachines.instants, this.ticks, this.ticks);
/*        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);
        List<UUID> groups = copperPipes.getGroups();
        for(int i = 0; i < groups.size(); i++)
        {
            new SecondaryPipeRunnable(ticks + i, PipeTypeEnum.COPPER, groups.get(i));
        }*/

    }
    public void setRunningTimeNow(UUID uuid, long time)
    {
        runningTimes.put(uuid, time);
    }
    public long getRunningTime(UUID uuid)
    {
        long l = 0;
        if (runningTimes.containsKey(uuid)) l = runningTimes.get(uuid);
        return l;
    }
    public void setTimeNow(UUID uuid)
    {
        times.put(uuid, System.currentTimeMillis());
    }
    public long getTime(UUID uuid)
    {
        long l = 0;
        if (times.containsKey(uuid)) l = times.get(uuid);
        return l;
    }
    public int getNumberGroups()
    {
        return size;
    }
    public int getMaxThreads()
    {
        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);
        return copperPipes.getGroups().size();
    }
    public void setLastTimeRan(UUID uuid)
    {
        long time = getTime(uuid);
        if (time == 0) return;
        long value = (System.currentTimeMillis() - time) - getRunningTime(uuid);
        lastTimes.put(uuid, value);
    }
    public long getLastTimeRan(UUID uuid)
    {
        long l = 0;
        if (lastTimes.containsKey(uuid)) l = lastTimes.get(uuid);
        return l;
    }


    public int getThreadsRunning() {
        return SecondaryPipeRunnable.getSize();
    }
    public double getTPS()
    {
        IntSummaryStatistics stats = tickAverage.stream().collect(Collectors.summarizingInt(Double::intValue));
        return stats.getAverage();
    }
    public boolean isTPSReady()
    {

        return tickAverage.size() >= tpsSampleSize;
    }
    public int isLowTPSMode()
    {
        double tps = PipeRunnable.instance.getTPS();
        if (tps < 12D) return 3;
        if (tps < 16D) return 2;
        if (tps < 19D || SecondaryPipeRunnable.getSlowCount() > 0) return 1;
        return 0;
    }

    private final List<Double> tickAverage = new ArrayList<Double>();
    @Override
    public void run() {
        long l = System.currentTimeMillis() - lastRan;
        double ticks =  ((50D *this.ticks)/(double)l) * 20;// 50L*ticks = 600 for 20TPS
        tickAverage.add(ticks);
        if (tickAverage.size() > tpsSampleSize) tickAverage.remove(0);
        lastRan = System.currentTimeMillis();
        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);
        if (isLowTPSMode() == 2 || !TitanMachines.pipedEnabled)
        {
            SecondaryPipeRunnable.stopALL();
            qGroups.clear();
            return;
        }



        if (qGroups.isEmpty())
        {
            List<UUID> groups = copperPipes.getGroups();
            size = groups.size();
            qGroups.addAll(groups);
        }
        if (qGroups.isEmpty()) return;
        for (int i = 0; i < qGroups.size(); i++)
        {
            if (SecondaryPipeRunnable.getSecondaryPipeRunnable(qGroups.get(i)) == null)
            {
                new SecondaryPipeRunnable(this.ticks + i, PipeTypeEnum.COPPER, qGroups.get(i));
            }
        }
        qGroups.clear();

    }
    private void CheckAllFilterTypes(UUID group, Location outLocation, int i, PipeLookUpInfo lookUp, ItemStack item) {
        PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
        checkAndMove(group, outLocation, i, lookUp, match, item);
        match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
        checkAndMove(group, outLocation, i, lookUp, match, item);
        match = PipeChestFilterTypeEnum.ALL;
        checkAndMove(group, outLocation, i, lookUp, match, item);
    }

    private ItemStack checkSort(UUID uuid, Location chest, ItemStack itemStack, int slot)
    {
        Location pipe = JunctionBoxBlock.getPipeLocation(uuid, chest);
        if (ContainerManager.isContainer(chest)) {
            ItemStack item = ContainerManager.getInventorySlot(pipe, chest, slot);
            if (TitanMachines.itemStackTool.isEmpty(item)) {
                ContainerManager.setInventorySlot(pipe, chest, itemStack, slot);
                return null;
            } else if (TitanMachines.itemStackTool.isItemEqual(itemStack, item)) {
                //noinspection DataFlowIssue
                int amount = item.getAmount();
                int max = item.getMaxStackSize();
                int needed = max - amount;
                int have = itemStack.getAmount();
                if (have == 0) return itemStack.clone();
                if (have > needed) {
                    itemStack.setAmount(have - needed);
                    item.setAmount(max);
                    ContainerManager.setInventorySlot(pipe, chest, item, slot);
                    return itemStack.clone();
                }
                item.setAmount(amount + have);
                ContainerManager.setInventorySlot(pipe, chest, item, slot);
                return null;
            }
        }
        return itemStack.clone();
    }
    private void checkAndMove(UUID group, Location location, int i, PipeLookUpInfo lookUp, PipeChestFilterTypeEnum match, ItemStack item) {
        List<Location> itemLocations = lookUp.getItemLocation(match, item);
        for (Location itemLocation : itemLocations) {
            List<Integer> itemSlots = lookUp.getItemSlot(match, item, itemLocation);
            for (Integer slot : itemSlots) {
                item = checkSort(group, itemLocation, item, slot);//place in the chest
                ContainerManager.setInventorySlot(group, location, item, i);//send back leftovers
                if (item == null) return;
            }
        }
    }
}
