package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LumberjackManager {
    private SaveManager lumberjack = new SaveManager(TitanMachines.instants.getName(), "lumberjack");
    public static LumberjackManager instance;

    public LumberjackManager() {

        LumberjackManager.instance = this;
    }
    public void save()
    {
        lumberjack.save();
    }
    public Location getLocation(String key)
    {
        if (this.lumberjack.contains(key)) {
            Location hoppersLocation = this.lumberjack.getLocation(key + ".location");
            return hoppersLocation.clone();
        }
        return null;
    }
    public List<String> getKeys()
    {
        Set<String> keys = this.lumberjack.getKeys();
        List<String> outKeys = new ArrayList<>();
        outKeys.addAll(keys);
        return outKeys;
    }
    public void remove(Location location)
    {
        Block block = location.getBlock();
        String key = getKey(location);
        this.lumberjack.delete(key);

    }
    public void setPower(Location location, Boolean power)
    {
        String key = getKey(location);
        setPower(key, power);
    }
    public void setPower(String key, Boolean power)
    {

        this.lumberjack.set(key + ".power", power);

    }
    public boolean removeSapling(Location location, Material material)
    {
        String key = getKey(location);
        int amount = 0;
        if (this.lumberjack.contains(key + ".sapling.material"))
        {
            if (this.lumberjack.getString(key + ".sapling.material").equals(material.name()))
            {
                amount = this.lumberjack.getInt(key + ".sapling.count");
                if (amount > 0) {
                    this.lumberjack.set(key + ".sapling.material", material.name());
                    this.lumberjack.set(key + ".sapling.count", amount - 1);
                    return true;
                }
            }
        }
        return false;
    }
    public Material getSaplingMaterial(Location location)
    {
        String key = getKey(location);
        String string = this.lumberjack.getString(key + ".sapling.material");
        Material material = Material.getMaterial(string);
        return material;
    }
    public int getSaplingCount(Location location)
    {
        String key = getKey(location);
        return this.lumberjack.getInt(key + ".sapling.count");
    }

    public void addSapling(Location location, Material material)
    {
        addSapling(location, material, 1);
    }
    public void addSapling(Location location, Material material, int count)
    {
        String key = getKey(location);
        int amount = 0;
        if (this.lumberjack.contains(key + ".sapling.material"))
        {
            if (this.lumberjack.getString(key + ".sapling.material").equals(material.name()))
            {
                amount = this.lumberjack.getInt(key + ".sapling.count");
            }
        }
        this.lumberjack.set(key + ".sapling.material", material.name());
        this.lumberjack.set(key + ".sapling.count", amount + count);
    }
    public boolean isPowered(Location location)
    {
        String key = getKey(location);
        return isPowered(key);
    }
    public boolean isPowered(String key)
    {
        return this.lumberjack.getBoolean(key + ".power");
    }
    public void add(Location location)
    {
        String key = getKey(location);
        this.lumberjack.set(key + ".location", location.clone());
        this.lumberjack.set(key + ".power", true);

    }
    public String getKey(Location location)
    {
        String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        return serializeLocation;
    }
    public Boolean isLumberjack(Location location)
    {
        boolean locationsEqual = false;
        String key = getKey(location);
        if (this.lumberjack.contains(key)) {
            Location hoppersLocation = this.lumberjack.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(hoppersLocation, location);
        }
        return locationsEqual;
    }
}
