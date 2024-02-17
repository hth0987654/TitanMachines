package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class PipeLoaderRunnable  extends BukkitRunnable {
    @Override
    public void run() {
        for (PipeTypeEnum type: PipeTypeEnum.values()) {
            System.out.println("Starting Fast Lookup for: " + type.getCaption());
            long startTImeL = System.currentTimeMillis();
            List<UUID> groups = PipesManager.getInstant(type).getGroups();
            for(UUID group: groups) {
                PipesManager.getInstant(type).loadPipeLookupGroup(group);
            }
            long done = System.currentTimeMillis() - startTImeL;
            System.out.println("Created fast lookups, done: " + done + " ms");
        }
        new PipeRunnable(10);
    }


}
