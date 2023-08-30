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
        if (TitanMachines.itemStackTool.getTitanItemID(itemStack).toUpperCase().equals("JUNCTION_BOX")) return true;
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
    public ItemStack getOutputItem(Location location)
    {
        if (isSupported(location))
        {
            BigStorageUnit BUS = getBigStorageUnit(location);
            return BUS.getOutputItem();
        }
        return null;
    }
    public ItemStack getInventorySlot(Location location) {
        if (this.isSupported(location)) {
            BigStorageUnit BSU = this.getBigStorageUnit(location);
            if (BSU.getOutputItem() == null) return null;
            ItemStack out = BSU.getOutputItem();
            if (BSU.getChargeRate() > 0) {
                if (BSU.getCharge() < BSU.getChargePerOperation(Math.min(BSU.getTotalAmount(), out.getMaxStackSize())))
                    return null;
            }
            if (BSU.getTotalAmount() == 0) return null;
            out.setAmount(Math.min(BSU.getTotalAmount(), out.getMaxStackSize()));
            return out;
        }
        return null;
    }
    public void setInventorySlot(Location location, ItemStack itemStack) {
        if (this.isSupported(location)) {
            BigStorageUnit BSU = this.getBigStorageUnit(location);
            int size = BSU.getOutputItem().getMaxStackSize();
            if (itemStack != null) {
                size = itemStack.getAmount();
            }
            if (BSU.getChargeRate() > 0) {
                double charge = BSU.getCharge() - BSU.getChargePerOperation(Math.min(BSU.getTotalAmount(), size));
                BSU.setCharge(charge);
            }
            int min = Math.min(BSU.getTotalAmount(), size);
            min = min - BSU.getOutputAmount();
            BSU.setOutputAmount(0);
            BSU.getInventory().setItem(BSU.getOutputSlots()[0], null);
            BSU.setStorageAmount(BSU.getStorageAmount() - min);
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
            if (BSU.getChargeRate() > 0) {
                if (BSU.getCharge() < BSU.getChargePerOperation(Math.min(BSU.getTotalAmount(), BSU.getOutputItem().getMaxStackSize())))
                    return itemStack.clone();
            }
            if (BSU.getStoredItemType() == null && !BSU.isLocked())
            {
                BSU.setStoredItemType(itemStack.clone());
                BSU.setLocked(true);
            }
            if (BSU.getStoredItemType() != null) {
                if (TitanMachines.tools.getItemStackTool().isItemEqual(BSU.getStoredItemType(), itemStack.clone())) {
                    if (BSU.getTotalAmount() < BSU.getStackCapacity() * (BSU.getStoredItemType().getMaxStackSize())) {
                        if (BSU.getChargeRate() > 0) {
                            double charge = BSU.getCharge() - BSU.getChargePerOperation(itemStack.getAmount());
                            BSU.setCharge(charge);
                        }
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
