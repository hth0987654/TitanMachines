package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterType;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.JunctionBoxManager;
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

import java.util.HashMap;
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
            for (int slot: ContainerManager.getInventorySlots(getLocation(), chest))
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
        else if (state instanceof Container || JunctionBoxManager.instance.isTitanBlock(chest))
        {
            Inventory containerInventory;
            int defaultSize = 9;
            if (JunctionBoxManager.instance.isTitanBlock(chest))
            {
                TitanBlock titanBlock = JunctionBoxManager.instance.getTitanBlock(chest);
                if (titanBlock != null && titanBlock.getTitanID().equals(JunctionBoxBlock.titanID)) {
                    JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
                    containerInventory = junctionBoxBlock.getInventory(chest.getBlock().getFace(location.getBlock()));
                    defaultSize = containerInventory.getSize();
                }else return false;
            }else {
                Container container  = (Container) (state);
                containerInventory = container.getInventory();
            }
            if (containerInventory.getSize() > 26) {
                this.inventory = Bukkit.createInventory(null, containerInventory.getSize(), AdvancedPipeGUI.name);
            }
            else
            {
                this.inventory = Bukkit.createInventory(null, defaultSize, AdvancedPipeGUI.name);
            }
            int getter = -1;
            SelectorGUI s = select.get(player.getUniqueId());
            if (s != null)
            {
                getter = s.getGetter();
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
                    }
                    if (type == PipeChestFilterType.ALL) {
                        item = new ItemStack(Material.NETHER_STAR);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click to change");
                    }
                    if (type == PipeChestFilterType.TOTAL_MATCH) {
                        item = chestSettingsFilter;
                    }
                    if (type == PipeChestFilterType.MATERIAL_ONLY) {
                        item = new ItemStack(chestSettingsFilter.getType());
                    }
                    if (item == null)  item = new ItemStack(Material.BARRIER);
                    if (getter == i)
                    {
                        item = new ItemStack(Material.CLOCK);
                        if (s.getPipeChestFilterType() == PipeChestFilterType.MATERIAL_ONLY) item = new ItemStack(Material.COMPASS);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click Item To set");
                        PipeChestFilterType pipeChestFilterType = s.getPipeChestFilterType();
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.AQUA + "Type: " + ChatColor.GOLD + pipeChestFilterType.getCaption(), ChatColor.GRAY + "Click Item in your inventory, now!", ChatColor.GRAY + "Right Click here to change type", ChatColor.GRAY + "Left Click to cancel");
                    }

                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "Filter: " + ChatColor.YELLOW + type.getCaption(), ChatColor.DARK_PURPLE + "Click to Change");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "---------------------", ChatColor.AQUA + "Click to set Item from your Inventory");
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
    public static void setTypeSelect(Player player, ItemStack item)
    {
        if (!select.containsKey(player.getUniqueId())) return;
        SelectorGUI s = select.get(player.getUniqueId());
        if (s.getGetter() > -1) {
            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                ItemStack filter = item.clone();
                filter.setAmount(1);

                PipeChestFilterType totalMatch = PipeChestFilterType.TOTAL_MATCH;
                if (s.getGetter() > 1) totalMatch = PipeChestFilterType.MATERIAL_ONLY;
                PipesManager.instance.setChestSettingsFilterType(s.getConnection(), s.getGroup(), s.getGetter(), totalMatch);
                PipesManager.instance.setChestSettingsFilter(s.getConnection(), s.getGroup(), s.getGetter(), filter);
                select.remove(player.getUniqueId());
            }

            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, s.getLocation(), s.getConnection());
            advancedPipeGUI.open();
        }
    }
    private static final HashMap<UUID, SelectorGUI> select = new HashMap<UUID, SelectorGUI>();
    public static void onClickButtonEvent(Player player, Integer button, Location location, Location connection, UUID group, int slot) {
        SelectorGUI s = select.get(player.getUniqueId());
        if (button == 1 && (s == null || s.getGetter() != slot))
        {
            ItemStack itemStack = PipesManager.instance.getChestSettingsFilter(connection, group, slot);
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
        else {
            if (button == 11 || button == 1) {
                AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, location, connection);
                if (s == null) {
                    s = new SelectorGUI(location, connection, group);
                }
                if (s.getPipeChestFilterType() == null || s.getPipeChestFilterType() == PipeChestFilterType.MATERIAL_ONLY) s.setPipeChestFilterType(PipeChestFilterType.TOTAL_MATCH);
                else s.setPipeChestFilterType(PipeChestFilterType.MATERIAL_ONLY);
                s.setGetter(slot);
                select.put(player.getUniqueId(), s);
                if (button == 1) select.remove(player.getUniqueId());
                advancedPipeGUI.open();
            }
        }
    }
}
