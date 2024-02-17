package com.firesoftitan.play.titanbox.titanmachines.infos;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InOutBoundInfo {
    private List<Integer> slots;
    private Integer currentSlot, index;

    private List<Location> locations;
    private Location location;

    private ItemStack itemStack;

    private final UUID group;

    public InOutBoundInfo(UUID group) {
        this.group = group;
        currentSlot = 0;
        index = 0;
    }

    public UUID getGroup() {
        return group;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(Set<Integer> slots) {
        this.slots = new ArrayList<Integer>(slots);
    }
    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getSlot(int i)
    {
        return this.slots.get(i);
    }
    public Integer getCurrentSlot() {
        return currentSlot;
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public int getCurrentLocationIndex()
    {
        if (getCurrentLocation() == null) return 0;
        return locations.indexOf(getCurrentLocation());
    }
    public Location getCurrentLocation() {
        return location;
    }

    public void setCurrentLocation(Location location) {
        this.location = location;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
