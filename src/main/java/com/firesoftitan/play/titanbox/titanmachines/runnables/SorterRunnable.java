package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.ItemSorterManager;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SorterRunnable extends BukkitRunnable {

    private List<String> quList = new ArrayList<String>();

    public SorterRunnable() {

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
        if (!TitanMachines.sorterEnabled)
        {
            return;
        }
        long startTIme = System.currentTimeMillis();
        if (quList.isEmpty())
        {
            Set<String> keys = ItemSorterManager.instance.getKeys();
            if (keys == null || keys.isEmpty()) return;
            quList  = new ArrayList<String>(keys);
        }
        String key = quList.get(0);
        quList.remove(0);
        if (key.equals("players")) return;
        Location location = ItemSorterManager.instance.getLocation(key);
        if (location != null && location.getChunk().isLoaded()) {
            Block block = location.getBlock();
            World world = block.getWorld();
            if (block.getType() != Material.HOPPER) {
                block.setType(Material.HOPPER);
            }
            Hopper hopper = (Hopper) block.getState();
            BlockFace facing = ((Directional) hopper.getBlockData()).getFacing();
            Block relative = block.getRelative(facing);
            Inventory goingTo = null;
            boolean locationsEqual =  false;
            if (relative.getState() instanceof Container)
            {
                goingTo = ((Container) relative.getState()).getInventory();
                locationsEqual = TitanMachines.tools.getLocationTool().isLocationsEqual(goingTo.getLocation(), location);
                if (locationsEqual) System.out.println(locationsEqual);
            }
            if (!isPowered(block) && !locationsEqual) {
                Inventory inventory = hopper.getInventory();
                Particle value = Particle.COMPOSTER;

                world.spawnParticle(value, block.getLocation().clone().add(0.5, 1, 0.5), 5);
                for(int i = 0; i < inventory.getSize(); i++)
                {
                    ItemStack item = inventory.getItem(i);
                    if (!TitanMachines.tools.getItemStackTool().isEmpty(item)) {
                        List<Location> containerItems = ItemSorterManager.instance.getSortingContainer(location, item);
                        List<Location> containerMaterials = ItemSorterManager.instance.getSortingContainer(location, item.getType());
                        inventory.setItem(i, null);
                        if (containerItems != null && !containerItems.isEmpty()) {
                            sortItems(item, inventory, goingTo, containerItems, 1);
                        }else if (containerMaterials != null && !containerMaterials.isEmpty())
                        {
                            sortItems(item, inventory, goingTo, containerMaterials, 2);
                        }
                        else
                        {
                            if (goingTo != null)
                            {
                                goingTo.addItem(item);
                            }
                            else {
                                world.dropItem(hopper.getLocation().clone().subtract(0,1,0), item);
                            }
                        }
                    }
                }

            }
        }
        long doneTime = System.currentTimeMillis() - startTIme;
        if (doneTime > 100)
            TitanMachines.messageTool.sendMessageSystem("Sorter took to long:" + key + ":" + doneTime + " ms");
    }

    private void sortItems(ItemStack item, Inventory hopper, Inventory outputChest, List<Location> containerItems, int sortingT) {
        Location location = hopper.getLocation();
        World world = location.getWorld();
        for (Location sortLocation: containerItems) {
            int sortingType = ItemSorterManager.instance.getSettingsSortingType(location, sortLocation);
            if (sortingType > 0 && sortingT == sortingType) {
                if (sortLocation.getBlock().getState() instanceof Container sortedChest) {
                    item = addItem(sortedChest.getInventory(), item); //adds to a sorting chest
                }
                else {
                    if (SensibleToolboxSupport.instance.isSupported(sortLocation))
                    {
                        item = addItem(sortLocation, item);
                    }
                }
                if (TitanMachines.tools.getItemStackTool().isEmpty(item)) return;
            }
        }
        item = addItem(outputChest, item);// adds leftovers to an output chest on hopper
        item = addItem(hopper, item);// adds leftovers to back to hopper
        if (!TitanMachines.tools.getItemStackTool().isEmpty(item)) world.dropItem(location, item); //drops any leftovers
    }
    public ItemStack addItem(Location location, ItemStack itemStack)
    {
        return SensibleToolboxSupport.instance.addStorage(location, itemStack);
    }
    public ItemStack addItem(Inventory inventory, ItemStack itemStack)
    {
        if (inventory != null && !TitanMachines.tools.getItemStackTool().isEmpty(itemStack)) {
            HashMap<Integer, ItemStack> integerItemStackHashMap = inventory.addItem(itemStack.clone());
            int amount = 0;
            for (ItemStack leftOvers: integerItemStackHashMap.values())
            {
                amount = amount + leftOvers.getAmount();
            }
            if (amount == 0) return null;
            ItemStack outputStack = itemStack.clone();
            outputStack.setAmount(amount);
            return outputStack.clone();
        }
        return itemStack;
    }
}
