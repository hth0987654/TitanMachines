package com.firesoftitan.play.titanbox.titanmachines.support;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BigStorageUnit;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.HyperStorageUnit;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SensibleToolboxSupport extends PluginSupport{
    public static SensibleToolboxSupport instance;
    public SensibleToolboxSupport() {
        super("SensibleToolbox");
        SensibleToolboxSupport.instance = this;
    }
    public boolean isStorage(Location location)
    {
        if (!this.isInstalled()) return false;
        BaseSTBBlock blockAt = SensibleToolbox.getBlockAt(location);
        if (blockAt != null)
        {
            return blockAt.getItemTypeID().equals("bigstorageunit") || blockAt.getItemTypeID().equals("hyperstorageunit");
        }
        return false;
    }

    public boolean isSupported(Location location)
    {
        if (!this.isInstalled()) return false;
        BaseSTBBlock blockAt = SensibleToolbox.getBlockAt(location);
        if (blockAt != null)
        {
            return (blockAt instanceof BaseSTBMachine);
            //return blockAt.getItemTypeID().equals("bigstorageunit") || blockAt.getItemTypeID().equals("hyperstorageunit");
        }
        return false;
    }
    public boolean isSupported(ItemStack itemStack)
    {
        if (!this.isInstalled()) return false;
        if (SensibleToolbox.getItemRegistry().isSTBItem(itemStack))
        {
            BaseSTBItem baseSTBItem = SensibleToolbox.getItemRegistry().fromItemStack(itemStack);
            if (baseSTBItem != null) {
                //return baseSTBItem.getItemTypeID().equals("bigstorageunit") || baseSTBItem.getItemTypeID().equals("hyperstorageunit");
                return true;

            }
        }
        return false;
    }
    public ItemStack getStoredItem(Location location)
    {
        if (isStorage(location))
        {
            BigStorageUnit BSU = (BigStorageUnit) getSTBMachine(location);
            return BSU.getStoredItemType();
        }
        return null;
    }
    public ItemStack getInventorySlot(Location location, int slot) {
        if (this.isSupported(location)) {
            BaseSTBMachine BSU = this.getSTBMachine(location);
            return BSU.getInventory().getItem(slot);
        }
        return null;
    }
    public void setInventorySlot(Location location, int slot, ItemStack itemStack) {
        if (this.isSupported(location)) {
            BaseSTBMachine BSU = this.getSTBMachine(location);
            BSU.getInventory().setItem(slot, itemStack);
        }
    }

    public Inventory getInventory(Location location)
    {
        if (this.isSupported(location)) {
            BaseSTBMachine BSU = this.getSTBMachine(location);
            return BSU.getInventory();
        }
        return null;
    }
    public Set<Integer> getInventorySlots(Location location) {
        if (this.isSupported(location)) {
            BaseSTBMachine BSU = this.getSTBMachine(location);
            int[] tm = {BSU.getEnergyCellSlot()};
            return Stream.of(BSU.getOutputSlots(),
                            BSU.getInputSlots(),
                            BSU.getUpgradeSlots(),
                            tm)
                    .flatMapToInt(Arrays::stream)
                    .filter(slot -> slot != -1)
                    .boxed()
                    .collect(Collectors.toSet());
        }
        return new HashSet<Integer>();
    }

    public ItemStack addStorage(Location location, ItemStack itemStack) {
        if (this.isSupported(location)) {
            BaseSTBMachine BSU = this.getSTBMachine(location);
            int inputSlot = BSU.getInputSlots()[0];
            if (BSU.getInventory().getItem(inputSlot) == null)
            {
                BSU.getInventory().setItem(inputSlot, itemStack);
                return null;
            }
            else
            {
                if (BSU instanceof BigStorageUnit BSUU) {
                    int maxCapacity = BSUU.getStackCapacity() * itemStack.getMaxStackSize();
                    int value = BSUU.getStorageAmount();
                    if (value + itemStack.getAmount() < maxCapacity)
                    {
                        BSUU.setStorageAmount(value + itemStack.getAmount());
                        return null;
                    }
                }
            }
        }
        return itemStack.clone();
    }

    public static class Result {
        public Location location;
        public ItemStack item;
    }
    public List<Result> ScanChunk(Location location)
    {
        List<Result> results = new ArrayList<Result>();
        if (this.isInstalled()) {
            if (LocationManager.getManager() != null) {
                List<BaseSTBBlock> baseSTBBlocks = LocationManager.getManager().get(location.getChunk());
                for (BaseSTBBlock baseSTBBlock : baseSTBBlocks) {
                    if (this.isSupported(baseSTBBlock.getLocation())) {
                        BaseSTBMachine BSU = this.getSTBMachine(location);
                        Result result = new Result();
                        result.location = BSU.getLocation();
                        //result.item = BSU.getOutputItem();
                        results.add(result);
                    }
                }
            }

        }
        return results;
    }
    public boolean hasAvailableSlot(Location location)
    {
        if (isSupported(location)) {
            BaseSTBMachine BSU = getSTBMachine(location);
            for (int slot: BSU.getInputSlots())
            {
                if (BSU.getInventory().getItem(slot) == null) return true;
            }
            //return BSU.getTotalAmount() < BSU.getStackCapacity() * (BSU.getStoredItemType() == null ? 64 : BSU.getStoredItemType().getMaxStackSize());
        }
        return false;
    }

    private BaseSTBMachine getSTBMachine(Location location) {
        BaseSTBBlock blockAt = SensibleToolbox.getBlockAt(location);
        if (blockAt instanceof BaseSTBMachine) return (BaseSTBMachine) blockAt;
        return null;
    }

}
