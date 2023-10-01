package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.libs.TitanBoxLibs;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.libs.managers.WorkerManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.MobKillerBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MobKillerRunnable extends BukkitRunnable {
    private final List<Location> quList = new ArrayList<Location>();
    private final HashMap<String, Long> timedKillList = new HashMap<String, Long>();
    @Override
    public void run() {
        if (quList.isEmpty()) {
            Set<Location> locations = TitanBlockManager.getLocations(MobKillerBlock.titanID);
            quList.addAll(locations);
        }
        List<Location> runningKeys = new ArrayList<Location>();
        for(int i = 0;i<10; i++)
        {
            if (!quList.isEmpty()) {
                runningKeys.add(quList.get(0));
                quList.remove(0);
            }
        }
        if (runningKeys.isEmpty()) return;
        for(Location location: runningKeys) {
            String key = TitanMachines.serializeTool.serializeLocation(location);
            long time = 0;
            if (timedKillList.containsKey(key))
            {
                time = timedKillList.get(key);
            }
            if (System.currentTimeMillis() - time > 500) {
                timedKillList.put(key, System.currentTimeMillis());
                Player worker = TitanBoxLibs.workerManager.getCraftWorker(location);
                List<Entity> nearbyEntities = worker.getNearbyEntities(7, 7, 7);
                for (Entity entity : nearbyEntities) {
                    if ((entity instanceof Mob livingEntity) && entity.getType() != EntityType.PLAYER) {
                        livingEntity.damage(0.5f, worker);
                    }
                }
            }
        }
    }
}
