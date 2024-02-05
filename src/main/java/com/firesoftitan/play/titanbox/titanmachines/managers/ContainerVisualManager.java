package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.enums.ArmorStandPoseEnum;
import com.firesoftitan.play.titanbox.libs.managers.HologramManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String keyU = TitanMachines.tools.getSerializeTool().serializeLocation(container);
        uuidVisualHashMap.put(keyU, this);
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
        Location check = container.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
        Location fixedCheck = ItemSorterManager.getFixContainerLocation(check);
        Location fixedClone = ItemSorterManager.getFixContainerLocation(clone);

        float yaw = yawAngleMap.get(blockFace);
        Location displayLoc = getOffset(blockFace, clone);
        //Check to see if it's a double-chest
        if (TitanMachines.locationTool.isLocationsEqual(fixedCheck, fixedClone))
        {
            displayLoc.add(blockFace.getModX() , blockFace.getModY() , blockFace.getModZ() );
        }
        if (hologramManager == null) {
            hologramManager = TitanMachines.hologramTool.addHologram(displayLoc.clone().add(0,200, 0));
        }
        if (blockFace != BlockFace.DOWN && blockFace != BlockFace.UP) hologramManager.setEquipmentAngles(ArmorStandPoseEnum.RIGHT_ARM, new EulerAngle(Math.PI/2, 0, Math.PI));
        else hologramManager.setEquipmentAngles(ArmorStandPoseEnum.RIGHT_ARM, new EulerAngle(0, 0, 0));
        displayLoc.setPitch(0);
        displayLoc.setYaw(yaw);
        hologramManager.setLocation(displayLoc);
        hologramManager.setEquipment(EquipmentSlot.HAND, this.itemStack);

    }
    private Location getOffset(BlockFace blockFace, Location location)
    {
        Location offsetLocation = location.clone();

        return switch (blockFace) {
            case NORTH -> offsetLocation.add(0.7f, -1.15f, 0.4f);//Move Left, Right is X, Forward, Back is Z
            case SOUTH -> offsetLocation.add(0.25f, -1.15f, 0.6f);//Move Left, Right is X, Forward, Back is Z
            case EAST -> offsetLocation.add(0.6f, -1.15f, 0.7f);//Move Left, Right is Z, Forward, Back is X
            case WEST -> offsetLocation.add(0.4f, -1.15f, 0.25f);//Move Left, Right is Z, Forward, Back is X
            case UP -> offsetLocation.add(0.85f, 0.4f, 0.25f);
            case DOWN -> offsetLocation.add(0.85f, -0.95f, 0.25f);
            default -> offsetLocation;
        };
    }

    private final Map<BlockFace, Float> yawAngleMap = Map.of(
            BlockFace.NORTH, 0f,
            BlockFace.SOUTH, 180f,
            BlockFace.EAST, 90f,
            BlockFace.WEST, -90f,
            BlockFace.UP, 0f,
            BlockFace.DOWN, 0f
    );
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
