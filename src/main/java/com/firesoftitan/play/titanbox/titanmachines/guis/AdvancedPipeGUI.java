package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.blocks.JunctionBoxBlock;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
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

    public AdvancedPipeGUI(Player player, Location location, Location chest) {
        this.location = location.clone();
        this.chest = chest.clone();
        this.player = player;
        this.group = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroup(this.location);
        this.connections = PipesManager.getInstant(PipeTypeEnum.COPPER).getConnections(this.location);
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
        int getter = -1;
        SelectorGUI s = select.get(player.getUniqueId());
        if (s != null)
        {
            getter = s.getGetter();
        }
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
                PipeChestFilterTypeEnum type = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilterType(chest, group, slot);
                ItemStack chestSettingsFilter = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilter(chest, group, slot);
                if (TitanMachines.itemStackTool.isEmpty(chestSettingsFilter)) {
                    chestSettingsFilter = new ItemStack(Material.DIRT);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Press Scan Book, to set item.");

                }
                if (type == PipeChestFilterTypeEnum.DISABLED) {
                    item = new ItemStack(Material.BARRIER);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Left Click to change");
                }
                if (type == PipeChestFilterTypeEnum.ALL) {
                    item = new ItemStack(Material.NETHER_STAR);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Left Click to change");
                }
                if (type == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                    item = chestSettingsFilter;
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "" + ChatColor.UNDERLINE + "-----------------");
                }
                if (type == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                    item = new ItemStack(chestSettingsFilter.getType());

                }
                if (getter == slot)
                {
                    item = new ItemStack(Material.CLOCK);
                    if (s.getPipeChestFilterType() == PipeChestFilterTypeEnum.MATERIAL_ONLY) item = new ItemStack(Material.COMPASS);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Click Item To set");
                    PipeChestFilterTypeEnum pipeChestFilterType = s.getPipeChestFilterType();
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.YELLOW + "Type: " + ChatColor.GREEN + pipeChestFilterType.getCaption(), ChatColor.WHITE + "Click Item in your inventory, now!", ChatColor.WHITE + "Right Click here to change type", ChatColor.WHITE + "Left Click to cancel");
                }
                else {
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.YELLOW + "Filter: " + ChatColor.GREEN + type.getCaption(), ChatColor.WHITE + "Left Click to Change");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "Right Click to set Item from your Inventory");
                }
                item = TitanMachines.nbtTool.set(item, "button", 1);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", this.chest);
                item = TitanMachines.nbtTool.set(item, "group", group);
                item = TitanMachines.nbtTool.set(item, "slot", slot);
                this.inventory.setItem(slot, item.clone());
            }
        }
        else if (state instanceof Container || TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, chest))
        {
            Inventory containerInventory;
            int defaultSize = 9;
            if (TitanBlockManager.isTitanBlock(JunctionBoxBlock.titanID, chest))
            {
                TitanBlock titanBlock = TitanBlockManager.getTitanBlock(JunctionBoxBlock.titanID, chest);
                if (titanBlock != null && titanBlock.getTitanID().equals(JunctionBoxBlock.titanID)) {
                    JunctionBoxBlock junctionBoxBlock = JunctionBoxBlock.convert(titanBlock);
                    BlockFace face = chest.getBlock().getFace(location.getBlock());
                    if (junctionBoxBlock == null || face == null) return false;
                    containerInventory = junctionBoxBlock.getInventory(face);
                    defaultSize = containerInventory.getSize();
                }else return false;
            }else if (state instanceof  Container container){
                containerInventory = container.getInventory();
            }
            else return false;
            if (containerInventory.getSize() > 26) {
                this.inventory = Bukkit.createInventory(null, containerInventory.getSize(), AdvancedPipeGUI.name);
            }
            else
            {
                this.inventory = Bukkit.createInventory(null, defaultSize, AdvancedPipeGUI.name);
            }

            for (int i = 0; i < inventory.getSize(); i++)
            {
                ItemStack item = new ItemStack(Material.BARRIER);
                if (i < containerInventory.getSize())
                {
                    PipeChestFilterTypeEnum type = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilterType(chest, group, i);
                    ItemStack chestSettingsFilter = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilter(chest, group, i);
                    if (TitanMachines.itemStackTool.isEmpty(chestSettingsFilter) || type == PipeChestFilterTypeEnum.DISABLED) {
                        item = new ItemStack(Material.BARRIER);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Left Click to Change");
                    }
                    if (type == PipeChestFilterTypeEnum.ALL) {
                        item = new ItemStack(Material.NETHER_STAR);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Left Click to Change");
                    }
                    if (type == PipeChestFilterTypeEnum.TOTAL_MATCH) {
                        item = chestSettingsFilter;
                    }
                    if (type == PipeChestFilterTypeEnum.MATERIAL_ONLY) {
                        item = new ItemStack(chestSettingsFilter.getType());
                    }
                    if (item == null)  item = new ItemStack(Material.BARRIER);
                    if (getter == i)
                    {
                        item = new ItemStack(Material.CLOCK);
                        if (s.getPipeChestFilterType() == PipeChestFilterTypeEnum.MATERIAL_ONLY) item = new ItemStack(Material.COMPASS);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Click Item To set");
                        PipeChestFilterTypeEnum pipeChestFilterType = s.getPipeChestFilterType();
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.YELLOW + "Type: " + ChatColor.GREEN + pipeChestFilterType.getCaption(), ChatColor.WHITE + "Click Item in your inventory, now!", ChatColor.WHITE + "Right Click here to change type", ChatColor.WHITE + "Left Click to cancel");
                    }
                    else {
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.YELLOW + "Filter: " + ChatColor.GREEN + type.getCaption());
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "Right Click to set Item from your Inventory");
                    }
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

                PipeChestFilterTypeEnum totalMatch = PipeChestFilterTypeEnum.TOTAL_MATCH;
                if (s.getGetter() > 1) totalMatch = PipeChestFilterTypeEnum.MATERIAL_ONLY;
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(s.getConnection(), s.getGroup(), s.getGetter(), totalMatch);
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilter(s.getConnection(), s.getGroup(), s.getGetter(), filter);
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
            ItemStack itemStack = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilter(connection, group, slot);
            PipeChestFilterTypeEnum chestSettingsType = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsFilterType(connection, group, slot);
            PipeChestFilterTypeEnum nextSetting = PipeChestFilterTypeEnum.getPipeChestType(chestSettingsType.getValue() + 1);
            if (TitanMachines.itemStackTool.isEmpty(itemStack) )
            {
                if (nextSetting == PipeChestFilterTypeEnum.MATERIAL_ONLY || nextSetting == PipeChestFilterTypeEnum.TOTAL_MATCH)
                {
                    nextSetting = PipeChestFilterTypeEnum.DISABLED;
                }
            }
            PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(connection, group, slot, nextSetting);
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, location, connection);
            advancedPipeGUI.open();

        }
        else {
            if (button == 11 || button == 1) {
                AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, location, connection);
                if (s == null) {
                    s = new SelectorGUI(location, connection, group);
                }
                if (s.getPipeChestFilterType() == null || s.getPipeChestFilterType() == PipeChestFilterTypeEnum.MATERIAL_ONLY) s.setPipeChestFilterType(PipeChestFilterTypeEnum.TOTAL_MATCH);
                else s.setPipeChestFilterType(PipeChestFilterTypeEnum.MATERIAL_ONLY);
                s.setGetter(slot);
                select.put(player.getUniqueId(), s);
                if (button == 1) select.remove(player.getUniqueId());
                advancedPipeGUI.open();
            }
        }
    }
}
