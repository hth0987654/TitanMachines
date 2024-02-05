package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.managers.HologramManager;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.infos.PipeLookUpInfo;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PipesManager {
    private static final BlockFace[] blockFaces = {BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
    private static final HashMap<UUID, PipeLookUpInfo> lookups = new HashMap<UUID, PipeLookUpInfo>();
    private static final HashMap<PipeTypeEnum, PipesManager> pipeInstants = new HashMap<PipeTypeEnum, PipesManager>();
    public static PipesManager getInstant(PipeTypeEnum type)
    {
        return pipeInstants.get(type);
    }

    private final SaveManager pipes;
    private final PipeTypeEnum type;
    public PipesManager(PipeTypeEnum type) {
        this.type = type;
        pipeInstants.put(this.type, this);
        String extension  = "_" + this.type.getCaption();
        pipes = new SaveManager(TitanMachines.instants.getName(), "pipes" + extension);
    }

    public PipeTypeEnum getType() {
        return type;
    }

    public List<Location> getConnections(Location location)
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


    public void remove(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        UUID id = getGroup(location);
        deleteGroup(id);
        removeConnection(location); //removes chest
        UUID HGuuid = pipes.getUUID("pipes." + key + ".hologram");
        UUID HGuuid2 = pipes.getUUID("pipes." + key + ".hologram2");
        deletePipe(key);
        if (HGuuid != null) {
            HologramManager hologram = TitanMachines.hologramTool.getHologram(HGuuid);
            TitanMachines.hologramTool.removeHologram(hologram);
        }
        if (HGuuid2 != null) {
            HologramManager hologram = TitanMachines.hologramTool.getHologram(HGuuid2);
            TitanMachines.hologramTool.removeHologram(hologram);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                List<HologramManager> holograms = TitanMachines.hologramTool.getHolograms(location);
                for (HologramManager hologramManager: holograms)
                {
                    hologramManager.delete();
                }
            }
        }.runTaskLater(TitanMachines.instants, 10);

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
    private void deletePipe(String key) {
        if (pipes.contains("pipes." + key)) {
            for (String subKey : pipes.getKeys("pipes." + key)) {
                pipes.delete("pipes." + key + "." + subKey);
            }
            pipes.delete("pipes." + key);
        }
    }

    private void cleanSave() {
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

    private void relabelGroup(Location location, UUID newGroup, UUID oldGroup, List<String> checkList)
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
                    PipeChestTypeEnum chestSettingsType = this.getChestSettingsType(locationConnection, oldGroup);
                    List<Integer> chestSettingsFilterAccessSlots = this.getChestSettingsFilterAccessSlots(locationConnection, oldGroup);
                    HashMap<Integer, ItemStack > itemStack = new HashMap<Integer, ItemStack>();
                    HashMap<Integer, PipeChestFilterTypeEnum> chestSettingsFilterType= new HashMap<Integer, PipeChestFilterTypeEnum>();
                    for(int k: chestSettingsFilterAccessSlots) {
                    //for(int k =0; k < 2; k++) {
                        itemStack.put(k,this.getChestSettingsFilter(locationConnection, oldGroup, k));
                        chestSettingsFilterType.put(k, this.getChestSettingsFilterType(locationConnection, oldGroup, k));
                    }

                    removeChest(locationConnection);
                    scanPlacedChest(locationConnection);

                    this.setChestSettingsType(locationConnection, newGroup, chestSettingsType);
                    for(int k: chestSettingsFilterAccessSlots) {
                    //for(int k =0; k < 2; k++) {
                        this.setChestSettingsFilter(locationConnection, newGroup, k, itemStack.get(k));
                        this.setChestSettingsFilterType(locationConnection, newGroup, k, chestSettingsFilterType.get(k));
                    }

                }
            }
        }
    }

    public void add(Location location)
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
                    //addItem pipe to an existing group
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

    public void checkHologram(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        UUID uuid = pipes.getUUID("pipes." + key + ".hologram");
        UUID uuid2 = pipes.getUUID("pipes." + key + ".hologram2");
        HologramManager hologram = TitanMachines.hologramTool.getHologram(uuid);
        if (hologram == null)
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
    private void addHologram(Location location, List<BlockFace> hologramConnections) {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        int modelNumber = getModelNumber(hologramConnections);

        HologramManager hologramManager = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
        hologramManager.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(modelNumber));
        pipes.set("pipes." + key + ".hologram", hologramManager.getUUID());

        int pipeModel = this.type.getHorizontal();

        if (modelNumber != pipeModel + 300) {

            ItemStack pipeHead = null;

            if (hologramConnections.contains(BlockFace.UP) && hologramConnections.contains(BlockFace.DOWN)) {
                pipeHead = TitanMachines.instants.getPipe(pipeModel + 400);
            }
            else if (hologramConnections.contains(BlockFace.UP)) {
                pipeHead = TitanMachines.instants.getPipe(pipeModel + 100);
            }
            else if (hologramConnections.contains(BlockFace.DOWN)) {
                pipeHead = TitanMachines.instants.getPipe(pipeModel + 200);
            }

            if (pipeHead != null) {
                Location holoLoc = location.clone().add(0.5f, 0, 0.5f);
                HologramManager hologram = TitanMachines.hologramTool.addHologram(holoLoc);
                hologram.setEquipment(EquipmentSlot.HEAD, pipeHead);
                pipes.set("pipes." + key + ".hologram2", hologram.getUUID());
            }

        }
    }

    public void checkSurroundings(Location location)
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
    private List<BlockFace> scanChestOrientation(Location location) {
        List<BlockFace> hologramConnections = new ArrayList<BlockFace>();
        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (ContainerManager.isContainer(testLocation))
            {
                UUID group = getGroup(location);
                PipeChestTypeEnum chestSettingsType = this.getChestSettingsType(testLocation, group);
                if (chestSettingsType != PipeChestTypeEnum.NOT_CONNECTED) hologramConnections.add(blockFace);
            }
        }
        return hologramConnections;
    }

    public void rescanPipeOrientation(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        if (!pipes.contains("pipes." + key + ".location"))
        {
            deletePipe(key);
            return;
        }
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
        if (hologramManager == null) hologramManager = TitanMachines.hologramTool.addHologram(location.clone().add(0.5f, 0, 0.5f));
        if (hologramManager != null)
        {
            hologramManager.setEquipment(EquipmentSlot.HEAD, TitanMachines.instants.getPipe(modelNumber));
            pipes.set("pipes." + key + ".hologram", hologramManager.getUUID());
        }
        int pipeModel = this.type.getHorizontal();

        ItemStack pipeHead = null;

        if (hologramConnections.contains(BlockFace.UP) && hologramConnections.contains(BlockFace.DOWN)) {
            pipeHead = TitanMachines.instants.getPipe(pipeModel + 400);
        }
        else if (hologramConnections.contains(BlockFace.UP)) {
            pipeHead = TitanMachines.instants.getPipe(pipeModel + 100);
        }
        else if (hologramConnections.contains(BlockFace.DOWN)) {
            pipeHead = TitanMachines.instants.getPipe(pipeModel + 200);
        }

        if (pipeHead != null) {

            Location holoLoc = location.clone().add(0.5f, 0, 0.5f);
            HologramManager hologram = null;
            if (uuid2 != null) hologram = TitanMachines.hologramTool.getHologram(uuid2);
            if (hologram == null) hologram = TitanMachines.hologramTool.addHologram(holoLoc);
            hologram.setEquipment(EquipmentSlot.HEAD, pipeHead);

            pipes.set("pipes." + key + ".hologram2", hologram.getUUID());

        } else if (uuid2 != null) {

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

    public int getModelNumber(List<BlockFace> connections)
    {
        int number = this.type.getHorizontal();
        if ((connections.contains(BlockFace.UP) && connections.contains(BlockFace.DOWN) && connections.size() == 2)) number = number + 300;
        if (connections.contains(BlockFace.EAST)) number = number + 1;
        if (connections.contains(BlockFace.WEST)) number = number + 2;
        if (connections.contains(BlockFace.NORTH)) number = number + 10;
        if (connections.contains(BlockFace.SOUTH)) number = number + 20;
        return number;
    }
    public void scanPlacedChest(Location location) {

        for(BlockFace blockFace: blockFaces)
        {
            Location testLocation = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (isPipe(testLocation))
            {
                this.rescanPipeOrientation(testLocation);
                // this is scanned after the block is place to make sure it is a supported item,the block place gets a general idea and sends it
                if (location.getBlock().getState() instanceof Container || SensibleToolboxSupport.instance.isSupported(location) || SlimefunSupport.instance.isSupported(location)) {
                    addConnection(testLocation, location);
                    addChestConnection(testLocation, location);
                }
                UUID group = this.getGroup(testLocation);
                PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
            }
        }

    }
    private List<BlockFace> scanForChest(Location location) {
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
    public List<Integer> getChestSettingsFilterAccessSlots(Location chest, UUID group)
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
    public void setChestSettingsFilter(Location chest, UUID group, int slot, ItemStack itemStack)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".filter." + slot + ".item", itemStack);
    }
    public ItemStack getChestSettingsFilter(Location chest, UUID group, int slot) {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        return pipes.getItem("chest." + key + ".settings." + group + ".filter." + slot + ".item");
    }
    public void clearChestSettingsFilterType(Location chest, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.delete("chest." + key + ".settings." + group + ".filter");
    }
    public void setChestSettingsFilterType(Location chest, UUID group, int slot, PipeChestFilterTypeEnum type)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".filter." + slot + ".type", type.getValue());
        if (getChestSettingsType(chest, group) == PipeChestTypeEnum.CHEST_IN) createLookupKey(chest, group, slot, type);
    }
    public PipeLookUpInfo getLookUp(UUID group)
    {
        return lookups.get(group);
    }
    public void createLookupKey(Location chest, UUID group, int slot, PipeChestFilterTypeEnum type) {
        PipeLookUpInfo pipeLookUpInfo;
        if (lookups.containsKey(group)) pipeLookUpInfo =lookups.get(group);
        else  pipeLookUpInfo = new PipeLookUpInfo(group);

        if (type == PipeChestFilterTypeEnum.ALL)
        {
            pipeLookUpInfo.addItem(type, chest, slot);
        }
        else if (type != PipeChestFilterTypeEnum.DISABLED)
        {
            ItemStack chestSettingsFilter = getChestSettingsFilter(chest, group, slot);
            pipeLookUpInfo.addItem(type, chestSettingsFilter, chest, slot);
        }
        lookups.put(group, pipeLookUpInfo);
    }
    private void clearLookupGroup(UUID group)
    {
        lookups.remove(group);
    }
    public void reScanLookupGroup(UUID group)
    {
        clearLookupGroup(group);
        loadPipeLookupGroup(group);
    }

    public void loadPipeLookupGroup(UUID group) {
        List<Location> InChestsInGroup = PipesManager.getInstant(type).getInChestsInGroup(group);
        for (Location inChest : InChestsInGroup) {
            List<Integer> chestSettingsFilterAccessSlots = PipesManager.getInstant(type).getChestSettingsFilterAccessSlots(inChest, group);
            for (int k : chestSettingsFilterAccessSlots) {
                PipeChestFilterTypeEnum InChestSettingsFilterType = PipesManager.getInstant(type).getChestSettingsFilterType(inChest, group, k);
                PipesManager.getInstant(type).createLookupKey(inChest, group, k, InChestSettingsFilterType);
            }

        }
    }


    public PipeChestFilterTypeEnum getChestSettingsFilterType(Location chest, UUID group, int slot) {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        if (!pipes.contains("chest." + key + ".settings." + group + ".filter." + slot + ".type") && slot > 0) return PipeChestFilterTypeEnum.DISABLED;
        int anInt = pipes.getInt("chest." + key + ".settings." + group + ".filter." + slot + ".type");

        return PipeChestFilterTypeEnum.getPipeChestType(anInt);
    }

    public void setChestSettingsType(Location chest, UUID group, PipeChestTypeEnum type)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("chest." + key + ".settings." + group + ".type", type.getValue());
    }
    public PipeChestTypeEnum getChestSettingsType(Location chest, UUID group)
    {
        if (group == null) return PipeChestTypeEnum.NOT_CONNECTED;
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        if (!pipes.contains("chest." + key + ".settings." + group + ".type")) setChestSettingsType(chest,group, PipeChestTypeEnum.CHEST_IN);
        int anInt = pipes.getInt("chest." + key + ".settings." + group + ".type");
        return PipeChestTypeEnum.getPipeChestType(anInt);
    }
    private void removeConnection(Location pipe)
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
    private void removeChestConnection(Location chest, Location pipe)
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

    public void removeChest(Location chest)
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
                final PipesManager pipesManager = this;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        pipesManager.rescanPipeOrientation(testLocation);
                    }
                }.runTaskLater(TitanMachines.instants, 5);
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
    public boolean isChestConnected(Location chest)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        return pipes.contains("chest." + key);
    }


    public List<UUID> getChestGroups(Location chest)
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
    private void addChestConnection(Location pipe, Location chest)
    {
        UUID group = getGroup(pipe);
        addChestConnection(pipe, chest, group);
    }
    private void addChestConnection(Location from, Location chest, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(chest);
        pipes.set("groups." + group + ".chest." + key + ".location", chest);
        for(int i = 0; i < 7; i++) {
            if (!pipes.contains("chest." + key + ".groups." + i )) {
                pipes.set("chest." + key + ".groups." + i + ".id", group);
                for (int slot: ContainerManager.getInventorySlots(from, chest))
                {
                        if (!pipes.contains("chest." + key + ".settings." + group + ".filter." + slot))
                            setChestSettingsFilterType(chest, group, slot, PipeChestFilterTypeEnum.ALL);
                }
                return;
            }
        }
    }
    private void addConnection(Location pipe, Location connection)
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
    public List<Location> getOutChestsInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                PipeChestTypeEnum chestSettingsType = this.getChestSettingsType(location, group);
                if (chestSettingsType == PipeChestTypeEnum.CHEST_OUT) locations.add(location);
            }

        }
        return locations;
    }
    public List<Location> getInChestsInGroup(UUID group)
    {
        List<Location> locations = new ArrayList<Location>();
        List<Location> locationsLast = new ArrayList<Location>();
        Set<String> groups = pipes.getKeys("groups." + group + ".chest");
        for(String key: groups)
        {
            Location location = pipes.getLocation("groups." + group + ".chest." + key + ".location");
            if (location != null) {
                PipeChestTypeEnum chestSettingsType = this.getChestSettingsType(location, group);
                if (chestSettingsType == PipeChestTypeEnum.CHEST_IN) {
                    PipeChestFilterTypeEnum chestSettingsFilterType = this.getChestSettingsFilterType(location, group, 0);
                    if (chestSettingsFilterType != PipeChestFilterTypeEnum.ALL) locations.add(location);
                    else locationsLast.add(location);
                }
            }

        }
        locations.addAll(locationsLast);
        return locations;
    }
    @SuppressWarnings("unused")
    public List<Location> getChestsInGroup(UUID group)
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
    public List<UUID> getGroups()
    {
        List<UUID> output = new ArrayList<UUID>();
        Set<String> groups = pipes.getKeys("groups");
        for(String key: groups)
        {
            try {
                UUID uuid = UUID.fromString(key);
                output.add(uuid);
            } catch (Exception ignored) {
                pipes.delete(key);
            }

        }
        return output;
    }
    public Boolean hasGroup(UUID uuid)
    {
        return pipes.contains("groups." + uuid.toString());
    }
    public UUID getGroup(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        return pipes.getUUID("pipes." + key + ".groupid");
    }
    public void addToGroup(Location pipe, UUID group)
    {
        String key = TitanMachines.serializeTool.serializeLocation(pipe);
        pipes.set("pipes." + key + ".groupid", group);
        pipes.set("groups." + group + ".pipes." + key + ".location", pipe);
    }
    public int getGroupSize(UUID group)
    {
        Set<String> keys = pipes.getKeys("groups." + group + ".pipes");
        if (keys == null) return 0;
        return keys.size();
    }
    private void mergeGroup(UUID keepingGroup, UUID removingGroup)
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
                if (uuid != null)
                {
                    pipes.set("chest." + key + ".groups." + i + ".id", keepingGroup);
                    //Setting merge\
                    PipeChestTypeEnum chestSettingsType = PipeChestTypeEnum.getPipeChestType(pipes.getInt("chest." + key + ".settings." + removingGroup + ".type"));
                    if (!pipes.contains("chest." + key + ".settings." + removingGroup + ".type")) chestSettingsType = PipeChestTypeEnum.CHEST_IN;
                    pipes.set("chest." + key + ".settings." + keepingGroup + ".type", chestSettingsType.getValue());

                    Set<String> keys = pipes.getKeys("chest." + key + ".settings." + removingGroup + ".filter");
                    List<Integer> chestSettingsFilterAccessSlots = new ArrayList<Integer>();
                    for(String s: keys)
                    {
                        chestSettingsFilterAccessSlots.add(Integer.parseInt(s));
                    }
                    for (int k: chestSettingsFilterAccessSlots) {
                        ItemStack itemStack = pipes.getItem("chest." + key + ".settings." + removingGroup + ".filter." + k + ".item");
                        PipeChestFilterTypeEnum chestSettingsFilterType = PipeChestFilterTypeEnum.getPipeChestType(pipes.getInt("chest." + key + ".settings." + removingGroup + ".filter." + k + ".type"));
                        pipes.set("chest." + key + ".settings." + keepingGroup + ".filter." + k + ".item", itemStack);
                        pipes.set("chest." + key + ".settings." + keepingGroup + ".filter." + k + ".type", chestSettingsFilterType.getValue());
                    }
                }
            }

        }
        deleteGroup(removingGroup);
    }
    public void deleteGroup(UUID group)
    {
        pipes.delete("groups." + group);
    }

    @NotNull
    private UUID getNewUUID() {
        UUID id = UUID.randomUUID();
        if (pipes.contains("groups." + id )) return getNewUUID();
        return id;
    }
    public boolean isPipe(Location location)
    {
        String key = TitanMachines.serializeTool.serializeLocation(location);
        return pipes.contains("pipes." + key);
    }
    public Set<String> getPipes()
    {
        return pipes.getKeys("pipes");
    }
    public void save()
    {
        pipes.save();
    }
}

