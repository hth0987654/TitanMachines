package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.HologramManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ContainerVisualManager {
    private static final HashMap<String, List<ContainerVisualManager>> listVisualHashMap = new HashMap<String, List<ContainerVisualManager>>();
    private static final HashMap<String, ContainerVisualManager> uuidVisualHashMap = new HashMap<String, ContainerVisualManager>();
    public static ContainerVisualManager getManager(Location location)
    {
        String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        return uuidVisualHashMap.get(serializeLocation);
    }
    public static void removeManager(Location location)
    {
        String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(location);
        if (uuidVisualHashMap.containsKey(serializeLocation)) {
            uuidVisualHashMap.get(serializeLocation).remove();
            uuidVisualHashMap.remove(serializeLocation);
        }
    }
    public static void removeChunk(String key)
    {
        List<ContainerVisualManager> visualManagers = listVisualHashMap.get(key);
        if (visualManagers != null) {
            for (ContainerVisualManager visualManager : visualManagers) {
                String serializeLocation = TitanMachines.tools.getSerializeTool().serializeLocation(visualManager.getContainer());
                uuidVisualHashMap.remove(serializeLocation);
                visualManager.remove();
            }
            listVisualHashMap.remove(key);
        }
    }

    public static String getKey(Location location)
    {
        Chunk chunk = location.getChunk();
        World world = location.getWorld();
        Location key = new Location(world, chunk.getX(), 0, chunk.getZ());
        return TitanMachines.tools.getSerializeTool().serializeLocation(key);
    }
    private final Location container;
    private HologramManager hologramManager;
    private final ItemStack itemStack;
    private BlockFace blockFace;
    public ContainerVisualManager(Location container, ItemStack itemStack, BlockFace blockFace) {
        this.container = container;
        this.itemStack = itemStack.clone();
        this.blockFace = blockFace;
        this.rescan();
        String key = ContainerVisualManager.getKey(container);
        List<ContainerVisualManager> visualManagers = listVisualHashMap.get(key);
        if (visualManagers == null) visualManagers = new ArrayList<ContainerVisualManager>();
        visualManagers.add(this);
        listVisualHashMap.put(key, visualManagers);

        String keyuu = TitanMachines.tools.getSerializeTool().serializeLocation(container);
        uuidVisualHashMap.put(keyuu, this);
    }
    public void remove()
    {
        try {
            hologramManager.delete();
            hologramManager = null;
        } catch (Exception e) {
            if (hologramManager != null) {
                World world = hologramManager.getLocation().getWorld();
                if (world != null) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getUniqueId().equals(hologramManager.getUUID())) entity.remove();
                    }
                }
            }
        }
    }
    public void rescan() {
        Location clone = container.clone();
        Location check = container.clone().add(blockFace.getModX() * 1, blockFace.getModY() * 1, blockFace.getModZ() * 1);
        float x = blockFace.getModX() * 0.75f + 1;
        float y = blockFace.getModY() * 0.5f - 0.25f;
        float z = blockFace.getModZ() * 0.75f ;
        Location fixContainerLocationA = ItemSorterManager.getFixContainerLocation(check);
        Location fixContainerLocationB = ItemSorterManager.getFixContainerLocation(clone);
        if (fixContainerLocationA != null && fixContainerLocationB != null && TitanMachines.locationTool.isLocationsEqual(fixContainerLocationA, fixContainerLocationB))
        {
            x = blockFace.getModX() * 1.75f + 1;
            y = blockFace.getModY() * 0.5f - 0.25f;
            z = blockFace.getModZ() * 1.75f ;
        }
        Location add = clone.add(x, y, z);
        if (hologramManager == null) {
            hologramManager = TitanMachines.hologramTool.addHologram(add.clone().add(0,200, 0));
        }
        hologramManager.setLocation(add);
        hologramManager.setEquipment(EquipmentSlot.HAND, this.itemStack);
        //if (this.itemStack.hasItemMeta() && this.itemStack.getItemMeta().hasDisplayName()) hologramManager.setText(this.itemStack.getItemMeta().getDisplayName());

    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setBlockFace(BlockFace blockFace) {
        this.blockFace = blockFace;
        rescan();
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public Location getContainer() {
        return container;
    }
}
