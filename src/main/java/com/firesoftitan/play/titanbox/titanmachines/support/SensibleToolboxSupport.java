package com.firesoftitan.play.titanbox.titanmachines.support;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BigStorageUnit;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SensibleToolboxSupport extends PluginSupport{
    public static SensibleToolboxSupport instance;
    public SensibleToolboxSupport() {
        super("SensibleToolbox");
        SensibleToolboxSupport.instance = this;
    }
    public boolean isSupported(Location location)
    {
        if (!this.isInstalled()) return false;
        BaseSTBBlock blockAt = SensibleToolbox.getBlockAt(location);
        if (blockAt != null)
        {
            return blockAt.getItemTypeID().equals("bigstorageunit") || blockAt.getItemTypeID().equals("hyperstorageunit");
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
                return baseSTBItem.getItemTypeID().equals("bigstorageunit") || baseSTBItem.getItemTypeID().equals("hyperstorageunit");
            }
        }
        return false;
    }
    public ItemStack getStoredItem(Location location)
    {
        if (isSupported(location))
        {
            BigStorageUnit BSU = getBigStorageUnit(location);
            return BSU.getStoredItemType();
        }
        return null;
    }
    public ItemStack getInventorySlot(Location location) {
        if (this.isSupported(location)) {
            BigStorageUnit BSU = this.getBigStorageUnit(location);
            if (BSU.getStoredItemType() == null) return null;

            ItemStack out = BSU.getStoredItemType().clone();
            if (BSU.getTotalAmount() == 0) return null;
            out.setAmount(Math.min(BSU.getTotalAmount(), out.getMaxStackSize()));
            return out;
        }
        return null;
    }
    public void setInventorySlot(Location location, ItemStack itemStack) {
        if (this.isSupported(location)) {
            BigStorageUnit BSU = this.getBigStorageUnit(location);
            ItemStack storedItem = BSU.getStoredItemType();
            if (storedItem == null)
            {
                if (itemStack != null)
                {
                    BSU.setStoredItemType(itemStack);
                    storedItem = BSU.getStoredItemType();
                }
                else if (BSU.getStorageAmount() < 1) return;
            }
            //This section is backwards because it is a single slot
            //normally I can override the slot with the new amount, but if you do that here
            //you will add to the storage
            int amount = 0;
            if (itemStack != null)
            {
                amount = itemStack.getAmount();
            }
            int size = -1 * (storedItem.getMaxStackSize() - amount);
            if (size != 0) { //Zero means no changes needed
                if (BSU.getStorageAmount() < 1) {
                    //this is the normal chest slot, so we can change it normal, but
                    //only if the storage amount is 0
                    BSU.setOutputAmount(0);
                    BSU.getInventory().setItem(BSU.getOutputSlots()[0], itemStack);

                }
                else BSU.setStorageAmount(BSU.getStorageAmount() + size);
            }

            if (BSU.getStorageAmount() < 0) BSU.setStorageAmount(0);

        }
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
                        BigStorageUnit BSU = this.getBigStorageUnit(location);
                        Result result = new Result();
                        result.location = BSU.getLocation();
                        result.item = BSU.getOutputItem();
                        results.add(result);
                    }
                }
            }

        }
        return results;
    }
    public ItemStack addStorage(Location location, ItemStack itemStack)
    {
        if (TitanMachines.tools.getItemStackTool().isEmpty(itemStack)) return null;
        if (isSupported(location)) {
            BigStorageUnit BSU = getBigStorageUnit(location);
            if (BSU.getStoredItemType() == null && !BSU.isLocked())
            {
                BSU.setStoredItemType(itemStack.clone());
                BSU.setLocked(true);
            }
            if (BSU.getStoredItemType() != null) {
                if (TitanMachines.tools.getItemStackTool().isItemEqual(BSU.getStoredItemType(), itemStack.clone())) {
                    if (BSU.getTotalAmount() < BSU.getStackCapacity() * (BSU.getStoredItemType().getMaxStackSize())) {
                        BSU.setStorageAmount(BSU.getStorageAmount() + itemStack.getAmount());
                        return null;
                    }
                    return itemStack.clone();
                }
            }
        }
        return itemStack.clone();
    }
    public boolean hasAvailableSlot(Location location)
    {
        if (isSupported(location)) {
            BigStorageUnit BSU = getBigStorageUnit(location);
            return BSU.getTotalAmount() < BSU.getStackCapacity() * (BSU.getStoredItemType() == null ? 64 : BSU.getStoredItemType().getMaxStackSize());
        }
        return false;
    }

    private BigStorageUnit getBigStorageUnit(Location location) {
        BaseSTBBlock blockAt = SensibleToolbox.getBlockAt(location);
        return (BigStorageUnit)blockAt;
    }

}
