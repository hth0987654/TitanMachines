package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;

public class ChunkHopperManager {
    private static final SaveManager chunkHoppers = new SaveManager(TitanMachines.instants.getName(), "chunk_hoppers");

    public static String getKey(Location location)
    {
        Chunk chunk = location.getChunk();
        World world = location.getWorld();
        int normalizedY = location.getBlockY() - (location.getBlockY() % 5);
        Location key = new Location(world, chunk.getX(), 0, chunk.getZ());
        return TitanMachines.tools.getSerializeTool().serializeLocation(key);
    }
    public static Boolean hasHopper(Location location)
    {
        String key = getKey(location);
        return ChunkHopperManager.chunkHoppers.contains(key);
    }
    public static Boolean isHopper(Location location)
    {
        boolean locationsEqual = false;
        String key = getKey(location);
        if (ChunkHopperManager.chunkHoppers.contains(key)) {
            Location hoppersLocation = ChunkHopperManager.chunkHoppers.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(hoppersLocation, location);
        }
        return locationsEqual;
    }
    public static void setLocation(String key, Location location)
    {
        chunkHoppers.set(key + ".location", location);
    }
    public static Location getLocation(String key)
    {
        return chunkHoppers.getLocation(key + ".location");
    }
    public static void delete(String key)
    {
        chunkHoppers.delete(key);
    }
    public static void save()
    {
        chunkHoppers.save();
    }

    public static Set<String> getKeys() {
        return chunkHoppers.getKeys();
    }
}
