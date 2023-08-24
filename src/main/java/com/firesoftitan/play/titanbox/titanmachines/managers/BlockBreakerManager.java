package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockBreakerManager {
    private SaveManager block_breaker = new SaveManager(TitanMachines.instants.getName(), "block_breaker");
    public static BlockBreakerManager instance;

    public BlockBreakerManager() {

        BlockBreakerManager.instance = this;
    }
    public void save()
    {
        block_breaker.save();
    }
    public Location getLocation(String key)
    {
        if (this.block_breaker.contains(key)) {
            Location hoppersLocation = this.block_breaker.getLocation(key + ".location");
            return hoppersLocation.clone();
        }
        return null;
    }
    public List<String> getKeys()
    {
        Set<String> keys = this.block_breaker.getKeys();
        List<String> outKeys = new ArrayList<>();
        outKeys.addAll(keys);
        return outKeys;
    }
    public void remove(Location location)
    {
        Block block = location.getBlock();
        String key = getKey(location);
        this.block_breaker.delete(key);

    }
    public void setPower(Location location, Boolean power)
    {
        String key = getKey(location);
        setPower(key, power);
    }
    public void setPower(String key, Boolean power)
    {

        this.block_breaker.set(key + ".power", power);

    }
    public boolean isPowered(Location location)
    {
        String key = getKey(location);
        return isPowered(key);
    }
    public boolean isPowered(String key)
    {
        return this.block_breaker.getBoolean(key + ".power");
    }
    public void add(Location location)
    {
        String key = getKey(location);
        this.block_breaker.set(key + ".location", location.clone());
        this.block_breaker.set(key + ".power", true);

    }
    public String getKey(Location location)
    {
        String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(location.clone());
        return serializeLocation;
    }
    public Boolean isBreaker(Location location)
    {
        boolean locationsEqual = false;
        String key = getKey(location);
        if (this.block_breaker.contains(key)) {
            Location hoppersLocation = this.block_breaker.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(hoppersLocation, location);
        }
        return locationsEqual;
    }
}
