package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.HologramManager;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.PipeBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterType;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PipesManager {
    private static final BlockFace[] blockFaces = {BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};

    public static  List<Location> getConnections(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        List<Location> outList = new ArrayList<Location>();
        for(int i = 0; i < 7; i++) {
            Location locationConnection = pipes.getLocation("pipes." + key + ".connection." + i);
            if (locationConnection != null) {
                outList.add(locationConnection);
            }
        }
        return outList;
    }

    private static final SaveManager pipes = new SaveManager(TitanMachines.instants.getName(), "pipes");
    public static void remove(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        UUID id = getGroup(location);
        deleteGroup(id);
        removeConnection(location); //removes chest to
        UUID HGuuid = pipes.getUUID("pipes." + key + ".hologram");
        UUID HGuuid2 = pipes.getUUID("pipes." + key + ".hologram2");
        pipes.delete("pipes." + key);

        if (HGuuid != null) {
            HologramManager hologram = TitanMachines.hologramTool.getHologram(HGuuid);
            TitanMachines.hologramTool.removeHologram(hologram);
        }
        if (HGuuid2 != null) {
            HologramManager hologram = TitanMachines.hologramTool.getHologram(HGuuid2);
            TitanMachines.hologramTool.removeHologram(hologram);
        }

        long start = System.currentTimeMillis();
        List<UUID> taken = new ArrayList<UUID>();
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                UUID group = getGroup(testLocation);
                if (!taken.contains(group)) {
                    UUID uuid = getNewUUID();
                    taken.add(uuid);
                    addToGroup(testLocation, uuid);
                        ArrayList<String> checkList = new ArrayList<>();
                        String keyMine = TitanMachines.serializeTool.serializeLocation(testLocation);
                        checkList.add(keyMine);
                        keyMine = TitanMachines.serializeTool.serializeLocation(location);
                        checkList.add(keyMine);
                    relabelGroup(testLocation, uuid, id, checkList);
                }
            }
        }
        cleanSave();
        long passed = System.currentTimeMillis() - start;
        if (passed > 100) TitanMachines.messageTool.sendMessageSystem("pipe break: " + passed);
    }

    private static void cleanSave() {
        for(String keyChest: pipes.getKeys("chest"))
        {
            for(String keyChestGroup: pipes.getKeys("chest." + keyChest + ".settings"))
            {
                try {
                    UUID uuid = UUID.fromString(keyChestGroup);
                    if (!hasGroup(uuid)) pipes.delete("chest." + keyChest + ".settings." + keyChestGroup);
                } catch (Exception e) {
                    pipes.delete("chest." + keyChest + ".settings." + keyChestGroup);
                    System.out.println("Clean Error....");
                }
            }
            if (pipes.contains("chest." + keyChest + ".settings") && pipes.getKeys("chest." + keyChest + ".settings").isEmpty()) pipes.delete("chest." + keyChest + ".settings");
            if (pipes.contains("chest." + keyChest + ".groups") && pipes.getKeys("chest." + keyChest + ".groups").isEmpty()) pipes.delete("chest." + keyChest + ".groups");
            if (pipes.contains("chest." + keyChest ) && pipes.getKeys("chest." + keyChest).isEmpty()) pipes.delete("chest." + keyChest);
        }
        for(String keyChest: pipes.getKeys("groups"))
        {
            if (!pipes.contains("groups." + keyChest + ".pipes") && pipes.getKeys("groups." + keyChest + ".chest").isEmpty()) pipes.delete("groups." + keyChest);
            if (pipes.getKeys("groups." + keyChest + ".pipes").isEmpty() && pipes.getKeys("groups." + keyChest + ".chest").isEmpty()) pipes.delete("groups." + keyChest);
        }
    }

    private static void relabelGroup(Location location, UUID newGroup, UUID oldGroup, List<String> checkList)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);

        for(int i = 0; i < 7; i++) {
            Location locationConnection = pipes.getLocation("pipes." + key + ".connection." + i);
            if (locationConnection != null) {
                if (isPipe(locationConnection)) {
                    UUID checkGroup = getGroup(locationConnection);
                    if (checkGroup == null || !checkGroup.equals(newGroup)) {
                        String keyMine = TitanMachines.serializeTool.serializeLocation(locationConnection);
                        if (!checkList.contains(keyMine)) {
                            checkList.add(keyMine);
                            addToGroup(locationConnection, newGroup);
                            relabelGroup(locationConnection, newGroup, oldGroup, checkList);
                        }

                    }
                }
                if (isChestConnected(locationConnection))
                {
                    PipeChestType chestSettingsType = PipesManager.getChestSettingsType(locationConnection, oldGroup);
                    List<Integer> chestSettingsFilterAccessSlots = PipesManager.getChestSettingsFilterAccessSlots(locationConnection, oldGroup);
                    HashMap<Integer, ItemStack > itemStack = new HashMap<Integer, ItemStack>();
                    HashMap<Integer, PipeChestFilterType > chestSettingsFilterType= new HashMap<Integer, PipeChestFilterType>();
                    for(int k: chestSettingsFilterAccessSlots) {
                    //for(int k =0; k < 2; k++) {
                        itemStack.put(k,PipesManager.getChestSettingsFilter(locationConnection, oldGroup, k));
                        chestSettingsFilterType.put(k, PipesManager.getChestSettingsFilterType(locationConnection, oldGroup, k));
                    }

                    removeChest(locationConnection);
                    scanPlacedChest(locationConnection);

                    PipesManager.setChestSettingsType(locationConnection, newGroup, chestSettingsType);
                    for(int k: chestSettingsFilterAccessSlots) {
                    //for(int k =0; k < 2; k++) {
                        PipesManager.setChestSettingsFilter(locationConnection, newGroup, k, itemStack.get(k));
                        PipesManager.setChestSettingsFilterType(locationConnection, newGroup, k, chestSettingsFilterType.get(k));
                    }

                }
            }
        }
    }

    public static void add(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        pipes.set("pipes." + key + ".location", location);

        UUID id = null;
        List<BlockFace> hologramConnections = new ArrayList<BlockFace>();
        for(BlockFace blockFace: blockFaces)
        {
            Location locationToCheck = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(locationToCheck))
            {
                hologramConnections.add(blockFace);
                if (id == null) {
                    //add pipe to an existing group
                    id = getGroup(locationToCheck);
                    addToGroup(location, id);
                }
                else
                {
                    //if two groups meet, merge them to groupA
                    if (!getGroup(locationToCheck).equals(id)) {
                        mergeGroup(id, getGroup(locationToCheck));
                    }
                }
                addConnection(location, locationToCheck);
                addConnection(locationToCheck, location);
            }
        }
        if (getGroup(location) == null)
        {
            id = getNewUUID();
            addToGroup(location, id);
        }
        List<BlockFace> blockFacesChest = scanForChest(location);
        hologramConnections.addAll(blockFacesChest);
        addHologram(location, hologramConnections);

    }
    @SuppressWarnings("unused")
    public static void checkHologram(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        UUID uuid = pipes.getUUID("pipes." + key + ".hologram");
        UUID uuid2 = pipes.getUUID("pipes." + key + ".hologram2");
        HologramManager hologram = TitanMachines.hologramTool.getHologram(uuid);
        if (hologram == null || hologram.getArmorStand() == null || hologram.getArmorStand().isDead())
        {
            if (uuid != null) {
                hologram = TitanMachines.hologramTool.getHologram(uuid);
                TitanMachines.hologramTool.removeHologram(hologram);
            }
            if (uuid2 != null) {
                hologram = TitanMachines.hologramTool.getHologram(uuid2);
                TitanMachines.hologramTool.removeHologram(hologram);
            }

            List<BlockFace> blockFacesChest = scanForChest(location);
            addHologram(location, blockFacesChest);
        }
    }
    private static void addHologram(Location location, List<BlockFace> hologramConnections) {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        int modelNumber = getModelNumber(hologramConnections);

        HologramManager hologramManager = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
        hologramManager.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(modelNumber));
        pipes.set("pipes." + key + ".hologram", hologramManager.getUUID());

        if (modelNumber != 30300 && hologramConnections.contains(BlockFace.UP) && hologramConnections.contains(BlockFace.DOWN))
        {
            HologramManager hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30400));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }else if (modelNumber != 30300 && hologramConnections.contains(BlockFace.UP))
        {
            HologramManager hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30100));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }else if (modelNumber != 30300 && hologramConnections.contains(BlockFace.DOWN))
        {
            HologramManager hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30200));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }

    }

