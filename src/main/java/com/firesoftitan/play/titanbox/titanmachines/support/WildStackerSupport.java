package com.firesoftitan.play.titanbox.titanmachines.support;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class WildStackerSupport extends PluginSupport{
    public static WildStackerSupport instance;
    public WildStackerSupport() {
        super("WildStacker");
        WildStackerSupport.instance = this;
    }
    public boolean isStackable(Item item)
    {
        if (this.isInstalled()) {
            try {
                StackedItem stackedItem = WStackedItem.of(item);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }
    public void setAmount(Item item, int amount)
    {
        if (item == null || item.isDead()) return;
        if (this.isInstalled() && isStackable(item)) {
            StackedItem stackedItem = WStackedItem.of(item);
            stackedItem.setStackAmount(amount, true);
            if (amount <= 0) stackedItem.remove();
        }
        if (amount <= 0)
        {
            if (item.isDead()) return;
            item.remove();
            return;
        }
        ItemStack clone = item.getItemStack().clone();
        clone.setAmount(amount);
        item.setItemStack(clone);

    }
    public int getAmount(Item item)
    {
        if (item == null || item.isDead()) return 0;
        if (this.isInstalled() && isStackable(item)) {
            StackedItem stackedItem = WStackedItem.of(item);
            return stackedItem.getStackAmount();
        }
        return item.getItemStack().getAmount();
    }
}
