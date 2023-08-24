package com.firesoftitan.play.titanbox.titanmachines.runnables;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.ChunkHopperManager;
import com.firesoftitan.play.titanbox.titanmachines.support.WildStackerSupport;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PickUpItemRunnable extends BukkitRunnable {
    private final Hopper hopper;
    private final Block block;
    private final Entity entity;
    public PickUpItemRunnable(Block hopper, Entity itemEntity) {
        this.block = hopper;
        this.hopper = (Hopper) this.block.getState();
        this.entity = itemEntity;
    }

    @Override
    public void run() {
        if (!TitanMachines.hopperEnabled)
        {
            this.cancel();
            return;
        }
        long startTIme = System.currentTimeMillis();

        Inventory inventory = hopper.getInventory();
        Inventory goingTo = null;
        BlockFace facing = ((Directional) hopper.getBlockData()).getFacing();
        Block relative = block.getRelative(facing);
        if (relative.getState() instanceof Container)
        {
            goingTo = ((Container) relative.getState()).getInventory();
        }
        Item item = (Item) entity;
        if (!entity.isDead()) {
            ItemStack clone = item.getItemStack().clone();
            int amount = WildStackerSupport.instance.getAmount(item);
            if (goingTo != null) {
                amount = addToInventory(goingTo, clone, amount);
            }
            if (amount > 0 ) amount = addToInventory(inventory, clone, amount);
            WildStackerSupport.instance.setAmount(item, amount);
        }
        else
        {
            this.cancel();
        }
        String key = ChunkHopperManager.getKey(block.getLocation());
        long doneTime = System.currentTimeMillis() - startTIme;
        if (doneTime > 100)
            TitanMachines.messageTool.sendMessageSystem("Hopper took to long:" + key + ":" + doneTime + " ms");
    }
    private int addToInventory(Inventory inventory, ItemStack clone, int amount) {
        HashMap<Integer, ItemStack> integerItemStackHashMap = inventory.addItem(clone);
        if (integerItemStackHashMap.isEmpty()) {
            return 0;
        } else {
            amount = 0;
            for (int index : integerItemStackHashMap.keySet()) {
                ItemStack itemStack = integerItemStackHashMap.get(index);
                amount = amount + itemStack.getAmount();
            }
        }
        if (amount < 1) amount = 0;
        return amount;
    }
}
