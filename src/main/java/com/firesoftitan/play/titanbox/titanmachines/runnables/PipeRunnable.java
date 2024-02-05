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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PipeRunnable extends BukkitRunnable {
    private boolean test = false;
    private final List<UUID> groupsQ = new ArrayList<UUID>();
    private final long tickTime;

    public PipeRunnable(int ticks) {
        this.tickTime = ticks * 50L;
        this.runTaskTimer(TitanMachines.instants, ticks, ticks);
    }

    private List<UUID> tookLong = new ArrayList<UUID>();
    private HashMap<String, Integer> keeperofAll = new HashMap<String, Integer>();
    @Override
    public void run() {
        if (!TitanMachines.pipedEnabled) return;
        long startTImeL = System.currentTimeMillis();
        PipesManager copperPipes = PipesManager.getInstant(PipeTypeEnum.COPPER);
        if (groupsQ.isEmpty())
        {
            List<UUID> groups = copperPipes.getGroups();
            groupsQ.addAll( groups);
        }
        int count = 0;
        long done;
        for (UUID group : groupsQ) {
            long selftimer = System.currentTimeMillis();
            done = System.currentTimeMillis() - startTImeL;
            long maxTick = this.tickTime / 3L;
            if (tookLong.contains(group) && count > 0) break; //if we have done a lot of task and the next one will take long stop and continue later
            if (done > maxTick) break; //if pipes are taking to long stop and continue later
            count++;
            PipeLookUpInfo lookUp = copperPipes.getLookUp(group);
            if (lookUp != null) {
                List<Location> OutChestsInGroup = copperPipes.getOutChestsInGroup(group);
                for (Location outLocation : OutChestsInGroup) {
                    List<Integer> intSlotsN = new ArrayList<Integer>(ContainerManager.getInventorySlots(group, outLocation));
                    int x = 0;
                    String key = TitanMachines.serializeTool.serializeLocation(outLocation) + group.toString();
                    if (keeperofAll.containsKey(key)) x = keeperofAll.get(key);
                    for(int j = x; j < intSlotsN.size(); j++)
                    {
                        int i = intSlotsN.get(j);
/*                    }
                    for (int i : ContainerManager.getInventorySlots(group, outLocation)) {*/
                        PipeChestFilterTypeEnum OutChestSettingsFilterType = copperPipes.getChestSettingsFilterType(outLocation, group, i);
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.ALL) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
                                boolean checkAndMove = checkAndMove(group, outLocation, i, lookUp, match, item);
                                match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
                                boolean checkAndMove2 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                match = PipeChestFilterTypeEnum.ALL;
                                boolean checkAndMove3 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                if (checkAndMove || checkAndMove2 || checkAndMove3) {
                                    keeperofAll.put(key, j + 1);
                                    if (j + 1 >= intSlotsN.size()) keeperofAll.remove(key);
                                    break;//makes it less laggy and work as a hopper would
                                }
                            }
                        }
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, group, i);
                                if (chestSettingsFilter.getType() == item.getType()) {
                                    PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
                                    boolean checkAndMove = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
                                    boolean checkAndMove2 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    match = PipeChestFilterTypeEnum.ALL;
                                    boolean checkAndMove3 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    if (checkAndMove || checkAndMove2 || checkAndMove3) {
                                        keeperofAll.put(key, j + 1);
                                        if (j + 1 >= intSlotsN.size()) keeperofAll.remove(key);
                                        break;//makes it less laggy and work as a hopper would
                                    }
                                }
                            }
                        }
                        if (OutChestSettingsFilterType == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                            ItemStack item = ContainerManager.getInventorySlot(group, outLocation, i);
                            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                                ItemStack chestSettingsFilter = copperPipes.getChestSettingsFilter(outLocation, group, i);
                                if (TitanMachines.itemStackTool.isItemEqual(chestSettingsFilter, item)) {
                                    PipeChestFilterTypeEnum match = PipeChestFilterTypeEnum.TOTAL_MATCH;
                                    boolean checkAndMove = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    match = PipeChestFilterTypeEnum.MATERIAL_ONLY;
                                    boolean checkAndMove2 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    match = PipeChestFilterTypeEnum.ALL;
                                    boolean checkAndMove3 = checkAndMove(group, outLocation, i, lookUp, match, item);
                                    if (checkAndMove || checkAndMove2 || checkAndMove3) {
                                        keeperofAll.put(key, j + 1);
                                        if (j + 1 >= intSlotsN.size()) keeperofAll.remove(key);
                                        break;//makes it less laggy and work as a hopper would
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (!test) TitanMachines.messageTool.sendMessageSystem("Group not found: " + group);
            }
            selftimer = System.currentTimeMillis() - selftimer;
            if (selftimer > 100)
            {
                if (!tookLong.contains(group)) tookLong.add(group);

                //TitanMachines.messageTool.sendMessageSystem(selftimer + " " + group);
            }
            if (tookLong.contains(group)) break;
        }
        groupsQ.subList(0, count).clear();
        test = true;
        done = System.currentTimeMillis() - startTImeL;
        if (done > this.tickTime && count > 1) TitanMachines.messageTool.sendMessageSystem("Took to long, " + count + " pipes, Time: " + done + " ms");

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
    private boolean checkAndMove(UUID group, Location location, int i, PipeLookUpInfo lookUp, PipeChestFilterTypeEnum match, ItemStack item) {
        List<Location> itemLocations = lookUp.getItemLocation(match, item);
        for (Location itemLocation : itemLocations) {
            List<Integer> itemSlots = lookUp.getItemSlot(match, item, itemLocation);
            for (Integer slot : itemSlots) {
                item = checkSort(group, itemLocation, item, slot);//place in the chest
                ContainerManager.setInventorySlot(group, location, item, i);//send back leftovers
                if (item == null) return true;
            }
        }
        return false;
    }
}
