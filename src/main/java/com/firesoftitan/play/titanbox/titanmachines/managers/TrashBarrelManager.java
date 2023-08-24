package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TrashBarrelManager {
    private SaveManager trash_Barrel = new SaveManager(TitanMachines.instants.getName(), "trash_barrel");
    public static TrashBarrelManager instance;

    public TrashBarrelManager() {

        TrashBarrelManager.instance = this;
    }
    public void save()
    {
        trash_Barrel.save();
    }
    public Location getLocation(String key)
    {
        if (this.trash_Barrel.contains(key)) {
            Location location = this.trash_Barrel.getLocation(key + ".location");
            return location.clone();
        }
        return null;
    }
    public List<String> getKeys()
    {
        Set<String> keys = this.trash_Barrel.getKeys();
        List<String> outKeys = new ArrayList<>();
        outKeys.addAll(keys);
        return outKeys;
    }
    public void remove(Location location)
    {
        Block block = location.getBlock();
        String key = getKey(location);
        this.trash_Barrel.delete(key);

    }
    public void setPower(Location location, Boolean power)
    {
        String key = getKey(location);
        setPower(key, power);
    }
    public void setPower(String key, Boolean power)
    {

        this.trash_Barrel.set(key + ".power", power);

    }
    public boolean isPowered(Location location)
    {
        String key = getKey(location);
        return isPowered(key);
    }
    public boolean isPowered(String key)
    {
        return this.trash_Barrel.getBoolean(key + ".power");
    }
    public void add(Location location)
    {
        String key = getKey(location);
        this.trash_Barrel.set(key + ".location", location.clone());
        this.trash_Barrel.set(key + ".power", true);

    }
    public String getKey(Location location)
    {
        String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(location.clone());
        return serializeLocation;
    }
    public Boolean isTrashBarrel(Location location)
    {
        boolean locationsEqual = false;
        String key = getKey(location);
        if (this.trash_Barrel.contains(key)) {
            Location hoppersLocation = this.trash_Barrel.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(hoppersLocation, location);
        }
        return locationsEqual;
    }
}
