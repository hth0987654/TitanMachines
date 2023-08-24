package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.BlockBreakerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.LumberjackManager;
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

public class LumberjackRunnable extends BukkitRunnable {

    private List<String> quList = new ArrayList<String>();

    @Override
    public void run() {
        LumberjackManager manager = LumberjackManager.instance;
        if (quList.size() == 0) quList = manager.getKeys();
        List<String> runningKeys = new ArrayList<String>();
        for(int i = 0;i<10; i++)
        {
            if (quList.size() > 0) {
                runningKeys.add(quList.get(0));
                quList.remove(0);
            }
        }
        for(String key: runningKeys) {
            if (manager.isPowered(key)) {
                Location location = manager.getLocation(key);
                if (location.getChunk().isLoaded()) {
                    Material saplingMaterial = manager.getSaplingMaterial(location);
                    int saplingCount = manager.getSaplingCount(location);
                    Block block = location.getBlock();
                    if (block.getBlockData() instanceof  Directional) {
                        BlockFace facing = ((Directional) block.getBlockData()).getFacing();
                        Block base = block.getRelative(facing);
                        if (isBlockBreakable(base)) {
                            World world = base.getWorld();
                            for (int y = 0; y < 9; y++) {
                                for (int x = -3; x < 4; x++) {
                                    for (int z = -3; z < 4; z++) {
                                        Block relative = block.getRelative(x, y, z);
                                        if (isBlockBreakable(relative)) {
                                            Collection<ItemStack> drops = relative.getDrops();
                                            relative.setType(Material.AIR);
                                            for (ItemStack itemStack : drops) {
                                                boolean dropsap = true;
                                                if (itemStack.getType().name().contains("SAPLING"))
                                                {
                                                    if (saplingMaterial == null || (itemStack.getType() == saplingMaterial && saplingCount < 9) || itemStack.getType() != saplingMaterial)
                                                    {
                                                        manager.addSapling(location, itemStack.getType());
                                                        saplingMaterial = manager.getSaplingMaterial(location);
                                                        saplingCount = manager.getSaplingCount(location);
                                                        dropsap = false;
                                                    }
                                                }
                                                if (dropsap) world.dropItem(relative.getLocation().clone().add(0.5f, 1, 0.5f), itemStack);
                                            }
                                        }
                                    }
                                }
                            }
                            world.playSound(base.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
                            if (saplingCount > 0)
                            {
                                base.setType(saplingMaterial);
                                manager.removeSapling(location, saplingMaterial);
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isBlockBreakable(Block relative)
    {
        Material type = relative.getType();
        if (type.name().contains("WOOD") || type.name().contains("LOG") || type.name().contains("LEAVES")) return true;
        return false;
    }
}
