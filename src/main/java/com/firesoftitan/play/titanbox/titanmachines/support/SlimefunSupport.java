package com.firesoftitan.play.titanbox.titanmachines.support;

import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

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
            return menu.getItemInSlot(slot);
        }
        return null;
    }
}
