package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.Set;

public class AreaHopperManager {
    private static final SaveManager areaHopper = new SaveManager(TitanMachines.instants.getName(), "area_hoppers");

    public static String getKey(Location location)
    {
        return TitanMachines.tools.getSerializeTool().serializeLocation(location);
    }
    public static String getHopperKey(Location location)
    {
        Set<String> keys = getKeys();
        for(String key: keys)
        {
            Location locationA = getLocation(key);
            if (locationA != null && locationA.getWorld() != null) {
                if (locationA.getWorld().getName().equals(location.getWorld().getName())) {
                    if (locationA.distance(location) < 7) {
                        return getKey(locationA.clone());
                    }
                }
            }
        }
        return null;
    }
    public static Boolean isHopper(Location location)
    {
        boolean locationsEqual = false;
        String key = getKey(location);
        if (AreaHopperManager.areaHopper.contains(key)) {
            Location hoppersLocation = AreaHopperManager.areaHopper.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(hoppersLocation, location);
        }
        return locationsEqual;
    }
    public static void setLocation(String key, Location location)
    {
        areaHopper.set(key + ".location", location);
    }
    public static Location getLocation(String key)
    {
        return areaHopper.getLocation(key + ".location");
    }
    public static void delete(String key)
    {
        areaHopper.delete(key);
    }
    public static void save()
    {
        areaHopper.save();
    }

    public static Set<String> getKeys() {
        return areaHopper.getKeys();
    }
}
