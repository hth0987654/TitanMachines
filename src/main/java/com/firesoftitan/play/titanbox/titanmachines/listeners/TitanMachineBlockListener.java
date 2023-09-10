package com.firesoftitan.play.titanbox.titanmachines.listeners;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.events.TitanBlockEvent;
import com.firesoftitan.play.titanbox.libs.events.TitanBlockInteractEvent;
import com.firesoftitan.play.titanbox.libs.listeners.TitanBlockListener;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.PipeBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.guis.JunctionBoxGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.PipeConnectionGUI;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TitanMachineBlockListener extends TitanBlockListener {
    @Override
    public void onPlayerInteract(TitanBlockInteractEvent event) {
        TitanBlock titanBlock = event.titanBlock();
        Player player = event.player();
        Location location = event.location();
        Action action = event.action();
        ItemStack itemStack = event.item();
        if (titanBlock.getTitanID().equals(PipeBlock.titanID))
        {
            PipesManager.getInstant(PipeTypeEnum.COPPER).rescanPipeOrientation(location);
            if (action == Action.LEFT_CLICK_BLOCK && !TitanMachines.itemStackTool.isEmpty(itemStack))
            {
                if (itemStack.getType() == Material.DIAMOND_PICKAXE || itemStack.getType() == Material.NETHERITE_PICKAXE
                        || itemStack.getType() == Material.GOLDEN_PICKAXE  || itemStack.getType() == Material.IRON_PICKAXE
                        || itemStack.getType() == Material.STONE_PICKAXE  || itemStack.getType() == Material.WOODEN_PICKAXE)
                {
                    PipesManager.getInstant(PipeTypeEnum.COPPER).remove(location);
                    TitanBlock.breakBlock(titanBlock);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PipesManager.getInstant(PipeTypeEnum.COPPER).checkSurroundings(location);
                        }
                    }.runTaskLater(TitanMachines.instants, 1);
                }
            }
            if (action == Action.RIGHT_CLICK_BLOCK) {
                if (PipesManager.getInstant(PipeTypeEnum.COPPER).isPipe(location))
                {
                    PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, location);
                    pipeConnectionGUI.open();
                }
            }
        }
        if (titanBlock.getTitanID().equals(JunctionBoxBlock.titanID))
        {
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            if (junctionBoxBlock != null)
            {
                JunctionBoxGUI junctionBoxGUI = new JunctionBoxGUI(player, location);
                junctionBoxGUI.open();
            }
        }
        if (titanBlock.getTitanID().equals(LumberjackBlock.titanID))
        {
            LumberjackBlock lumberjackBlock = LumberjackBlock.convert(titanBlock);
            if (lumberjackBlock != null)
            {
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    Material saplingMaterial = lumberjackBlock.getSaplingMaterial();
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

    @Override
    public void onBlockPlace(TitanBlockEvent event) {
        TitanBlock titanBlock = event.titanBlock();
        Player player = event.player();
        Location location = event.location();
        if (titanBlock.getTitanID().equals(PipeBlock.titanID))
        {
            PipeBlock pipeBlock = PipeBlock.convert(titanBlock);
            if (pipeBlock != null) pipeBlock.setup();
            PipesManager.getInstant(PipeTypeEnum.COPPER).add(location);
            new BukkitRunnable() {
                @Override
                public void run() {
                    location.getBlock().setType(Material.BARRIER);
                    PipesManager.getInstant(PipeTypeEnum.COPPER).checkSurroundings(location);
                }
            }.runTaskLater(TitanMachines.instants, 1);
        }
        if (titanBlock.getTitanID().equals(LumberjackBlock.titanID))
        {
            LumberjackBlock lumberjackBlock = LumberjackBlock.convert(titanBlock);
            if (lumberjackBlock != null) lumberjackBlock.setup();
        }

    }

    @Override
    public void onBlockBreak(TitanBlockEvent event) {

    }
}
