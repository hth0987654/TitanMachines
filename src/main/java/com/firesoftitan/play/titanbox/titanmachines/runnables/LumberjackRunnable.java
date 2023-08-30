package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import com.firesoftitan.play.titanbox.titanmachines.managers.BlockBreakerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.LumberManager;
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
import java.util.Set;

public class LumberjackRunnable extends BukkitRunnable {

    private final List<Location> quList = new ArrayList<Location>();

    @Override
    public void run() {
        LumberManager manager = LumberManager.instance;
        if (quList.isEmpty()) {
            Set<Location> locations = LumberManager.instance.getLocations();
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
            TitanBlock titanBlock = manager.getTitanBlock(location);
            if (titanBlock == null) continue;
            LumberjackBlock lumberjackBlock = LumberjackBlock.convert(titanBlock);
            if (lumberjackBlock == null) continue;
            if (lumberjackBlock.isPowered()) {
                if (location.getChunk().isLoaded()) {
                    Material saplingMaterial = lumberjackBlock.getSaplingMaterial();
                    int saplingCount = lumberjackBlock.getSaplingCount();
                    Block block = location.getBlock();
                    if (block.getBlockData() instanceof  Directional) {
                        BlockFace facing = ((Directional) block.getBlockData()).getFacing();
                        Block base = block.getRelative(facing);
                        if (isBlockBreakable(base)) {
                            World world = base.getWorld();
                            for (int y = 0; y < 12; y++) {
                                for (int x = -3; x < 4; x++) {
                                    for (int z = -3; z < 4; z++) {
                                        Block relative = block.getRelative(x, y, z);
                                        if (isBlockBreakable(relative)) {
                                            Collection<ItemStack> drops = relative.getDrops();
                                            if (y == 0)
                                            {
                                                if (saplingCount > 0) {
                                                    relative.setType(saplingMaterial);
                                                    lumberjackBlock.removeSapling(saplingMaterial);
                                                }
                                                else relative.setType(Material.AIR);
                                            }
                                            else relative.setType(Material.AIR);
                                            for (ItemStack itemStack : drops) {
                                                boolean dropsap = true;
                                                if (itemStack.getType().name().contains("SAPLING"))
                                                {
                                                    if (saplingMaterial == null || (itemStack.getType() == saplingMaterial && saplingCount < 9) || itemStack.getType() != saplingMaterial)
                                                    {
                                                        lumberjackBlock.addSapling( itemStack.getType());
                                                        saplingMaterial = lumberjackBlock.getSaplingMaterial();
                                                        saplingCount = lumberjackBlock.getSaplingCount();
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
