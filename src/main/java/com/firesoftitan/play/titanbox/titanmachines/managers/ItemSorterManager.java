package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.tools.LibsItemStackTool;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemSorterManager {
    public static ItemSorterManager instance;

    public static boolean isSortingContainer(Location location)
    {
        if (SensibleToolboxSupport.instance.isSupported(location)) return true;
        Block block = location.getBlock();
        Material type = block.getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL;
    }


    private final SaveManager itemSorters = new SaveManager(TitanMachines.instants.getName(), "item_sorter");
    public ItemSorterManager() {
        instance = this;
    };
    public Location getSelector(Player player)
    {
        return this.itemSorters.getLocation("players." + player.getUniqueId() + ".location");
    }
    public void setSelector(Player player, Location location)
    {
        this.itemSorters.set("players." + player.getUniqueId() + ".location", location);
    }
    public int getSettingsSortingType(Location sorter, Location location)
    {

        String key = this.getKey(sorter);
        return getSettingsSortingType(key, location);
    }
    public int getSettingsSortingType(Player player, Location location)
    {
        String key = this.getKey(this.getSelector(player));
        return getSettingsSortingType(key, location);
    }
    public int getSettingsSortingType(String key, Location location)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        return this.itemSorters.getInt(key + ".settings." + locationKey + ".type");
    }

    public List<Location> getSortingContainers(Player player)
    {
        List<Location> locations = new ArrayList<>();
        String key = this.getKey(getSelector(player));
        Set<String> keys = this.itemSorters.getKeys(key + ".settings" );
        for(String locationKey: keys)
        {
            Location location1 = TitanMachines.tools.getSerializeTool().deserializeLocation(locationKey);
            locations.add( location1.clone());
        }
        return locations;

    }
    public static Location getFixContainerLocation(Location location)
    {
        if (location.getBlock().getState() instanceof Container sortedChest) {
            if (sortedChest.getInventory().getLocation() != null) return sortedChest.getInventory().getLocation().clone();
        }
        return location.clone();
    }
    public BlockFace getSettingsSortingFacing(Player player, Location location)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String key = this.getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        String facing = this.itemSorters.getString(key + ".settings." + locationKey + ".facing");
        if (facing == null || facing.isEmpty()) return BlockFace.NORTH;
        return BlockFace.valueOf(facing);
    }
    public Boolean getSettingsSortingLock(Player player, Location location)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String key = this.getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        return this.itemSorters.getBoolean(key + ".settings." + locationKey + ".lock");
    }
    public void setSettingsSortingLock(Player player, Location location)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String key = this.getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        this.itemSorters.set(key + ".settings." + locationKey + ".lock", true);

    }
    public void setSettingsSortingFacing(Player player, Location location, BlockFace blockFace)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String key = this.getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        this.itemSorters.set(key + ".settings." + locationKey + ".facing", blockFace.name());

    }
    public void setSettingsSortingType(Player player, Location location, int type)
    {
        location = ItemSorterManager.getFixContainerLocation(location);
        String key = this.getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        this.itemSorters.set(key + ".settings." + locationKey + ".type", type);

    }
    public void save()
    {
        itemSorters.save();
    }
    public void add(Player player, Location location)
    {
        String key = this.getKey(location);
        this.itemSorters.set(key + ".location", location);
        this.itemSorters.set("players." + player.getUniqueId() + ".location", location);
        return; //this.scanChunk(location);
    }
    
    public Set<String> getKeys()
    {
        return itemSorters.getKeys();
    }
    public Location getLocation(String key)
    {
        return this.itemSorters.getLocation(key + ".location");
    }
    public Location getLocation(Location location)
    {
        String key = getKey(location);
        return getLocation(key);
    }
    public String getKey(Location location)
    {
        Chunk chunk = location.getChunk();
        World world = location.getWorld();
        Location key = new Location(world, chunk.getX(), 0, chunk.getZ());
        return TitanMachines.tools.getSerializeTool().serializeLocation(key);
    }
    public Boolean hasSorter(Player player)
    {
        return itemSorters.contains("players." + player.getUniqueId());
    }
    public Boolean isSorter(Location location)
    {
        if (location == null) return false;
        boolean locationsEqual = false;
        String key = getKey(location);
        if (this.itemSorters.contains(key)) {
            Location itemSortersLocation = this.itemSorters.getLocation(key + ".location");
            locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(itemSortersLocation, location);
        }
        return locationsEqual;
    }
    public UUID getOwner(Location hopper)
    {

        for(String key: itemSorters.getKeys("players"))
        {
            Location location = this.itemSorters.getLocation("players." + key + ".location");
            if (TitanMachines.tools.getLocationTool().isLocationsEqual(location, hopper)) return UUID.fromString(key);
        }
        return null;
    }
    public boolean isOwner(Player player, Location location)
    {
        if (location == null) return false;
        if (isSorter(location))
        {
            Location selector = getSelector(player);
            if (selector == null) return false;
            return TitanMachines.tools.getLocationTool().isLocationsEqual(location, selector);
        }
        return false;
    }

    public void removeSorter(Player player, Location location)
    {
        String keySorter = getKey(location);

        for(String key: this.itemSorters.getKeys(keySorter + ".chest"))
        {
            Location location1 = TitanMachines.tools.getSerializeTool().deserializeLocation(key);
            ContainerVisualManager.removeManager(location1);
        }
        this.itemSorters.delete(keySorter);
        this.itemSorters.delete("players." + player.getUniqueId());
    }
    public void removeContainer(Player player, Location chestLocation)
    {
        chestLocation = ItemSorterManager.getFixContainerLocation(chestLocation);
        String keySorter = getKey(getSelector(player));

        ItemStack clone = this.getSortingItem(player, chestLocation);
        String itemKey = TitanMachines.tools.getSerializeTool().serializeItemStack(clone);
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(chestLocation);
        this.itemSorters.delete(keySorter + ".chest." + locationKey);
        this.itemSorters.delete(keySorter + ".settings." + locationKey);
        List<Location> sortingContainer = getSortingContainer(player, clone);
        List<Location> sortingContainer2 = getSortingContainer(player, clone.getType());
        for (int i = 0; i < sortingContainer.size(); i++)
        {
            if (TitanMachines.locationTool.isLocationsEqual(sortingContainer.get(i), chestLocation))
            {
                sortingContainer.remove(i);
                this.itemSorters.set(keySorter + ".items." + itemKey, sortingContainer);
                break;
            }
        }
        for (int i = 0; i < sortingContainer2.size(); i++)
        {
            if (TitanMachines.locationTool.isLocationsEqual(sortingContainer2.get(i), chestLocation))
            {
                sortingContainer2.remove(i);
                this.itemSorters.set(keySorter + ".materials." + clone.getType().getKey(), sortingContainer2);
                break;
            }
        }
        if (sortingContainer.isEmpty())
        {
            this.itemSorters.delete(keySorter + ".items." + itemKey);
        }
        if (sortingContainer2.isEmpty())
        {
            this.itemSorters.delete(keySorter + ".materials." + clone.getType().getKey());
        }

    }
    public ItemStack getSortingItem(Player player, Location chestLocation)
    {
        chestLocation = ItemSorterManager.getFixContainerLocation(chestLocation);
        String keySorter = getKey(this.getSelector(player));
        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(chestLocation);
        ItemStack item = this.itemSorters.getItem(keySorter + ".chest." + locationKey);
        if (TitanMachines.itemStackTool.isEmpty(item)) return null;
        return item.clone();
    }
    public List<Location> getSortingContainer(Player player, Material material) {
        return getSortingContainer(getKey(getSelector(player)), material);
    }
    public List<Location> getSortingContainer(Location location, Material material) {
        return getSortingContainer(getKey(location), material);
    }
    public List<Location> getSortingContainer(String keySorter, Material material)
    {

        List<Location> outCheck = new ArrayList<Location>();
        List<Location> chestLocations = this.itemSorters.getLocationList(keySorter + ".materials." + material.getKey());
        for(Location sortLocation: chestLocations) {
            int settingsSortingType = ItemSorterManager.instance.getSettingsSortingType(keySorter, sortLocation);
            if (settingsSortingType == 2) outCheck.add(sortLocation.clone());
        }
        return outCheck;
    }
    public List<Location> getSortingContainer(Player player, ItemStack itemStack)
    {
        String keySorter = getKey(getSelector(player));
        return getSortingContainer(keySorter, itemStack);
    }
    public List<Location> getSortingContainer(Location location, ItemStack itemStack)
    {
        String keySorter = getKey(location);
        return getSortingContainer(keySorter, itemStack);
    }
    public List<Location> getSortingContainer(String keySorter, ItemStack itemStack)
    {
        List<Location> outCheck = new ArrayList<Location>();
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        String itemKey = TitanMachines.tools.getSerializeTool().serializeItemStack(clone, true);
        List<Location> chestLocations = this.itemSorters.getLocationList(keySorter + ".items." + itemKey);
        for(Location sortLocation: chestLocations) {
                int settingsSortingType = ItemSorterManager.instance.getSettingsSortingType(keySorter, sortLocation);
                if (settingsSortingType == 1) outCheck.add(sortLocation.clone());
        }
        return outCheck;
    }
    public void setSortingItem(Player player, Location chestLocation, ItemStack itemStack)
    {
        chestLocation = ItemSorterManager.getFixContainerLocation(chestLocation);
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        String keySorter = getKey(this.getSelector(player));

        String locationKey = TitanMachines.tools.getSerializeTool().serializeLocation(chestLocation);
        String itemKey = TitanMachines.tools.getSerializeTool().serializeItemStack(clone, true);
        this.itemSorters.set(keySorter + ".chest." + locationKey, itemStack);
        this.itemSorters.set(keySorter + ".settings." + locationKey + ".type", 1);
        this.itemSorters.set(keySorter + ".settings." + locationKey + ".facing", BlockFace.NORTH.name());

        List<Location> locations = getSortingContainer(player, itemStack);
        locations.add(chestLocation.clone());
        this.itemSorters.set(keySorter + ".items." + itemKey, locations);

        locations = getSortingContainer(player, itemStack.getType());
        locations.add(chestLocation.clone());
        this.itemSorters.set(keySorter + ".materials." + itemStack.getType().getKey(), locations);

    }
    public ItemStack addChest(Player player, Location location, ItemStack outputItem) {
        if (!TitanMachines.tools.getItemStackTool().isEmpty(outputItem)) {
            setSortingItem(player, location, outputItem);
            return outputItem;
        }
        return null;
    }
    public ItemStack addChest(Player player, Location location, Inventory inventory) {
        LibsItemStackTool itemStackTool = TitanMachines.tools.getItemStackTool();
        ItemStack[] contents = inventory.getContents();
        for (ItemStack sortingItem: contents) {
            if (!itemStackTool.isEmpty(sortingItem)) {
                setSortingItem(player, location, sortingItem);
                return sortingItem.clone();
            }
        }
        return null;
    }
}
