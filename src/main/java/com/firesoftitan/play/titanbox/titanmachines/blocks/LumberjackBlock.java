package com.firesoftitan.play.titanbox.titanmachines.blocks;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LumberjackBlock  extends TitanBlock {
    public static final String titanID = "LUMBERJACK";
    public static LumberjackBlock convert(TitanBlock titanBlock)
    {
        if (titanBlock.getTitanID() == null) return null;
        if (!titanBlock.getTitanID().equals(LumberjackBlock.titanID)) return null;
        return new LumberjackBlock(titanBlock.getSaveManager());
    }

    public LumberjackBlock(String titanID, ItemStack itemStack, Location location) {
        super(titanID, itemStack, location);
        this.saveManager.set("power", true);
        updateBlock(this);
    }

    public LumberjackBlock(SaveManager saveManager) {
        super(saveManager);
    }

    public void setPower(Boolean power)
    {

        this.saveManager.set("power", power);
        updateBlock(this);

    }
    public boolean removeSapling(Material material)
    {
        int amount = 0;
        if (this.saveManager.contains("sapling.material"))
        {
            if (this.saveManager.getString("sapling.material").equals(material.name()))
            {
                amount = this.saveManager.getInt("sapling.count");
                if (amount > 0) {
                    this.saveManager.set("sapling.material", material.name());
                    this.saveManager.set("sapling.count", amount - 1);
                    updateBlock(this);
                    return true;
                }
            }
        }
        return false;
    }
    public Material getSaplingMaterial()
    {
        String string = this.saveManager.getString("sapling.material");
        return Material.getMaterial(string);
    }
    public int getSaplingCount()
    {
        return this.saveManager.getInt("sapling.count");
    }

    public void addSapling(Material material)
    {
        addSapling(material, 1);
    }
    public void addSapling(Material material, int count)
    {
        int amount = 0;
        if (this.saveManager.contains("sapling.material"))
        {
            if (this.saveManager.getString("sapling.material").equals(material.name()))
            {
                amount = this.saveManager.getInt("sapling.count");
            }
        }
        this.saveManager.set("sapling.material", material.name());
        this.saveManager.set("sapling.count", amount + count);
        updateBlock(this);
    }

    public boolean isPowered()
    {
        return this.saveManager.getBoolean("power");
    }

}