/*    public static void rescanAllPipes()
    {
        int i = 0;
        System.out.println("Starting: Upgrading all pipes to TitanPipes");
        for(String key: pipes.getKeys("pipes"))
        {
            Location location = pipes.getLocation("pipes." + key + ".location");
            if (location != null)
            {
                i++;
                checkForOldVersion(location);
            }
        }
        System.out.println("Done: Upgrading all pipes to TitanPipes");
        System.out.println("Found: " + i);
    }
    private static void checkForOldVersion(Location location)
    {
        if (location != null && PipesManager.isPipe(location))
        {
            TitanBlock block = TitanBlock.getBlock(location);
            if (block == null)
            {
                PipeBlock titanBlock = new PipeBlock(PipeBlock.titanID, TitanMachines.instants.getPipe(), location);
                TitanBlockManager.setTitanBlock(titanBlock, location);
                titanBlock.setup();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        location.getBlock().setType(Material.BARRIER);
                    }
                }.runTaskLater(TitanMachines.instants, 2);
            }
        }
    }*/
    public static void checkSurroundings(Location location)
    {
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                rescanPipeOrientation(testLocation);
            }
        }
    }
    private static List<BlockFace> scanChestOrientation(Location location) {
        List<BlockFace> hologramConnections = new ArrayList<BlockFace>();
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (ContainerManager.isContainer(testLocation))
            {
                UUID group = getGroup(location);
                PipeChestType chestSettingsType = PipesManager.getChestSettingsType(testLocation, group);
                if (chestSettingsType != PipeChestType.NOT_CONNECTED) hologramConnections.add(blockFace);
            }
        }
        return hologramConnections;
    }

    public static void rescanPipeOrientation(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        UUID uuid = pipes.getUUID("pipes." + key + ".hologram");
        UUID uuid2 = pipes.getUUID("pipes." + key + ".hologram2");
        List<BlockFace> hologramConnections = new ArrayList<BlockFace>();
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                hologramConnections.add(blockFace);
            }
        }
        List<BlockFace> blockFacesChest = scanChestOrientation(location);
        hologramConnections.addAll(blockFacesChest);
        int modelNumber = getModelNumber(hologramConnections);
        HologramManager hologramManager = null;

        if (uuid != null) hologramManager= TitanMachines.hologramTool.getHologram(uuid);
        if (hologramManager == null || hologramManager.getArmorStand() == null || hologramManager.getArmorStand().isDead()) hologramManager = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
        if (hologramManager != null)
        {
            hologramManager.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(modelNumber));
            pipes.set("pipes." + key + ".hologram", hologramManager.getUUID());
        }

        if (modelNumber != 30300 && hologramConnections.contains(BlockFace.UP) && hologramConnections.contains(BlockFace.DOWN))
        {
            HologramManager hologramManager2 = null;
            if (uuid2 != null) hologramManager2 = TitanMachines.hologramTool.getHologram(uuid2);
            if (hologramManager2 == null || hologramManager2.getArmorStand() == null || hologramManager2.getArmorStand().isDead()) hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30400));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }else if (modelNumber != 30300 && hologramConnections.contains(BlockFace.UP))
        {
            HologramManager hologramManager2 = null;
            if (uuid2 != null) hologramManager2 = TitanMachines.hologramTool.getHologram(uuid2);
            if (hologramManager2 == null || hologramManager2.getArmorStand() == null || hologramManager2.getArmorStand().isDead()) hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30100));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }else if (modelNumber != 30300 && hologramConnections.contains(BlockFace.DOWN))
        {
            HologramManager hologramManager2 = null;
            if (uuid2 != null) hologramManager2 = TitanMachines.hologramTool.getHologram(uuid2);
            if (hologramManager2 == null || hologramManager2.getArmorStand() == null || hologramManager2.getArmorStand().isDead()) hologramManager2 = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
            hologramManager2.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(30200));
            pipes.set("pipes." + key + ".hologram2", hologramManager2.getUUID());
        }
        else if (uuid2 != null)
        {
            HologramManager hologram = TitanMachines.hologramTool.getHologram(uuid2);
            TitanMachines.hologramTool.removeHologram(hologram);
            pipes.delete("pipes." + key + ".hologram2");
            new BukkitRunnable() {
                @Override
                public void run() {
                    rescanPipeOrientation(location);
                }
            }.runTaskLater(TitanMachines.instants, 2);
        }
    }

    public static int getModelNumber(List<BlockFace> connections)
    {
        int number = 30000;
        if ((connections.contains(BlockFace.UP) && connections.contains(BlockFace.DOWN) && connections.size() == 2)
                || (connections.contains(BlockFace.UP) && connections.size() == 1)
                || (connections.contains(BlockFace.DOWN) && connections.size() == 1)) number = 30300;
        if (connections.contains(BlockFace.EAST)) number = number + 1;
        if (connections.contains(BlockFace.WEST)) number = number + 2;
        if (connections.contains(BlockFace.NORTH)) number = number + 10;
        if (connections.contains(BlockFace.SOUTH)) number = number + 20;
        if (number == 30000) number = 30030;
        if (number == 30010 || number == 30020) number = 30030;
        if (number == 30001 || number == 30002) number = 30003;

        return number;
    }
    public static void scanPlacedChest(Location location) {

        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                addConnection(testLocation, location);
                addChestConnection(testLocation, location);
            }
        }
    }
    private static List<BlockFace> scanForChest(Location location) {
        List<BlockFace> hologramConnections = new ArrayList<BlockFace>();
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (ContainerManager.isContainer(testLocation))
            {
                hologramConnections.add(blockFace);
                addConnection(location, testLocation);
                addChestConnection(location, testLocation);
            }
        }
        return hologramConnections;
    }
    public static List<Integer> getChestSettingsFilterAccessSlots(Location chest, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        Set<String> keys = pipes.getKeys("chest." + key + ".settings." + group + ".filter");
        List<Integer> out = new ArrayList<Integer>();
        for(String s: keys)
        {
            out.add(Integer.parseInt(s));
        }
        return out;
    }
    public static void setChestSettingsFilter(Location chest, UUID group, int slot, ItemStack itemStack)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".filter." + slot + ".item", itemStack);
    }
    public static ItemStack getChestSettingsFilter(Location chest, UUID group, int slot) {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        return pipes.getItem("chest." + key + ".settings." + group + ".filter." + slot + ".item");
    }
    public static void clearChestSettingsFilterType(Location chest, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.delete("chest." + key + ".settings." + group + ".filter");
    }
    public static void setChestSettingsFilterType(Location chest, UUID group, int slot, PipeChestFilterType type)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".filter." + slot + ".type", type.getValue());
    }
    public static PipeChestFilterType getChestSettingsFilterType(Location chest, UUID group, int slot) {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        if (!pipes.contains("chest." + key + ".settings." + group + ".filter." + slot + ".type") && slot > 0) return PipeChestFilterType.DISABLED;
        int anInt = pipes.getInt("chest." + key + ".settings." + group + ".filter." + slot + ".type");
        return PipeChestFilterType.getPipeChestType(anInt);
    }

    public static void setChestSettingsType(Location chest, UUID group, PipeChestType type)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".type", type.getValue());
    }
    public static PipeChestType getChestSettingsType(Location chest, UUID group)
    {
        if (group == null) return PipeChestType.NOT_CONNECTED;
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        if (!pipes.contains("chest." + key + ".settings." + group + ".type")) setChestSettingsType(chest,group, PipeChestType.CHEST_IN);
        int anInt = pipes.getInt("chest." + key + ".settings." + group + ".type");
        return PipeChestType.getPipeChestType(anInt);
    }
    private static void removeConnection(Location pipe)
    {
        String key = TitanMachines.serializeTool.serializeLocation(pipe);
        for(int i = 0; i < 7; i++) {
            if (pipes.contains("pipes." + key + ".connection." + i))
            {
                Location location = pipes.getLocation("pipes." + key + ".connection." + i);
                pipes.delete("pipes." + key + ".connection." + i);
                if (isChestConnected(location))
                {
                    removeChestConnection(location, pipe);
                }
                String keyLocation = TitanMachines.serializeTool.serializeLocation(location);
                for(int j = 0; j < 7; j++) {
                    Location locationCheck = pipes.getLocation("pipes." + keyLocation + ".connection." + j);
                    if (locationCheck != null && pipe != null) {
                        if (TitanMachines.locationTool.isLocationsEqual(locationCheck, pipe)) {
                            pipes.delete("pipes." + keyLocation + ".connection." + j);
                        }
                    }
                }
            }
        }
    }
    private static void removeChestConnection(Location chest, Location pipe)
    {
        UUID uuid = getGroup(pipe);
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        String keyLocation = TitanMachines.serializeTool.serializeLocation(pipe);
        for(int i = 0; i < 7; i++) {
            if (pipes.contains("chest." + key + ".groups." + i)) {
                UUID group = pipes.getUUID("chest." + key + ".groups." + i + ".id");
                if (group.equals(uuid)) {
                    pipes.delete("chest." + key + ".groups." + i);
                    pipes.delete("groups." + group + ".chest." + key);
                    for(int j = 0; j < 7; j++) {
                        Location locationCheck = pipes.getLocation("pipes." + keyLocation + ".connection." + j);
                        if (locationCheck != null) {
                            if (TitanMachines.locationTool.isLocationsEqual(locationCheck, chest)) {
                                pipes.delete("pipes." + keyLocation + ".connection." + j);
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    public static void removeChest(Location chest)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        for(int i = 0; i < 7; i++) {
            if (pipes.contains("chest." + key + ".groups." + i)) {
                UUID group = pipes.getUUID("chest." + key + ".groups." + i + ".id");
                pipes.delete("groups." + group + ".chest." + key);
            }
        }
        for(BlockFace blockFace: blockFaces) {
            Location testLocation = chest.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                String keyLocation = TitanMachines.serializeTool.serializeLocation(testLocation);
                for(int j = 0; j < 7; j++) {
                    Location locationCheck = pipes.getLocation("pipes." + keyLocation + ".connection." + j);
                    if (locationCheck != null) {
                        if (TitanMachines.locationTool.isLocationsEqual(locationCheck, chest)) {
                            pipes.delete("pipes." + keyLocation + ".connection." + j);
                        }
                    }
                }
            }
        }
        //pipes.delete("chest." + key);
    }
    public static boolean isChestConnected(Location chest)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        return pipes.contains("chest." + key);
    }


    public static List<UUID> getChestGroups(Location chest)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        List<UUID> outList = new ArrayList<UUID>();
        for(int i = 0; i < 7; i++) {
            UUID uuid = pipes.getUUID("chest." + key + ".groups." + i + ".id");
            if (uuid != null) {
                outList.add(uuid);
            }
        }
        return outList;
    }
    private static void addChestConnection(Location pipe, Location chest)
    {
        UUID group = getGroup(pipe);
        addChestConnection(pipe, chest, group);
    }
    private static void addChestConnection(Location from, Location chest, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("groups." + group + ".chest." + key + ".location", chest);
        for(int i = 0; i < 7; i++) {
            if (!pipes.contains("chest." + key + ".groups." + i )) {
                pipes.set("chest." + key + ".groups." + i + ".id", group);
                for (int slot: ContainerManager.getInventorySlots(from, chest))
                {
                        if (!pipes.contains("chest." + key + ".settings." + group + ".filter." + slot))
                            setChestSettingsFilterType(chest, group, slot, PipeChestFilterType.ALL);
                }
                return;
            }
        }
    }
    private static void addConnection(Location pipe, Location connection)
    {
        String key = TitanMachines.serializeTool.serializeLocation(pipe);
        for(int i = 0; i < 7; i++) {
            if (!pipes.contains("pipes." + key + ".connection." + i))
            {
                pipes.set("pipes." + key + ".connection." + i, connection);
                return;
            }
        }
    }
    public static List<Location> getOutChestsInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                PipeChestType chestSettingsType = PipesManager.getChestSettingsType(location, group);
                if (chestSettingsType == PipeChestType.CHEST_OUT) locations.add(location);
            }

        }
        return locations;
    }
    public static List<Location> getOverflowInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        List<Location> locationsLast = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                PipeChestType chestSettingsType = PipesManager.getChestSettingsType(location, group);
                if (chestSettingsType == PipeChestType.OVERFLOW) {
                    PipeChestFilterType chestSettingsFilterType = PipesManager.getChestSettingsFilterType(location, group, 0);
                    if (chestSettingsFilterType != PipeChestFilterType.ALL) locations.add(location);
                    else locationsLast.add(location);
                }
            }

        }
        locations.addAll(locationsLast);
        return locations;
    }
    public static List<Location> getInChestsInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        List<Location> locationsLast = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                PipeChestType chestSettingsType = PipesManager.getChestSettingsType(location, group);
                if (chestSettingsType == PipeChestType.CHEST_IN) {
                    PipeChestFilterType chestSettingsFilterType = PipesManager.getChestSettingsFilterType(location, group, 0);
                    if (chestSettingsFilterType != PipeChestFilterType.ALL) locations.add(location);
                    else locationsLast.add(location);
                }
            }

        }
        locations.addAll(locationsLast);
        return locations;
    }
    @SuppressWarnings("unused")
    public static List<Location> getChestsInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }
    public static List<UUID> getGroups()
    {
        List<UUID> output = new ArrayList<UUID>();
        Set<String> groups = pipes.getKeys("groups");
        for(String key: groups)
        {
            UUID uuid = UUID.fromString(key);
            output.add(uuid);
        }
        return output;
    }
    public static Boolean hasGroup(UUID uuid)
    {
        return pipes.contains("groups." + uuid.toString());
    }
    public static UUID getGroup(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        return pipes.getUUID("pipes." + key + ".groupid");
    }
    public static void addToGroup(Location pipe, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(pipe);
        pipes.set("pipes." + key + ".groupid", group);
        pipes.set("groups." + group + ".pipes." + key + ".location", pipe);
    }
    public static int getGroupSize(UUID group)
    {
        Set<String> keys = pipes.getKeys("groups." + group + ".pipes");
        if (keys == null) return 0;
        return keys.size();
    }
    private static void mergeGroup(UUID keepingGroup, UUID removingGroup)
    {
        for(String key: pipes.getKeys("groups." + removingGroup + ".pipes"))
        {
            Location location = pipes.getLocation("pipes." + key + ".location");
            if (location != null) {
                addToGroup(location, keepingGroup);
            }
        }
        for(String key: pipes.getKeys("groups." + removingGroup + ".chest"))
        {
            Location location = pipes.getLocation("groups." + removingGroup + ".chest." + key + ".location");
            pipes.set("groups." + keepingGroup + ".chest." + key + ".location", location);
            for(int i = 0; i < 7; i++) {
                UUID uuid = pipes.getUUID("chest." + key + ".groups." + i + ".id");
                if (uuid != null && !uuid.equals(removingGroup))
                {
                    pipes.set("chest." + key + ".groups." + i + ".id", keepingGroup);
                    //Setting merge\
                    PipeChestType chestSettingsType = PipeChestType.getPipeChestType(pipes.getInt("chest." + key + ".settings." + removingGroup + ".type"));
                    if (!pipes.contains("chest." + key + ".settings." + removingGroup + ".type")) chestSettingsType = PipeChestType.CHEST_IN;
                    pipes.set("chest." + key + ".settings." + keepingGroup + ".type", chestSettingsType.getValue());

                    Set<String> keys = pipes.getKeys("chest." + key + ".settings." + removingGroup + ".filter");
                    List<Integer> chestSettingsFilterAccessSlots = new ArrayList<Integer>();
                    for(String s: keys)
                    {
                        chestSettingsFilterAccessSlots.add(Integer.parseInt(s));
                    }
                    for (int k: chestSettingsFilterAccessSlots) {
                        ItemStack itemStack = pipes.getItem("chest." + key + ".settings." + removingGroup + ".filter." + k + ".item");
                        PipeChestFilterType chestSettingsFilterType = PipeChestFilterType.getPipeChestType(pipes.getInt("chest." + key + ".settings." + removingGroup + ".filter." + k + ".type"));
                        pipes.set("chest." + key + ".settings." + keepingGroup + ".filter." + k + ".item", itemStack);
                        pipes.set("chest." + key + ".settings." + keepingGroup + ".filter." + k + ".type", chestSettingsFilterType.getValue());
                    }
                }
            }

        }
        deleteGroup(removingGroup);
    }
    public static void deleteGroup(UUID group)
    {
        pipes.delete("groups." + group);
    }

    @NotNull
    private static UUID getNewUUID() {
        UUID id = UUID.randomUUID();
        if (pipes.contains("groups." + id )) return getNewUUID();
        return id;
    }
    public static boolean isPipe(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        return pipes.contains("pipes." + key);
    }
    public static void save()
    {
        pipes.save();
    }
}

