package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeLookUpInfo;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class PipeRunnable extends BukkitRunnable {
    private final List<SecondaryPipeRunnable> tookLongs;
    private final List<UUID> qGroups = new ArrayList<UUID>();
    public static PipeRunnable instance;
    private final int ticks;
    private long lastRan, lastSwitch;
    private  int max = 1;
    private final HashMap<UUID, Long> times = new HashMap<UUID, Long>();
    private final HashMap<UUID, Long> lastTimes = new HashMap<UUID, Long>();
    private int size = 0;
    private final int tpsSampleSize = 120;
    public PipeRunnable(int ticks) {
        instance = this;
        this.ticks = ticks;
        this.runTaskTimer(TitanMachines.instants, this.ticks, this.ticks);
        lastRan = System.currentTimeMillis();
        tookLongs = new ArrayList<SecondaryPipeRunnable>();
        lastSwitch = System.currentTimeMillis();
        int max = 30;
        for(int i = 0; i < max; i++)
        {
           tookLongs.add(new SecondaryPipeRunnable(ticks*2 + (ticks*2) / max));
        }

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
        return tookLongs.size();
    }
    public void setLastTimeRan(UUID uuid)
    {
        long time = getTime(uuid);
        if (time == 0) return;
        long value = System.currentTimeMillis() - time;
        lastTimes.put(uuid, value);
    }
    public long getLastTimeRan(UUID uuid)
    {
        long l = 0;
        if (lastTimes.containsKey(uuid)) l = lastTimes.get(uuid);
        return l;
    }


    public int getMax() {
        return max;
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
    private final List<Double> tickAverage = new ArrayList<Double>();
    @Override
    public void run() {
        long l = System.currentTimeMillis() - lastRan;
        double ticks =  ((50D *this.ticks)/(double)l) * 20;// 50L*ticks = 600 for 20TPS
        tickAverage.add(ticks);
        if (tickAverage.size() > tpsSampleSize) tickAverage.remove(0);
        double average = getTPS();
        if (!isTPSReady()) average = 18D;
        if (average <= 13D)
        {
            if (max > 0) {
                System.out.println("All Pipes STOPPED");
                max = 0;
                lastSwitch = System.currentTimeMillis();
            }
        }
        else if (average <= 15D)
        {
            if (max > 1) {
                System.out.println("Very low tps");
                max = 1;
                lastSwitch = System.currentTimeMillis();
            }
        }else if (average <= 17D && (System.currentTimeMillis() - lastSwitch > 30000))
        {
            if (max > 1) {
                System.out.println("low tps, backing off");
                max--;
                lastSwitch = System.currentTimeMillis();
            }
        } else if ((average > 19D) && (System.currentTimeMillis() - lastSwitch > 60000)){
            int total = Math.min(size, tookLongs.size());
            if (max < total)
            {
                System.out.println("high tps, stepping up");
                max++;
                lastSwitch = System.currentTimeMillis();
            }

        }

        lastRan = System.currentTimeMillis();
        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);

        if (qGroups.isEmpty())
        {
            List<UUID> groups = copperPipes.getGroups();
            size = groups.size();
            qGroups.addAll(groups);
        }
        UUID uuid = qGroups.get(0);
        for (int i = 0; i < max; i++)
        {
            SecondaryPipeRunnable pipeRunnable = tookLongs.get(i);
            if (!pipeRunnable.isBusy())
            {
                pipeRunnable.start(PipeTypeEnum.COPPER, uuid);
                qGroups.remove(0);
                return;
            }
        }
     /*   if (!TitanMachines.pipedEnabled) return;
        long startTImeL = System.currentTimeMillis();
        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);
        if (groupsQ.isEmpty())
        {
            List<UUID> groups = copperPipes.getGroups();
            groupsQ.addAll( groups);
            List<UUID> uuids = getAllTookLong();
            for (UUID uuid: uuids)
            {
                groupsQ.remove(uuid);
            }
        }
        int count = 0;
        long done;
        for (UUID group : groupsQ) {
            long selftimer = System.currentTimeMillis();
            done = System.currentTimeMillis() - startTImeL;
            long maxTick = this.tickTime / 3L;
            if (done > maxTick) break; //if pipes are taking to long stop and continue later
            count++;
            PipeLookUpInfo lookUp = copperPipes.getLookUp(group);
            if (lookUp != null) {
                List<Location> OutChestsInGroup = copperPipes.getOutChestsInGroup(group);
                for (Location outLocation : OutChestsInGroup) {
                    for(int i: ContainerManager.getInventorySlots(group, outLocation))
                    {
                        PipeChestFilterTypeEnum OutChestSettingsFilterType = copperPipes.getChestSettingsFilterType(outLocation, group, i);
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                CheckAllFilterTypes(group, outLocation, i, lookUp, item);

                            }
                        }
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, group, i);
                                if (chestSettingsFilter.getType() == item.getType()) {
                                    CheckAllFilterTypes(group, outLocation, i, lookUp, item);
                                }
                            }
                        }
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, group, i);
                                if (TitanMachines.itemStackTool.isItemEqual(chestSettingsFilter, item)) {
                                    CheckAllFilterTypes(group, outLocation, i, lookUp, item);
                                }
                            }
                        }
                    }
                }
            } else {
                if (!test) TitanMachines.messageTool.sendMessageSystem("Group not found: " + group);
            }
            selftimer = System.currentTimeMillis() - selftimer;
            if (selftimer > this.tickTime / 3L)
            {
                SecondaryPipeRunnable tookLong = getLowest();
                tookLong.addPipeGroup(group);
                TitanMachines.messageTool.sendMessageSystem("Group: " + group + " add to complex pipe array, Time: " + selftimer + " ms");
                break;
            }
        }
        groupsQ.subList(0, count).clear();
        test = true;
        done = System.currentTimeMillis() - startTImeL;
        if (done > this.tickTime && count > 1) TitanMachines.messageTool.sendMessageSystem("Took to long, " + count + " pipes, Time: " + done + " ms");*/

    }
    private List<UUID> getAllTookLong()
    {
        return tookLongs.stream()
                .map(SecondaryPipeRunnable::myPipesGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    private SecondaryPipeRunnable getLowest()
    {
        return tookLongs.stream()
                .min(Comparator.comparing(SecondaryPipeRunnable::getQSize))
                .orElse(null);
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
