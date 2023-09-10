package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.libs.runnables.TitanSaverRunnable;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.listeners.MainListener;
import com.firesoftitan.play.titanbox.titanmachines.managers.*;

public class SaveRunnable extends TitanSaverRunnable {
    public SaveRunnable() {
        super(TitanMachines.instants);
    }
    @Override
    public void run() {
        AreaHopperManager.save();
        ChunkHopperManager.save();
        ItemSorterManager.instance.save();
        for(PipeTypeEnum typeEnum: PipeTypeEnum.values()) {
            PipesManager.getInstant(typeEnum).save();
        }
        BlockBreakerManager.instance.save();
        TrashBarrelManager.instance.save();
    }
}
