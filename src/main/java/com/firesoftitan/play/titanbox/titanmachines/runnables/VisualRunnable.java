package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerVisualManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.ItemSorterManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VisualRunnable  extends BukkitRunnable {
    private final HashMap<String, Long> loadedChunks = new HashMap<String, Long>();
    @Override
    public void run() {
        cleanUpOldChunks();

        for(Player player: Bukkit.getOnlinePlayers())
        {
           // DebugPipeViewer(player);

            String chunkKey = ContainerVisualManager.getKey(player.getLocation());
            loadedChunks.put(chunkKey, System.currentTimeMillis());
            Location playerLocation = player.getLocation();
            if (ItemSorterManager.instance.hasSorter(player))
            {
                List<Location> sortingContainers = ItemSorterManager.instance.getSortingContainers(player);
                for(Location containerLocation: sortingContainers)
                {
                    if (containerLocation.getWorld() != null &&  playerLocation.getWorld() != null) {
                        if (containerLocation.getWorld().getName().equals(playerLocation.getWorld().getName())) {
                            if (containerLocation.distance(playerLocation) < 16) {
                                ItemStack itemStack = ItemSorterManager.instance.getSortingItem(player, containerLocation);
                                BlockFace settingsSortingFacing = ItemSorterManager.instance.getSettingsSortingFacing(player, containerLocation);
                                int settingsSortingType = ItemSorterManager.instance.getSettingsSortingType(player, containerLocation);
                                ContainerVisualManager manager = ContainerVisualManager.getManager(containerLocation);
                                if (settingsSortingType > 0) {
                                    if (manager == null) {
                                        new ContainerVisualManager(containerLocation, itemStack, settingsSortingFacing);
                                    } else {
                                        if (!TitanMachines.itemStackTool.isItemEqual(manager.getItemStack(), itemStack)) {
                                            ContainerVisualManager.removeManager(containerLocation);
                                            new ContainerVisualManager(containerLocation, itemStack, settingsSortingFacing);
                                        }
                                        if (settingsSortingFacing != manager.getBlockFace())
                                            manager.setBlockFace(settingsSortingFacing);
                                    }
                                } else if (manager != null) {
                                    ContainerVisualManager.removeManager(containerLocation);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void DebugPipeViewer(Player player) {
        Block targetBlockExact = player.getTargetBlockExact(5);
        if (targetBlockExact != null)
        {
            String uuid = "";
            String connections = "";
            if (PipesManager.isPipe(targetBlockExact.getLocation())) {
                uuid =  PipesManager.getGroup(targetBlockExact.getLocation()) + "";
            }
            List<Location> connections1 = PipesManager.getConnections(targetBlockExact.getLocation());
            if (!connections1.isEmpty())
            {
                for(Location l: connections1)
                {
                    connections = connections + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ":";
                }
            }
            if (PipesManager.isChestConnected(targetBlockExact.getLocation()))
            {
                List<UUID> uuids = PipesManager.getChestGroups(targetBlockExact.getLocation());
                for(UUID uuid1:uuids)
                {
                    connections = connections + uuid1 + "|";
                }
            }
            if (!uuid.isEmpty() || !connections.isEmpty())
            {
                player.sendTitle(uuid, connections, 1, 2, 1);
            }
        }
    }

    public void removeChunk(Player player)
    {
        Location location = player.getLocation();
        String chunkKey = ContainerVisualManager.getKey(location);
        ContainerVisualManager.removeChunk(chunkKey);
        loadedChunks.remove(chunkKey);
    }
    public void removeAll()
    {
        for(String key: loadedChunks.keySet())
        {
                ContainerVisualManager.removeChunk(key);
        }

    }
    private void cleanUpOldChunks() {
        List<String> delete = new ArrayList<String>();
        for(String key: loadedChunks.keySet())
        {
            Long aLong = loadedChunks.get(key);
            if (System.currentTimeMillis() - aLong > 10000)
            {
                ContainerVisualManager.removeChunk(key);
                delete.add(key);
            }
        }
        for(String key: delete)
        {
            loadedChunks.remove(key);
        }
    }
}
