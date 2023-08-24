package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterType;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PipeSubRunnable extends BukkitRunnable {

    private List<Location> OutChestsInGroup;
    private List<Location> InChestsInGroup;
    private List<Location> OverflowInGroup;
    private UUID uuid;
    private PipeRunnable parent;
    private boolean done;
    private long createdTime;
    public PipeSubRunnable() {
        done = true;
    }
    public PipeSubRunnable(UUID uuid, PipeRunnable parent) {
        this.uuid = uuid;
        this.parent = parent;
        this.done = false;
        OutChestsInGroup = PipesManager.instance.getOutChestsInGroup(uuid);
        InChestsInGroup = PipesManager.instance.getInChestsInGroup(uuid);
        OverflowInGroup = PipesManager.instance.getOverflowInGroup(uuid);
        createdTime = System.currentTimeMillis();

    }

    public boolean isDone() {
        return done;
    }
    private boolean overflow = false;
    @Override
    public void run() {
        if (!TitanMachines.pipedEnabled) {
            done = true;
            this.cancel();
            return;
        }
        long lifeSpan = System.currentTimeMillis() - createdTime;
        lifeSpan = lifeSpan / 1000;
        if (lifeSpan > 120000) TitanMachines.messageTool.sendMessageSystem("Pipe sub has been alive to long:" + uuid + ":" + lifeSpan + " seconds");

        if (OutChestsInGroup.isEmpty())
        {
            done = true;
            this.cancel();
            return;
        }
        long startTIme = System.currentTimeMillis();

        Location chestOut = OutChestsInGroup.get(0).clone();
        Location chestIn = null;
        Location overFlow = null;
        if (InChestsInGroup.isEmpty())
        {
            if (!OverflowInGroup.isEmpty()) {
                overFlow = OverflowInGroup.get(0).clone();
                OverflowInGroup.remove(0);
            }


            if (overFlow != null) {
                if (!ContainerManager.hasAvailableSlot(chestOut)) { //if chest is full then move to overflow
                    overflow = true;
                }
                if (overflow)
                {
                    List<Integer> chestSettingsFilterAccessSlots = PipesManager.instance.getChestSettingsFilterAccessSlots(overFlow, uuid);
                    for (int k : chestSettingsFilterAccessSlots) {
                        ItemStack InChestSettingsFilter = PipesManager.instance.getChestSettingsFilter(overFlow, uuid, k);
                        PipeChestFilterType InChestSettingsFilterType = PipesManager.instance.getChestSettingsFilterType(overFlow, uuid, k);
                        scanChest(uuid, k, chestOut, overFlow, InChestSettingsFilterType, InChestSettingsFilter); //only overflow stacks of 64
                    }
                }
            }
            if (overFlow == null || OverflowInGroup.isEmpty()) {
                overflow = false;
                OutChestsInGroup.remove(0);
                InChestsInGroup = PipesManager.instance.getInChestsInGroup(uuid);
                OverflowInGroup = PipesManager.instance.getOverflowInGroup(uuid);
            }
        }
        else {
            chestIn = InChestsInGroup.get(0).clone();
            InChestsInGroup.remove(0);

            List<Integer> chestSettingsFilterAccessSlots = PipesManager.instance.getChestSettingsFilterAccessSlots(chestIn, uuid);
            for (int k : chestSettingsFilterAccessSlots) {
                ItemStack InChestSettingsFilter = PipesManager.instance.getChestSettingsFilter(chestIn, uuid, k);
                PipeChestFilterType InChestSettingsFilterType = PipesManager.instance.getChestSettingsFilterType(chestIn, uuid, k);
                scanChest(uuid, k, chestOut, chestIn, InChestSettingsFilterType, InChestSettingsFilter);
            }
        }
        long doneTime = System.currentTimeMillis() - startTIme;
        if (doneTime > 500)
            TitanMachines.messageTool.sendMessageSystem("Pipe sub task took to long uuid:" + uuid + ":" + doneTime + " ms");

    }
    private void scanChest(UUID group, int slot, Location chestOut, Location chestIn, PipeChestFilterType chestInFilterType, ItemStack chestInFilterItem)
    {
        for(int i: ContainerManager.getInventorySlots(chestOut))
        {
            PipeChestFilterType OutChestSettingsFilterType = PipesManager.instance.getChestSettingsFilterType(chestOut, group, i);
            if (OutChestSettingsFilterType == PipeChestFilterType.ALL) {
                ItemStack item = ContainerManager.getInventorySlot(chestOut, i);

                if (!TitanMachines.itemStackTool.isEmpty(item)) {

                    if (chestInFilterType == PipeChestFilterType.ALL) {
                        item = checkSort(slot, chestIn, item);
                        ContainerManager.setInventorySlot(chestOut, item, i);
                        return;
                    } else if (chestInFilterType == PipeChestFilterType.MATERIAL_ONLY &&
                            item.getType() == chestInFilterItem.getType()) {
                        item = checkSort(slot, chestIn, item);
                        ContainerManager.setInventorySlot(chestOut, item, i);
                        return;
                    } else if (chestInFilterType == PipeChestFilterType.TOTAL_MATCH &&
                            TitanMachines.itemStackTool.isItemEqual(item, chestInFilterItem)) {
                        item = checkSort(slot, chestIn, item);
                        ContainerManager.setInventorySlot(chestOut, item, i);
                        return;
                    }

                }
            }
        }
    }

    @Nullable
    private ItemStack checkSort(int slot, Location chestIn, ItemStack item) {
        item = placeInChestFixed(chestIn, item, slot);
        return item;
    }

    private ItemStack placeInChestFixed(Location chest, ItemStack itemStack, int slot)
    {

        if (ContainerManager.isContainer(chest)) {
            ItemStack item = ContainerManager.getInventorySlot(chest, slot);
            if (TitanMachines.itemStackTool.isEmpty(item)) {
                ContainerManager.setInventorySlot(chest, itemStack, slot);
                return null;
            } else if (TitanMachines.itemStackTool.isItemEqual(itemStack, item)) {
                int amount = item.getAmount();
                int max = item.getMaxStackSize();
                int needed = max - amount;
                int have = itemStack.getAmount();
                if (have == 0) return itemStack.clone();
                if (have > needed) {
                    itemStack.setAmount(have - needed);
                    item.setAmount(max);
                    ContainerManager.setInventorySlot(chest, item, slot);
                    return itemStack.clone();
                }
                item.setAmount(amount + have);
                ContainerManager.setInventorySlot(chest, item, slot);
                return null;
            }
        }
        return itemStack.clone();
    }
}
