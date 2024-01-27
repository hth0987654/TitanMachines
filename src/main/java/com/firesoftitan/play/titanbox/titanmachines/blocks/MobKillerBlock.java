package com.firesoftitan.play.titanbox.titanmachines.blocks;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class MobKillerBlock extends TitanBlock {
    public static final String titanID = "MOB_KILLER_BLOCK";
    public static MobKillerBlock convert(TitanBlock titanBlock)
    {
        if (titanBlock.getTitanID() == null) return null;
        if (!titanBlock.getTitanID().equals(MobKillerBlock.titanID)) return null;
        return new MobKillerBlock(titanBlock.getSaveManager());
    }
    public MobKillerBlock(String titanID, ItemStack itemStack, Location location) {
        super(titanID, itemStack, location);
    }

    public MobKillerBlock(SaveManager saveManager) {
        super(saveManager);
    }

    public void setDamage(double damage)
    {
        this.saveManager.set("damage", damage);
        updateBlock(this);
    }
    public double getDamage()
    {
        double damage = this.saveManager.getDouble("damage");
        if (damage < 0.5f) damage = 0.5f;
        return damage;
    }



}
