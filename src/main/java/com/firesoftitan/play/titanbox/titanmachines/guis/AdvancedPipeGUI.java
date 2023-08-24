package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterType;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdvancedPipeGUI {
    private Inventory inventory;
    public static String name = "Advanced Pipe Connection Config";
    @SuppressWarnings("FieldCanBeLocal")
    private final int size = 54;
    private final Location location;
    private final Location chest;
    private final UUID group;
    private final List<Location> connections;
    private final Player player;

    public AdvancedPipeGUI( Player player, Location location, Location chest) {
        this.location = location.clone();
        this.chest = chest.clone();
        this.player = player;
        this.group = PipesManager.instance.getGroup(this.location);
        this.connections = PipesManager.instance.getConnections(this.location);
        this.inventory = Bukkit.createInventory(null, size, AdvancedPipeGUI.name);
    }
    public Location getLocation() {
        return location.clone();
    }

    @SuppressWarnings("unused")
    public UUID getGroup() {
        return group;
    }

    @SuppressWarnings("unused")
    public List<Location> getConnections() {
        return connections;
    }

    @SuppressWarnings("unused")
    public Player getPlayer() {
        return player;
    }

    public boolean render()
    {
        BlockState state = chest.getBlock().getState();
        if (SlimefunSupport.instance.isSupported(chest)) {
            Set<Integer> gui = SlimefunSupport.instance.getPresetSlots(chest);
            Set<Integer> inv = SlimefunSupport.instance.getInventorySlots(chest);
            this.inventory = Bukkit.createInventory(null, gui.size() + inv.size(), AdvancedPipeGUI.name);
            for(int slot: gui)
            {
                ItemStack itemInSlot = SlimefunSupport.instance.getItemInSlot(chest, slot);
                inventory.setItem(slot, itemInSlot);
            }
            ItemStack item = new ItemStack(Material.BARRIER);
            for (int slot: ContainerManager.getInventorySlots(chest))
            {
                PipeChestFilterType type = PipesManager.instance.getChestSettingsFilterType(chest, group, slot);
                ItemStack chestSettingsFilter = PipesManager.instance.getChestSettingsFilter(chest, group, slot);
                if (TitanMachines.itemStackTool.isEmpty(chestSettingsFilter)) {
                    chestSettingsFilter = new ItemStack(Material.DIRT);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Press Scan Book, to set item.");

                }
                if (type == PipeChestFilterType.DISABLED) {
                    item = new ItemStack(Material.BARRIER);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click to change");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.DARK_PURPLE + "Click to Change");
                }
                if (type == PipeChestFilterType.ALL) {
                    item = new ItemStack(Material.NETHER_STAR);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click to change");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "For more options place, item(s)", ChatColor.WHITE + "then press scan button bellow.", ChatColor.DARK_PURPLE + "Click to Change");
                }
                if (type == PipeChestFilterType.TOTAL_MATCH) {
                    item = chestSettingsFilter;
                }
                if (type == PipeChestFilterType.MATERIAL_ONLY) {
                    item = new ItemStack(chestSettingsFilter.getType());
                }

                item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "---------------------", ChatColor.GRAY + "Filter: " + ChatColor.YELLOW + type.getCaption(), ChatColor.DARK_PURPLE + "Click to Change");
                item = TitanMachines.nbtTool.set(item, "button", 1);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", this.chest);
                item = TitanMachines.nbtTool.set(item, "group", group);
                item = TitanMachines.nbtTool.set(item, "slot", slot);
                this.inventory.setItem(slot, item.clone());
            }
        }
        else if (state instanceof Container)
        {
            Container container = (Container) (state);
            Inventory containerInventory = container.getInventory();
            if (containerInventory.getSize() > 26) {
                this.inventory = Bukkit.createInventory(null, containerInventory.getSize(), AdvancedPipeGUI.name);
            }
            else
            {
                this.inventory = Bukkit.createInventory(null, 9, AdvancedPipeGUI.name);
            }
                for (int i = 0; i < inventory.getSize(); i++)
                {
                    ItemStack item = new ItemStack(Material.BARRIER);
                    if (i < containerInventory.getSize())
                    {
                        PipeChestFilterType type = PipesManager.instance.getChestSettingsFilterType(chest, group, i);
                        ItemStack chestSettingsFilter = PipesManager.instance.getChestSettingsFilter(chest, group, i);
                        if (TitanMachines.itemStackTool.isEmpty(chestSettingsFilter) || type == PipeChestFilterType.DISABLED) {
                            item = new ItemStack(Material.BARRIER);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click to change");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.DARK_PURPLE + "Click to Change");
                        }
                        if (type == PipeChestFilterType.ALL) {
                            item = new ItemStack(Material.NETHER_STAR);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click to change");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "For more options place, item(s)", ChatColor.WHITE + "then press scan button bellow.", ChatColor.DARK_PURPLE + "Click to Change");
                        }
                        if (type == PipeChestFilterType.TOTAL_MATCH) {
                            item = chestSettingsFilter;
                        }
                        if (type == PipeChestFilterType.MATERIAL_ONLY) {
                            item = new ItemStack(chestSettingsFilter.getType());
                        }
                        if (item == null)  item = new ItemStack(Material.BARRIER);
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "---------------------", ChatColor.GRAY + "Filter: " + ChatColor.YELLOW + type.getCaption(), ChatColor.DARK_PURPLE + "Click to Change");
                        item = TitanMachines.nbtTool.set(item, "button", 1);
                        item = TitanMachines.nbtTool.set(item, "location", this.location);
                        item = TitanMachines.nbtTool.set(item, "connection", this.chest);
                        item = TitanMachines.nbtTool.set(item, "group", group);
                        item = TitanMachines.nbtTool.set(item, "slot", i);
                    }
                    else
                    {
                        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    }

                    this.inventory.setItem(i, item.clone());
                }
        }
        else
        {
            return false;
        }
        return true;

    }
    public void open()
    {
        if (this.render()) this.player.openInventory(this.inventory);
    }
    public static void onClickButtonEvent(Player player, Integer button, Location location, Location connection, UUID group, int slot) {
        if (button == 1)
        {
            ItemStack itemStack = PipesManager.instance.getChestSettingsFilter(connection, group, slot);
            //if (!TitanMachines.itemStackTool.isEmpty(itemStack))
            {
                PipeChestFilterType chestSettingsType = PipesManager.instance.getChestSettingsFilterType(connection, group, slot);
                PipeChestFilterType nextSetting = PipeChestFilterType.getPipeChestType(chestSettingsType.getValue() + 1);
                if (TitanMachines.itemStackTool.isEmpty(itemStack) )
                {
                    if (nextSetting == PipeChestFilterType.MATERIAL_ONLY || nextSetting == PipeChestFilterType.TOTAL_MATCH)
                    {
                        nextSetting = PipeChestFilterType.DISABLED;
                    }
                }
                PipesManager.instance.setChestSettingsFilterType(connection, group, slot, nextSetting);
                AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, location, connection);
                advancedPipeGUI.open();
            }
        }
    }
}
