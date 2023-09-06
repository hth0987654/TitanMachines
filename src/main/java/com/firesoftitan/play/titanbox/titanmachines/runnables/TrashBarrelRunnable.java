package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.managers.TrashBarrelManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TrashBarrelRunnable extends BukkitRunnable {

    private List<String> quList = new ArrayList<String>();

    @Override
    public void run() {
        TrashBarrelManager manager = TrashBarrelManager.instance;
        if (quList.isEmpty()) quList = manager.getKeys();
        List<String> runningKeys = new ArrayList<String>();
        for(int i = 0;i<10; i++)
        {
            if (!quList.isEmpty()) {
                runningKeys.add(quList.get(0));
                quList.remove(0);
            }
        }
        for(String key: runningKeys) {
            if (manager.isPowered(key)) {
                Location location = manager.getLocation(key);
                if (location.getChunk().isLoaded()) {
                    Block block = location.getBlock();
                    if (block.getType() == Material.BARREL) {
                        Barrel chest = ((Barrel) block.getState());
                        Inventory inventory = chest.getInventory();
                        if (inventory.getViewers().isEmpty()) inventory.clear();
                    }
                }
            }
        }
    }
}
