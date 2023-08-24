package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.AreaHopperManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.ChunkHopperManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class HopperRunnable extends BukkitRunnable {

    private List<String> quList = new ArrayList<String>();

    public HopperRunnable() {

    }
    public boolean isPowered(Block block)
    {
        if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) return true;
        for(BlockFace blockFace: BlockFace.values())
        {
            if (block.isBlockFacePowered(blockFace)) return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (!TitanMachines.hopperEnabled)
        {
            return;
        }
        if (quList.isEmpty())
        {
            quList  = new ArrayList<String>();
            quList.addAll(ChunkHopperManager.getKeys());
            quList.addAll(AreaHopperManager.getKeys());
            return;
        }
        String key = quList.get(0);
        quList.remove(0);

        Location location = ChunkHopperManager.getLocation(key );
        if (location != null && location.getChunk().isLoaded()) {
            Block block = location.getBlock();
            World world = block.getWorld();
            if (block.getType() != Material.HOPPER) {
                block.setType(Material.HOPPER);
            }
            Hopper hopper = (Hopper) block.getState();
            if (!isPowered(block)) {
                Particle value = Particle.NAUTILUS;
                world.spawnParticle(value, block.getLocation().clone().add(0.5, 2, 0.5), 5);
            }
        }


    }
}
