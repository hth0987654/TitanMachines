package com.firesoftitan.play.titanbox.titanmachines.blocks;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PipeBlock extends TitanBlock {
    public static final String titanID = "PIPE";
    public static PipeBlock convert(TitanBlock titanBlock)
    {
        if (titanBlock.getTitanID() == null) return null;
        if (!titanBlock.getTitanID().equals(PipeBlock.titanID)) return null;
        return new PipeBlock(titanBlock.getSaveManager());
    }
    public PipeBlock(String titanID, ItemStack itemStack, Location location) {
        super(titanID, itemStack, location);
    }

    public PipeBlock(SaveManager saveManager) {
        super(saveManager);
    }
    public void setup()
    {
        this.setRedrawBlock(false);
        this.setBreakBlockAllowed(false);
    }
}
