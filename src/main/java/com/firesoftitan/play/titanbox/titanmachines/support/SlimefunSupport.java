package com.firesoftitan.play.titanbox.titanmachines.support;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SlimefunSupport extends PluginSupport{
    public static SlimefunSupport instance;
    public SlimefunSupport() {
        super("Slimefun");
        SlimefunSupport.instance = this;
    }
    public boolean isSupported(Location location)
    {
        if (this.isInstalled()) {
            return BlockStorage.hasInventory(location.getBlock());
        }
        return false;
    }
    public boolean isSupported(ItemStack itemStack)
    {
        if (!this.isInstalled()) return false;
        for(SlimefunItem item: Slimefun.getRegistry().getAllSlimefunItems())
        {
            if (TitanMachines.itemStackTool.isItemEqual(item.getItem(), itemStack)) return true;
        }
        return false;
    }


    public Set<Integer> getInventorySlots(Location location)
    {
        if (this.isInstalled()) {
            BlockMenu menu = BlockStorage.getInventory(location);
            BlockMenuPreset preset = menu.getPreset();
            return preset.getInventorySlots();
        }
        return Collections.emptySet();
    }
    public Set<Integer> getPresetSlots(Location location)
    {
        if (this.isInstalled()) {
            BlockMenu menu = BlockStorage.getInventory(location);
            BlockMenuPreset preset = menu.getPreset();

            return preset.getPresetSlots();
        }
        return Collections.emptySet();
    }
    public void setItemInSlot(Location location, int slot, ItemStack itemStack)
    {
        if (this.isInstalled()) {
            BlockMenu menu = BlockStorage.getInventory(location);
            menu.replaceExistingItem(slot, itemStack);
        }
    }
    public ItemStack getItemInSlot(Location location, int slot)
    {
        if (this.isInstalled()) {
            BlockMenu menu = BlockStorage.getInventory(location);
            ItemStack itemInSlot = menu.getItemInSlot(slot);
            if (itemInSlot != null && itemInSlot.getType() == Material.BARRIER) return null;
            return itemInSlot;
        }
        return null;
    }
}
