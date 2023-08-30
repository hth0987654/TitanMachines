package com.firesoftitan.play.titanbox.titanmachines.managers;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class LumberManager  extends TitanBlockManager {
    public static LumberManager instance = null;
    public LumberManager(JavaPlugin plugin, ItemStack titanItem, ItemStack[] recipe) {
        super(plugin, titanItem, recipe);
        instance = this;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent, Location location, TitanBlock titanBlock) {
        LumberjackBlock lumberjackBlock = LumberjackBlock.convert(titanBlock);
        if (lumberjackBlock != null)
        {
            if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material saplingMaterial = lumberjackBlock.getSaplingMaterial();
                Player player = playerInteractEvent.getPlayer();
                if (saplingMaterial == null) TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Lumberjack is Empty");
                else TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Lumberjack has: " + ChatColor.WHITE + lumberjackBlock.getSaplingCount() + "x" + TitanMachines.formattingTool.fixCapitalization(saplingMaterial.name()) );
                if (player.isSneaking()) lumberjackBlock.setPower(!lumberjackBlock.isPowered());
                if (lumberjackBlock.isPowered())
                    TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Lumberjack is: " + ChatColor.GREEN + "On");
                else
                    TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Lumberjack is: " + ChatColor.RED + "Off");

                ItemStack itemInUse = player.getInventory().getItemInMainHand();
                if (saplingMaterial != null && !TitanMachines.itemStackTool.isEmpty(itemInUse)) {
                    if (TitanMachines.itemStackTool.isItemEqual(itemInUse, new ItemStack(saplingMaterial))) {
                        if (lumberjackBlock.getSaplingCount() < 8) {
                            lumberjackBlock.addSapling(saplingMaterial);
                            itemInUse.setAmount(itemInUse.getAmount() - 1);
                        }
                        else TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Lumberjack is full");
                    }
                }
                else {
                    if (itemInUse.getType().name().contains("SAPLING"))
                    {
                        lumberjackBlock.addSapling(itemInUse.getType());
                        itemInUse.setAmount(itemInUse.getAmount() - 1);
                    }
                }
            }
        }
    }
}
