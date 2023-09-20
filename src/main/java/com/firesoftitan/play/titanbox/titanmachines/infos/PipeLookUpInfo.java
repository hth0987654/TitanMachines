package com.firesoftitan.play.titanbox.titanmachines.infos;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PipeLookUpInfo {
    private static String getLookupKey(PipeChestFilterTypeEnum pipeChestFilterTypeEnum, ItemStack itemStack) {
        String lookup_key = itemStack.getType().name();
        if (pipeChestFilterTypeEnum == PipeChestFilterTypeEnum.TOTAL_MATCH)
            lookup_key = lookup_key + ":" + TitanMachines.nbtTool.getNBTString(itemStack);
        return lookup_key;
    }

    private final UUID uuid;
    private final HashMap<String, SaveManager> locations = new HashMap<String, SaveManager>();
    public PipeLookUpInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public void addItem(PipeChestFilterTypeEnum pipeChestFilterTypeEnum, Location location, int slot)
    {
        addItem(pipeChestFilterTypeEnum, null, location, slot);
    }
    public void addItem(PipeChestFilterTypeEnum pipeChestFilterTypeEnum, ItemStack itemStack, Location location, int slot)
    {
        String lookup_key = "all";
        if (itemStack != null) lookup_key = getLookupKey(pipeChestFilterTypeEnum, itemStack);
        String key = TitanMachines.serializeTool.serializeLocation(location);
        if (!locations.containsKey(lookup_key)) {
            SaveManager saveManager = new SaveManager();
            //if (itemStack != null) saveManager.set("item", itemStack.clone());
            locations.put(lookup_key, saveManager);
        }
        SaveManager saveManager = locations.get(lookup_key);
        saveManager.set(key  + "." + slot, location);
        locations.put(lookup_key ,saveManager);
    }
    public List<Location> getItemLocation(PipeChestFilterTypeEnum pipeChestFilterTypeEnum, ItemStack itemStack)
    {
        List<Location> locationList = new ArrayList<Location>();
        String lookup_key = "all";
        if (pipeChestFilterTypeEnum != PipeChestFilterTypeEnum.ALL) lookup_key = getLookupKey(pipeChestFilterTypeEnum, itemStack);
        if (locations.containsKey(lookup_key)) {
            SaveManager saveManager = locations.get(lookup_key);
            for (String key: saveManager.getKeys())
            {
                Location location = TitanMachines.serializeTool.deserializeLocation(key);
                locationList.add(location);
            }

        }
        return locationList;
    }
    public List<Integer> getItemSlot(PipeChestFilterTypeEnum pipeChestFilterTypeEnum, ItemStack itemStack, Location location)
    {
        List<Integer> locationList = new ArrayList<Integer>();
        String lookup_key = "all";
        if (pipeChestFilterTypeEnum != PipeChestFilterTypeEnum.ALL) lookup_key = getLookupKey(pipeChestFilterTypeEnum, itemStack);
        if (locations.containsKey(lookup_key)) {
            SaveManager saveManager = locations.get(lookup_key);
            String key = TitanMachines.serializeTool.serializeLocation(location);
            for (String slot: saveManager.getKeys(key))
            {
                locationList.add(Integer.parseInt(slot));
            }

        }
        return locationList;
    }

    public UUID getUuid() {
        return uuid;
    }
}
