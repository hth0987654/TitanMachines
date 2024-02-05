package com.firesoftitan.play.titanbox.titanmachines.listeners;

import com.firesoftitan.play.titanbox.libs.TitanBoxLibs;
import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.events.TitanBlockEvent;
import com.firesoftitan.play.titanbox.libs.events.TitanBlockInteractEvent;
import com.firesoftitan.play.titanbox.libs.listeners.TitanBlockListener;
import com.firesoftitan.play.titanbox.libs.tools.LibsProtectionTool;
import com.firesoftitan.play.titanbox.libs.tools.Tools;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.LumberjackBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.MobKillerBlock;
import com.firesoftitan.play.titanbox.titanmachines.blocks.PipeBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.guis.JunctionBoxGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.PipeConnectionGUI;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TitanMachineBlockListener extends TitanBlockListener {
    @Override
    public void onPlayerInteract(TitanBlockInteractEvent event) {
        TitanBlock titanBlock = event.titanBlock();
        Player player = event.player();
        Location location = event.location();
        Action action = event.action();
        ItemStack itemStack = event.item();
        boolean isAllowed = LibsProtectionTool.canPlaceBlock(player, location);
        if (titanBlock.getTitanID().equals(MobKillerBlock.titanID) && isAllowed)
        {
            if (action == Action.RIGHT_CLICK_BLOCK) {
                MobKillerBlock convert = MobKillerBlock.convert(titanBlock);
                if (convert != null) {
                    double amountDMG = 0.5f;
                    AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                    if (attribute != null) {
                        amountDMG = attribute.getValue() / 2f; //this is the total no need to add.
                        for (AttributeModifier attributeModifier : attribute.getModifiers()) {
                            amountDMG = amountDMG + attributeModifier.getAmount() / 2f;
                        }
                    }
                    convert.setDamage(amountDMG);
                    TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.AQUA + "Damages is not set at " +ChatColor.WHITE + amountDMG *2);
                    TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.GRAY + "While holding weapon right click block damage will be set");
                    TitanMachines.messageTool.sendMessagePlayer(player, ChatColor.GRAY + "equal to that weapons and players combined damage");
                }

            }
        }
        if (titanBlock.getTitanID().equals(PipeBlock.titanID) && isAllowed)
        {
            PipesManager.getInstant(PipeTypeEnum.COPPER).rescanPipeOrientation(location);
            if (action == Action.LEFT_CLICK_BLOCK && !TitanMachines.itemStackTool.isEmpty(itemStack))
            {
                if (itemStack.getType() == Material.DIAMOND_PICKAXE || itemStack.getType() == Material.NETHERITE_PICKAXE
                        || itemStack.getType() == Material.GOLDEN_PICKAXE  || itemStack.getType() == Material.IRON_PICKAXE
                        || itemStack.getType() == Material.STONE_PICKAXE  || itemStack.getType() == Material.WOODEN_PICKAXE)
                {
                    UUID group = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroup(location);
                    PipesManager.getInstant(PipeTypeEnum.COPPER).remove(location);
                    TitanBlock.breakBlock(titanBlock);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PipesManager.getInstant(PipeTypeEnum.COPPER).checkSurroundings(location);
                            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
                        }
                    }.runTaskLater(TitanMachines.instants, 1);
                }
            }
            if (action == Action.RIGHT_CLICK_BLOCK && isAllowed) {
                if (PipesManager.getInstant(PipeTypeEnum.COPPER).isPipe(location))
                {
                    PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, location);
                    pipeConnectionGUI.open();
                }
            }
        }
        if (titanBlock.getTitanID().equals(JunctionBoxBlock.titanID) && isAllowed)
        {
            JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
            if (junctionBoxBlock != null)
            {
                JunctionBoxGUI junctionBoxGUI = new JunctionBoxGUI(player, location);
                junctionBoxGUI.open();
            }
        }
        if (titanBlock.getTitanID().equals(LumberjackBlock.titanID) && isAllowed)
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

                    UUID group = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroup(location);
                    PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
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
