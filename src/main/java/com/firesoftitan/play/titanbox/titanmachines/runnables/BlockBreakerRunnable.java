package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.BlockBreakerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockBreakerRunnable  extends BukkitRunnable {

    private List<String> quList = new ArrayList<String>();

    @Override
    public void run() {
        BlockBreakerManager manager = BlockBreakerManager.instance;
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
                    if (block.getBlockData() instanceof  Directional) {
                        BlockFace facing = ((Directional) block.getBlockData()).getFacing();
                        Block relative = block.getRelative(facing);
                        if (isBlockBreakable(relative)) {
                            World world = relative.getWorld();
                            Collection<ItemStack> drops = relative.getDrops();
                            relative.setType(Material.AIR);
                            world.playSound(relative.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                            for (ItemStack itemStack : drops) {
                                world.dropItem(relative.getLocation().clone().add(0.5f, 1, 0.5f), itemStack);
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isBlockBreakable(Block relative)
    {
        if (TitanMachines.tools.getEntityTool().isTileEntity(relative)) return false;
        switch (relative.getType())
        {
            case PLAYER_WALL_HEAD:
            case VOID_AIR:
            case CAVE_AIR:
            case LAVA:
            case WATER:
            case AIR:
            case PLAYER_HEAD:
                return false;
        }
        return true;
    }
}
