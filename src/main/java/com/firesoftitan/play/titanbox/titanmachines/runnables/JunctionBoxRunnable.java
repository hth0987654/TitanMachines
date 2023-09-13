package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class JunctionBoxRunnable extends BukkitRunnable {
    private final List<Location> quList = new ArrayList<Location>();

    @Override
    public void run() {
        if (quList.isEmpty())
        {
            Set<Location> locations = TitanBlockManager.getLocations(JunctionBoxBlock.titanID);
            quList.addAll(locations);
            return;
        }

        TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, quList.get(0));
        if (titanBlock == null)
        {
            quList.remove(0);
            return;
        }
        JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
        if (junctionBoxBlock == null || !junctionBoxBlock.getLocation().getChunk().isLoaded())
        {
            quList.remove(0);
            return;
        }
        quList.remove(0);
        BlockFace noFilter = junctionBoxBlock.getNoFilter();


        for(BlockFace faceFrom: JunctionBoxBlock.blockFaces) {
            Inventory inventoryFrom = junctionBoxBlock.getInventory(faceFrom);
            for (int i = 0; i < inventoryFrom.getSize(); i++) {
                ItemStack item = inventoryFrom.getItem(i);
                if (!TitanMachines.itemStackTool.isEmpty(item)) {
                    BlockFace faceTo = junctionBoxBlock.getFilter(item);
                    if (faceTo != faceFrom && faceFrom != noFilter) {
                        if (faceTo != null) moveItem(junctionBoxBlock, faceFrom, faceTo, i);
                        else moveItem(junctionBoxBlock, faceFrom, noFilter, i);
                    }
                }
            }
        }

    }

    private static void moveItem(JunctionBoxBlock junctionBoxBlock, BlockFace faceFrom, BlockFace faceTo, int slot) {
        BlockFace overflow = junctionBoxBlock.getOverflow();
        Inventory inventoryOverflow = junctionBoxBlock.getInventory(overflow);
        Inventory inventoryTo = junctionBoxBlock.getInventory(faceTo);
        Inventory inventoryFrom = junctionBoxBlock.getInventory(faceFrom);
        ItemStack item = inventoryFrom.getItem(slot);
        if (item == null) return; //sometimes the pipes move it out before we can move it.
        HashMap<Integer, ItemStack> integerItemStackHashMap = inventoryTo.addItem(item.clone());
        inventoryFrom.setItem(slot, null);

        for (ItemStack itemStack1 : integerItemStackHashMap.values()) {
            if (faceFrom != overflow && itemStack1.getAmount() >= itemStack1.getMaxStackSize()) inventoryOverflow.addItem(itemStack1);
            else inventoryFrom.addItem(itemStack1);
        }

        junctionBoxBlock.setInventory(faceTo, inventoryTo);
        junctionBoxBlock.setInventory(faceFrom, inventoryFrom);
        if (faceFrom != overflow && faceTo != overflow) junctionBoxBlock.setInventory(overflow, inventoryOverflow);
    }
}
