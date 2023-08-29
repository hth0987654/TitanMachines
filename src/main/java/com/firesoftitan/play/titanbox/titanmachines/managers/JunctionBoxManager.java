package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.guis.JunctionBoxGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.SorterGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class JunctionBoxManager extends TitanBlockManager {


    public static JunctionBoxManager instance = null;


    public JunctionBoxManager(JavaPlugin plugin, ItemStack titanItem, ItemStack[] recipe) {
        super(plugin, titanItem, recipe);
        instance = this;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent, Location location, TitanBlock titanBlock) {
        JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
        if (junctionBoxBlock != null)
        {
            JunctionBoxGUI junctionBoxGUI = new JunctionBoxGUI(playerInteractEvent.getPlayer(), location);
            junctionBoxGUI.open();
        }

    }


}
