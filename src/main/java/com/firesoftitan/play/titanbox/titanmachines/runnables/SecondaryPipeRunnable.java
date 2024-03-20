package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeLookUpInfo;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeSendingInfo;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SecondaryPipeRunnable extends BukkitRunnable {
    private static final HashMap<UUID, SecondaryPipeRunnable> allRunnersPipeID = new HashMap<UUID, SecondaryPipeRunnable>();

    private final long tickTime;
    private final UUID runnerID;
    private final UUID pipeGroupID;
    private final PipeTypeEnum typeEnum;
    private static int slowCount = 0;
    private PipeSendingInfo  pipSaves;
    private boolean running = true;
    private boolean power = true;
    private long selftimerRunner;
    public static SecondaryPipeRunnable getSecondaryPipeRunnable(UUID group)
    {
        return allRunnersPipeID.get(group);
    }
    public static void stopALL()
    {
        for(UUID group: allRunnersPipeID.keySet())
        {
            SecondaryPipeRunnable pipeRunnable = allRunnersPipeID.get(group);
            if (pipeRunnable != null && !pipeRunnable.isCancelled()) pipeRunnable.cancel();
        }
        allRunnersPipeID.clear();
    }

    public boolean isRunning() {
        return running;
    }


    public boolean isPowered() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }


    public static int getSize()
    {
        return allRunnersPipeID.size();
    }
    public static int getSlowCount()
    {
        return slowCount;
    }
    public SecondaryPipeRunnable(int ticks, PipeTypeEnum pipeTypeEnum, UUID group) {
        this.runnerID = UUID.randomUUID();
        this.tickTime = ticks;
        this.runTaskTimer(TitanMachines.instants, this.tickTime, this.tickTime);
        typeEnum = pipeTypeEnum;
        pipeGroupID = group;
        allRunnersPipeID.put(group, this);
    }
    public void clearPipe()
    {
        pipSaves = null;
        this.running = false;
        if (this.power) {
            this.power = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    power = true;
                }
            }.runTaskLater(TitanMachines.instants, 20);
        }
    }
    @Override
    public void run() {
        if (!this.power || pipeGroupID == null || typeEnum == null || !TitanMachines.pipedEnabled) return;
        long KickOutTime = 40L;
        if (PipeRunnable.instance.isLowTPSMode() == 1) {
            KickOutTime = 20L;
        }
        if (PipeRunnable.instance.isLowTPSMode() == 2)
        {
            KickOutTime = 10L;
        }
        if (PipeRunnable.instance.isLowTPSMode() == 3)
        {
            return;
        }
        this.running = true;
        PipesManager copperPipes = PipesManager.getInstant(typeEnum);
        long selftimer = System.currentTimeMillis();
        PipeLookUpInfo lookUp = copperPipes.getLookUp(pipeGroupID);
        if (lookUp != null) {
            if (pipSaves == null)
            {
                selftimerRunner = System.currentTimeMillis();
                pipSaves = new PipeSendingInfo(pipeGroupID, lookUp);
                List<Location> OutChestsInGroup = copperPipes.getOutChestsInGroup(pipeGroupID);
                pipSaves.outBound.setLocations(OutChestsInGroup);
            }

            for (int j = pipSaves.outBound.getCurrentLocationIndex(); j < pipSaves.outBound.getLocations().size(); j++) {
                Location outLocation = pipSaves.outBound.getLocations().get(j);
                pipSaves.outBound.setCurrentLocation(outLocation);
                pipSaves.outBound.setSlots(ContainerManager.getInventorySlots(pipeGroupID, outLocation));
                long selftimer2 = System.currentTimeMillis() - selftimer;

                if (selftimer2 > KickOutTime || !this.power  || !TitanMachines.pipedEnabled) return;
                for(int i = pipSaves.outBound.getIndex(); i < pipSaves.outBound.getSlots().size(); i++)
                {

                    int slot = pipSaves.outBound.getSlot(i);
                    pipSaves.outBound.setIndex(i);
                    pipSaves.outBound.setCurrentSlot(slot);

                    selftimer2 = System.currentTimeMillis() - selftimer;
                    if (selftimer2 > KickOutTime || !this.power  || !TitanMachines.pipedEnabled) return;
                    ItemStack item = ContainerManager.getInventorySlot(pipSaves.outBound);
                    pipSaves.outBound.setItemStack(item);


                    PipeChestFilterTypeEnum OutChestSettingsFilterType = copperPipes.getChestSettingsFilterType(outLocation, pipeGroupID, slot);
                    if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            CheckAllFilterTypes(pipSaves);
                        }
                    } else if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, pipeGroupID, slot);
                            if (chestSettingsFilter.getType() == item.getType()) {
                                CheckAllFilterTypes(pipSaves);
                            }
                        }
                    }else if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, pipeGroupID, slot);
                            if (TitanMachines.itemStackTool.isItemEqual(chestSettingsFilter, item)) {
                                CheckAllFilterTypes(pipSaves);
                            }
                        }
                    }

                }
                pipSaves.outBound.setIndex(0);
            }

        }
        selftimer = System.currentTimeMillis() - selftimer;
        if (selftimer > this.tickTime / 3L)
        {

        }
        PipeRunnable.instance.setRunningTimeNow(pipeGroupID, System.currentTimeMillis() - selftimerRunner);
        PipeRunnable.instance.setLastTimeRan(pipeGroupID);
        PipeRunnable.instance.setTimeNow(pipeGroupID);

        this.running = false;
        pipSaves = null;
        //if (selftimer > this.tickTime) TitanMachines.messageTool.sendMessageSystem("Took to long, Time: " + selftimer + " ms");
        //current = null;
        //typeEnum = null;
        //busy = false;

    }
    public String getProgress()
    {
        if (pipSaves == null) return "waiting...";
        return pipSaves.outBound.getCurrentLocationIndex() + "/" + pipSaves.outBound.getLocations().size();
    }

    public UUID getRunnerID() {
        return runnerID;
    }

    private void CheckAllFilterTypes(PipeSendingInfo sendingInfo) {
        PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
        checkAndMove(sendingInfo, match);
        match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
        checkAndMove(sendingInfo, match);
        match = PipeChestFilterTypeEnum.ALL;
        checkAndMove(sendingInfo, match);
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
    private void checkAndMove(PipeSendingInfo sendingInfo, PipeChestFilterTypeEnum match) {
        ItemStack item = sendingInfo.outBound.getItemStack();
        List<Location> itemLocations = sendingInfo.getLookUp().getItemLocation(match, item);
        for (Location itemLocation : itemLocations) {
            List<Integer> itemSlots = sendingInfo.getLookUp().getItemSlot(match, item, itemLocation);
            for (Integer slot : itemSlots) {
                item = checkSort(sendingInfo.getGroup(), itemLocation, item, slot);//place in the chest
                ContainerManager.setInventorySlot(sendingInfo.getGroup(), sendingInfo.outBound.getCurrentLocation(), item, sendingInfo.outBound.getCurrentSlot());//send back leftovers
                if (item == null) return;
            }
        }
    }
}
