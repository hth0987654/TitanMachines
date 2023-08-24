package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ContainerManager {
    public static boolean isContainer(Location location)
    {
        if (location.getBlock().getState() instanceof Container) return true;
        if (SensibleToolboxSupport.instance.isSupported(location)) return true;
        return SlimefunSupport.instance.isSupported(location);
    }
    public static ItemStack addToInventory(Location location, ItemStack itemStack)
    {
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
    public static boolean hasAvailableSlot(Location location)
    {
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
    public static void setInventorySlot(Location location, ItemStack itemStack, int slot)
    {
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            SensibleToolboxSupport.instance.setInventorySlot(location, itemStack);
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            SlimefunSupport.instance.setItemInSlot(location, slot, itemStack);
            return;
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            inventory.setItem(slot, itemStack);
            return;
        }

    }
    public static ItemStack getInventorySlot(Location location, int slot)
    {
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            return SensibleToolboxSupport.instance.getInventorySlot(location);
        }
        if (SlimefunSupport.instance.isSupported(location)) {
            return SlimefunSupport.instance.getItemInSlot(location, slot);
        }
        if (location.getBlock().getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            return inventory.getItem(slot);
        }

        return null;
    }
    public static Set<Integer> getInventorySlots(Location location)
    {
        if (SensibleToolboxSupport.instance.isSupported(location))
        {
            Set<Integer> out = new HashSet<Integer>();
            out.add(0);
            return out;
        }
        Set<Integer> out = new HashSet<Integer>();
        if (SlimefunSupport.instance.isSupported(location)) {
            return SlimefunSupport.instance.getInventorySlots(location);
        }

        if (location.getBlock().getState() instanceof Container container) {
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
