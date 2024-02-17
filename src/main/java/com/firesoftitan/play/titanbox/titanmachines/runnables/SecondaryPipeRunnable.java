package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeLookUpInfo;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeSendingInfo;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecondaryPipeRunnable extends BukkitRunnable {
    private final long tickTime;
    private final UUID myId;
    private final List<UUID> tookLong = new ArrayList<UUID>();

    private boolean busy = false;
    private UUID current;
    private PipeTypeEnum typeEnum;

    private PipeSendingInfo  pipSaves;
    public SecondaryPipeRunnable(int ticks) {
        this.myId = UUID.randomUUID();
        this.tickTime = ticks;
        this.runTaskTimer(TitanMachines.instants, this.tickTime, this.tickTime);
    }

    public boolean isBusy() {
        return busy;
    }
    public void start(PipeTypeEnum pipeTypeEnum, UUID group)
    {
        busy = true;
        typeEnum = pipeTypeEnum;
        current = group;
    }
    @Override
    public void run() {
        if (!busy || current == null || typeEnum == null) return;
        if (!TitanMachines.pipedEnabled) return;


        PipesManager copperPipes = PipesManager.getInstant(typeEnum);
        long selftimer = System.currentTimeMillis();
        PipeLookUpInfo lookUp = copperPipes.getLookUp(current);
        if (lookUp != null) {
            if (pipSaves == null)
            {
                pipSaves = new PipeSendingInfo(current, lookUp);
                List<Location> OutChestsInGroup = copperPipes.getOutChestsInGroup(current);
                pipSaves.outBound.setLocations(OutChestsInGroup);
            }

            for (int j = pipSaves.outBound.getCurrentLocationIndex(); j < pipSaves.outBound.getLocations().size(); j++) {
                Location outLocation = pipSaves.outBound.getLocations().get(j);
                pipSaves.outBound.setCurrentLocation(outLocation);
                pipSaves.outBound.setSlots(ContainerManager.getInventorySlots(current, outLocation));
                long selftimer2 = System.currentTimeMillis() - selftimer;
                if (selftimer2 > this.tickTime / 3L) return;

                for(int i = pipSaves.outBound.getIndex(); i < pipSaves.outBound.getSlots().size(); i++)
                {

                    int slot = pipSaves.outBound.getSlot(i);
                    pipSaves.outBound.setIndex(i);
                    pipSaves.outBound.setCurrentSlot(slot);

                    selftimer2 = System.currentTimeMillis() - selftimer;
                    if (selftimer2 > this.tickTime / 3L) return;
                    ItemStack item = ContainerManager.getInventorySlot(pipSaves.outBound);
                    pipSaves.outBound.setItemStack(item);


                    PipeChestFilterTypeEnum OutChestSettingsFilterType = copperPipes.getChestSettingsFilterType(outLocation, current, slot);
                    if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            CheckAllFilterTypes(pipSaves);
                        }
                    } else if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, current, slot);
                            if (chestSettingsFilter.getType() == item.getType()) {
                                CheckAllFilterTypes(pipSaves);
                            }
                        }
                    }else if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                        if (!TitanMachines.itemStackTool.isEmpty(item)) {
                            ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, current, slot);
                            if (TitanMachines.itemStackTool.isItemEqual(chestSettingsFilter, item)) {
                                CheckAllFilterTypes(pipSaves);
                            }
                        }
                    }

                }
                pipSaves.outBound.setIndex(0);
            }

        }
        else
        {
            tookLong.remove(current);
        }
        selftimer = System.currentTimeMillis() - selftimer;
        if (selftimer > this.tickTime / 3L)
        {

        }
        PipeRunnable.instance.setLastTimeRan(current);
        PipeRunnable.instance.setTimeNow(current);
        pipSaves = null;
        if (selftimer > this.tickTime) TitanMachines.messageTool.sendMessageSystem("Took to long, Time: " + selftimer + " ms");
        current = null;
        typeEnum = null;
        busy = false;

    }
    public void addPipeGroup(UUID uuid)
    {
        if (!tookLong.contains(uuid)) tookLong.add(uuid);
    }
    public boolean hasPipeGroup(UUID uuid)
    {
        return tookLong.contains(uuid);
    }
    public List<UUID> myPipesGroups()
    {
        return new ArrayList<UUID>(tookLong);
    }
    public UUID getMyId() {
        return myId;
    }

    public int getQSize()
    {
        return tookLong.size();
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
