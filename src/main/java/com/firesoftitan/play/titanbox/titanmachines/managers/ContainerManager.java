package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ContainerManager {
    public static boolean isContainer(Location location)
    {
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, location)) return true;
        if (location.getBlock().getState() instanceof Container) return true;
        if (SensibleToolboxSupport.instance.isSupported(location)) return true;
        return SlimefunSupport.instance.isSupported(location);
    }
    public static ItemStack addToInventory(UUID pipe, Location location, ItemStack itemStack)
    {
        Location pipeLocation = JunctionBoxBlock.getPipeLocation(pipe, location);
        return addToInventory(pipeLocation, location, itemStack);
    }
    public static ItemStack addToInventory(Location from, Location location, ItemStack itemStack)
    {
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, location))
        {
            TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, location);
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            BlockFace face = location.getBlock().getFace(from.getBlock());
            if (junctionBoxBlock != null && face != null) {
                Inventory inventory = junctionBoxBlock.getInventory(face);
                HashMap<Integer, ItemStack> integerItemStackHashMap = inventory.addItem(itemStack.clone());
                for (ItemStack itemStack1 : integerItemStackHashMap.values()) {
                    if (!TitanMachines.itemStackTool.isEmpty(itemStack1)) return itemStack1.clone();
                }
                junctionBoxBlock.setInventory(face, inventory);
            }
            return null;
        }
        if (TitanMachines.itemStackTool.isEmpty(itemStack)) return null;

        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            return SensibleToolboxSupport.instance.addStorage(location, itemStack);
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            for(Integer I: SlimefunSupport.instance.getInventorySlots(location))
            {
                ItemStack itemInSlot = SlimefunSupport.instance.getItemInSlot(location, I);
                if (TitanMachines.itemStackTool.isEmpty(itemInSlot))
                {
                    SlimefunSupport.instance.setItemInSlot(location, I, itemStack.clone());
                    return null;
                }
                if (TitanMachines.itemStackTool.isItemEqual(itemStack, itemInSlot))
                {
                    int amount = itemInSlot.getAmount();
                    int max = itemInSlot.getMaxStackSize();
                    int needed = max - amount;
                    int have = itemStack.getAmount();
                    if (needed > 0) {
                        if (have == 0) return itemStack.clone();
                        if (have > needed) {
                            itemStack.setAmount(have - needed);
                            itemInSlot.setAmount(max);
                            SlimefunSupport.instance.setItemInSlot(location,I, itemInSlot);
                            return itemStack.clone();
                        }
                        itemInSlot.setAmount(amount + have);
                        SlimefunSupport.instance.setItemInSlot(location,I, itemInSlot);
                        return null;
                    }
                }
            }
            return itemStack.clone();
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            HashMap<Integer, ItemStack> integerItemStackHashMap = inventory.addItem(itemStack.clone());
            for(ItemStack itemStack1: integerItemStackHashMap.values())
            {
                if (!TitanMachines.itemStackTool.isEmpty(itemStack1)) return itemStack1.clone();
            }
            return null;
        }
        return itemStack.clone();
    }
    public static boolean hasAvailableSlot(UUID pipe, Location location)
    {
        Location pipeLocation = JunctionBoxBlock.getPipeLocation(pipe, location);
        return hasAvailableSlot(pipeLocation, location);
    }
    public static boolean hasAvailableSlot(Location from, Location location)
    {
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, location))
        {
            TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, location);
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            BlockFace face = location.getBlock().getFace(from.getBlock());
            if (junctionBoxBlock != null && face != null) {
                Inventory inventory = junctionBoxBlock.getInventory(face);
                return inventory.firstEmpty() > -1;
            }
        }
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            return SensibleToolboxSupport.instance.hasAvailableSlot(location);
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            for(Integer I: SlimefunSupport.instance.getInventorySlots(location))
            {
                if (TitanMachines.itemStackTool.isEmpty(SlimefunSupport.instance.getItemInSlot(location, I))) return true;
            }
            return false;
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            return inventory.firstEmpty() > -1;
        }
        return false;
    }
    public static void setInventorySlot(UUID pipe, Location location, ItemStack itemStack, int slot)
    {
        Location pipeLocation = JunctionBoxBlock.getPipeLocation(pipe, location);
        setInventorySlot(pipeLocation, location, itemStack, slot);
    }
    public static void setInventorySlot(Location from, Location location, ItemStack itemStack, int slot)
    {
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, location))
        {
            TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, location);
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            BlockFace face = location.getBlock().getFace(from.getBlock());
            if (junctionBoxBlock != null && face != null) {
                Inventory inventory = junctionBoxBlock.getInventory(face);
                inventory.setItem(slot, itemStack);
                junctionBoxBlock.setInventory(face, inventory);
            }
            return;
        }
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            SensibleToolboxSupport.instance.setInventorySlot(location, itemStack);
            return;
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            SlimefunSupport.instance.setItemInSlot(location, slot, itemStack);
            return;
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            if (slot < inventory.getSize()) inventory.setItem(slot, itemStack);
        }

    }
    public static ItemStack getInventorySlot(UUID pipe, Location location, int slot)
    {
        Location pipeLocation = JunctionBoxBlock.getPipeLocation(pipe, location);
        return getInventorySlot(pipeLocation, location, slot);
    }
    public static ItemStack getInventorySlot(Location from, Location location, int slot)
    {
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, location))
        {
            TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, location);
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            BlockFace face = location.getBlock().getFace(from.getBlock());
            if (junctionBoxBlock != null && face != null) {
                Inventory inventory = junctionBoxBlock.getInventory(face);
                return inventory.getItem(slot);
            }
        }
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            return SensibleToolboxSupport.instance.getInventorySlot(location);
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            return SlimefunSupport.instance.getItemInSlot(location, slot);
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            if (slot < inventory.getSize()) return inventory.getItem(slot);
        }

        return null;
    }
    public static Set<Integer> getInventorySlots(UUID pipe, Location chest)
    {
        Location pipeLocation = JunctionBoxBlock.getPipeLocation(pipe, chest);
        return getInventorySlots(pipeLocation, chest);
    }
    public static Set<Integer> getInventorySlots(Location pipe, Location chest)
    {
        Set<Integer> out = new HashSet<Integer>();
        if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, chest))
        {
            if (pipe == null) return out;
            TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, chest);
            if (titanBlock.getTitanID().equals(JunctionBoxBlock.titanID)) {
                JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
                if (junctionBoxBlock != null) {
                    BlockFace face = chest.getBlock().getFace(pipe.getBlock());
                    if (face != null) {
                        Inventory inventory = junctionBoxBlock.getInventory(face);
                        for (int i = 0; i < inventory.getSize(); i++) {
                            out.add(i);
                        }
                    }
                }
                return out;
            }
        }
        if (SensibleToolboxSupport.instance.isSupported(chest))
        {
            out.add(0);
            return out;
        }

        if (SlimefunSupport.instance.isSupported(chest)) {
            return SlimefunSupport.instance.getInventorySlots(chest);
        }

        if (chest.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            for(int i = 0; i < inventory.getSize(); i++)
            {
                out.add(i);
            }
            return out;
        }

        return out;
    }


}
