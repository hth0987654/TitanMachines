package com.firesoftitan.play.titanbox.titanmachines.listeners;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import com.firesoftitan.play.titanbox.titanmachines.guis.JunctionBoxGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TitanBlockListener extends com.firesoftitan.play.titanbox.libs.listeners.TitanBlockListener {
    @Override
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent, Location location, TitanBlock titanBlock) {
        if (titanBlock.getTitanID().equals(JunctionBoxBlock.titanID))
        {
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            if (junctionBoxBlock != null)
            {
                JunctionBoxGUI junctionBoxGUI = new JunctionBoxGUI(playerInteractEvent.getPlayer(), location);
                junctionBoxGUI.open();
            }
        }
        if (titanBlock.getTitanID().equals(LumberjackBlock.titanID))
        {
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
}
