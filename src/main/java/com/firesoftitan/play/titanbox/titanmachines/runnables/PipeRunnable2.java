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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PipeRunnable2 extends BukkitRunnable {
    private boolean test = false;
    private final List<UUID> groupsQ = new ArrayList<UUID>();
    private final long tickTime;

    public PipeRunnable2(int ticks) {
        this.tickTime = ticks * 50L;
        this.runTaskTimer(TitanMachines.instants, ticks, ticks);
    }

    @Override
    public void run() {
        if (!TitanMachines.pipedEnabled || TitanMachines.pipedEnabled) return;
        long startTImeL = System.currentTimeMillis();
        if (groupsQ.isEmpty())
        {
            List<UUID> groups = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroups();
            groupsQ.addAll( groups);
        }
        int count = 0;
        long done;
        for (UUID group : groupsQ) {
            done = System.currentTimeMillis() - startTImeL;
            long maxTick = this.tickTime / 3L;
            if (done > maxTick) break;
            count++;
            PipeLookUpInfo lookUp = PipesManager.getInstant(PipeTypeEnum.COPPER).getLookUp(group);
            if (lookUp != null) {
                List<Location> OutChestsInGroup = PipesManager.getInstant(PipeTypeEnum.COPPER).getOutChestsInGroup(group);
                for (Location outLocation : OutChestsInGroup) {
                    for (int i : ContainerManager.getInventorySlots(group, outLocation)) {
                        PipeChestFilterTypeEnum OutChestSettingsFilterType = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilterType(outLocation, group, i);
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
                                checkAndMove(group, outLocation, i, lookUp, match, item);
                                match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
                                checkAndMove(group, outLocation, i, lookUp, match, item);
                                match = PipeChestFilterTypeEnum.ALL;
                                checkAndMove(group, outLocation, i, lookUp, match, item);
                            }
                        }
                    }
                }
            } else {
                if (!test) System.out.println("Group not found: " + group);
            }
        }
        groupsQ.subList(0, count).clear();
        test = true;
        done = System.currentTimeMillis() - startTImeL;
        if (done > this.tickTime) TitanMachines.messageTool.sendMessageSystem("Took to long, " + count + " pipes, Time: " + done + " ms");

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
