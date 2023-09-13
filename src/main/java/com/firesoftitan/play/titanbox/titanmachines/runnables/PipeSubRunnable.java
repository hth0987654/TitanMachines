package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PipeSubRunnable extends BukkitRunnable {

    private List<Location> OutChestsInGroup;
    private List<Location> InChestsInGroup;
    private final UUID uuid;
    private boolean done;
    private final long createdTime;
    private final PipeTypeEnum type;

    public PipeSubRunnable(UUID uuid, PipeTypeEnum type) {
        this.uuid = uuid;
        this.done = false;
        this.type = type;
        OutChestsInGroup = PipesManager.getInstant(this.type).getOutChestsInGroup(uuid);
        InChestsInGroup = PipesManager.getInstant(this.type).getInChestsInGroup(uuid);
        createdTime = System.currentTimeMillis();

    }

    public UUID getUuid() {
        return uuid;
    }

    public PipeTypeEnum getType() {
        return type;
    }

    public boolean isDone() {
        return done;
    }
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
        Location chestIn;
        if (InChestsInGroup.isEmpty())
        {
            OutChestsInGroup.remove(0);
            InChestsInGroup = PipesManager.getInstant(this.type).getInChestsInGroup(uuid);
        }
        else {
            chestIn = InChestsInGroup.get(0).clone();
            InChestsInGroup.remove(0);

            List<Integer> chestSettingsFilterAccessSlots = PipesManager.getInstant(this.type).getChestSettingsFilterAccessSlots(chestIn, uuid);
            for (int k : chestSettingsFilterAccessSlots) {
                ItemStack InChestSettingsFilter = PipesManager.getInstant(this.type).getChestSettingsFilter(chestIn, uuid, k);
                PipeChestFilterTypeEnum InChestSettingsFilterType = PipesManager.getInstant(this.type).getChestSettingsFilterType(chestIn, uuid, k);
                scanChest(uuid, k, chestOut, chestIn, InChestSettingsFilterType, InChestSettingsFilter);
            }
        }
        long doneTime = System.currentTimeMillis() - startTIme;
        if (doneTime > 1000)
            TitanMachines.messageTool.sendMessageSystem("Pipe sub task took to long uuid:" + uuid + ":" + doneTime + " ms");

    }
    private void scanChest(UUID group, int slot, Location chestOut, Location chestIn, PipeChestFilterTypeEnum chestInFilterType, ItemStack chestInFilterItem)
    {
        for(int i: ContainerManager.getInventorySlots(uuid, chestOut))
        {
            PipeChestFilterTypeEnum OutChestSettingsFilterType = PipesManager.getInstant(this.type).getChestSettingsFilterType(chestOut, group, i);
            if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                ItemStack item = ContainerManager.getInventorySlot(uuid, chestOut, i);
                if (!TitanMachines.itemStackTool.isEmpty(item))
                {
                    if (chestInFilterType == PipeChestFilterTypeEnum.ALL) {
                        item = checkSort(chestIn, item, slot); //place in the chest
                        ContainerManager.setInventorySlot(uuid, chestOut, item, i);//send back leftovers
                        return;
                    } else if (chestInFilterType == PipeChestFilterTypeEnum.MATERIAL_ONLY &&
                            item.getType() == chestInFilterItem.getType()) {
                        item = checkSort(chestIn, item, slot);//place in the chest
                        ContainerManager.setInventorySlot(uuid, chestOut, item, i);//send back leftovers
                        return;
                    } else if (chestInFilterType == PipeChestFilterTypeEnum.TOTAL_MATCH &&
                            TitanMachines.itemStackTool.isItemEqual(item, chestInFilterItem)) {
                        item = checkSort(chestIn, item, slot);//place in the chest
                        ContainerManager.setInventorySlot(uuid, chestOut, item, i);//send back leftovers
                        return;

                    }

                }
            }
        }
    }
    private ItemStack checkSort(Location chest, ItemStack itemStack, int slot)
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
}
