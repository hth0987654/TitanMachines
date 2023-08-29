package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterType;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestType;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PipeConnectionGUI {
    private final Inventory inventory;
    public static String name = "Pipe Connection Config";
    private final int size = 45;//36;
    private final Location location;
    private final UUID group;
    private final List<Location> connections;
    private final Player player;
    public PipeConnectionGUI(Player player, Location chest) {
        this.location = chest.clone();
        this.player = player;
        this.group = PipesManager.instance.getGroup(this.location);
        this.connections = PipesManager.instance.getConnections(this.location);
        this.inventory = Bukkit.createInventory(null, size, PipeConnectionGUI.name);
    }

    public Location getLocation() {
        return location.clone();
    }

    public UUID getGroup() {
        return group;
    }

    public List<Location> getConnections() {
        return connections;
    }

    public Player getPlayer() {
        return player;
    }

    public void render()
    {
        ItemStack borderItem = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        for(int i = 0; i < size; i++)
        {
            this.inventory.setItem(i, borderItem.clone());
        }
        ItemStack item = null;

        item = new ItemStack(Material.MANGROVE_SIGN);
        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Pipe Info");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.YELLOW + "Pipe Line ID: " + ChatColor.AQUA + this.group);
        lore.add(ChatColor.YELLOW + "Pipe Line Segments: " + ChatColor.AQUA + PipesManager.instance.getGroupSize(this.group));
        for(Location location: connections)
        {
            if (PipesManager.instance.isPipe(location))
            {
                lore.add(ChatColor.GRAY + "Connected to pipe" + ChatColor.YELLOW +" @ " + ChatColor.AQUA + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
            else if (PipesManager.instance.isChestConnected(location))
            {
                lore.add(ChatColor.GRAY + "Connected to container" + ChatColor.YELLOW +" @ " + ChatColor.AQUA + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
            else
            {
                lore.add(ChatColor.GRAY + "Connected to Unknown" + ChatColor.YELLOW +" @ " + ChatColor.AQUA + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
        }
        item = TitanMachines.itemStackTool.addLore(item,  lore);
        item = TitanMachines.nbtTool.set(item, "button", -1);
        item = TitanMachines.nbtTool.set(item, "location", this.location);
        item = TitanMachines.nbtTool.set(item, "group", group);
        this.inventory.setItem(0, item.clone());

        for(int i = 1; i < connections.size() + 1; i++)
        {
            Location location = connections.get(i - 1);
            item = new ItemStack(Material.BARRIER);
            if (PipesManager.instance.isPipe(location))
            {
                item = TitanMachines.instants.getPipe();
                item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Connected to pipe" + ChatColor.YELLOW +" @ " + ChatColor.AQUA + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                item = TitanMachines.itemStackTool.addLore(item,  "No Settings");
                item = TitanMachines.nbtTool.set(item, "button", i);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", location);
                item = TitanMachines.nbtTool.set(item, "group", group);
                this.inventory.setItem(i, item.clone());
            }else if (PipesManager.instance.isChestConnected(location))
            {
                Material material = location.getBlock().getType();
                item = new ItemStack(material);
                item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Connected to container" + ChatColor.YELLOW +" @ " + ChatColor.AQUA + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                PipeChestType chestSettingsType = PipesManager.instance.getChestSettingsType(location, group);
                item = TitanMachines.itemStackTool.addLore(item,  ChatColor.YELLOW + "Type: " + ChatColor.AQUA + chestSettingsType.getCaption(),ChatColor.DARK_PURPLE + "Left-Click: " + ChatColor.WHITE + "Change connection type",ChatColor.DARK_PURPLE + "Right-Click: " + ChatColor.WHITE + "Open Advanced Inventory Settings");
                item = TitanMachines.nbtTool.set(item, "button", 0);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", location);
                item = TitanMachines.nbtTool.set(item, "group", group);
                this.inventory.setItem(i, item.clone());

                if (chestSettingsType == PipeChestType.OVERFLOW ||chestSettingsType == PipeChestType.CHEST_IN || chestSettingsType == PipeChestType.CHEST_OUT) {

                    if (chestSettingsType == PipeChestType.OVERFLOW || chestSettingsType == PipeChestType.CHEST_IN) {
                        item = new ItemStack(Material.BOOK);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Scan Container");
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "This will scan container for filter");
                        item = TitanMachines.nbtTool.set(item, "button", 2);
                        item = TitanMachines.nbtTool.set(item, "location", this.location);
                        item = TitanMachines.nbtTool.set(item, "connection", location);
                        item = TitanMachines.nbtTool.set(item, "group", group);
                        this.inventory.setItem(i + 9, item.clone());

                        int getI = 0;
                        SelectorGUI s = select.get(player.getUniqueId());
                        if (s != null) {
                            getI = s.getGetter();
                        }
                        if (getI == 1) {
                            item = new ItemStack(Material.CLOCK);
                            if (s.getPipeChestFilterType() == PipeChestFilterType.MATERIAL_ONLY) item = new ItemStack(Material.COMPASS);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Click Item To set");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.AQUA + "Type: " + ChatColor.GOLD + s.getPipeChestFilterType().getCaption(), ChatColor.GRAY + "Click Item in your inventory, now!", ChatColor.GRAY + "Click here to change type", ChatColor.GRAY + "Right Click to cancel");
                        }
                        else {
                            item = new ItemStack(Material.ANVIL);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Maker All, Item");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "This will all slots to the next item you click in your inventory.");
                        }

                        item = TitanMachines.nbtTool.set(item, "button", 5);
                        item = TitanMachines.nbtTool.set(item, "location", this.location);
                        item = TitanMachines.nbtTool.set(item, "connection", location);
                        item = TitanMachines.nbtTool.set(item, "group", group);

                        this.inventory.setItem(i + 9*4, item.clone());
                    }

                    item = new ItemStack(Material.NETHER_STAR);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Maker All, No Filter");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "This will all slots: " + ChatColor.LIGHT_PURPLE + "No Filter");
                    item = TitanMachines.nbtTool.set(item, "button", 3);
                    item = TitanMachines.nbtTool.set(item, "location", this.location);
                    item = TitanMachines.nbtTool.set(item, "connection", location);
                    item = TitanMachines.nbtTool.set(item, "group", group);
                    this.inventory.setItem(i + 9*2, item.clone());

                    item = new ItemStack(Material.BARRIER);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.GRAY + "Maker All, Disabled");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.GRAY + "This will all slots: " + ChatColor.LIGHT_PURPLE + "Disabled");
                    item = TitanMachines.nbtTool.set(item, "button", 4);
                    item = TitanMachines.nbtTool.set(item, "location", this.location);
                    item = TitanMachines.nbtTool.set(item, "connection", location);
                    item = TitanMachines.nbtTool.set(item, "group", group);
                    this.inventory.setItem(i + 9*3, item.clone());
                }

            }





        }

    }


    public void open()
    {
        this.render();
        this.player.openInventory(this.inventory);
    }

    private static final HashMap<UUID, SelectorGUI> select = new HashMap<UUID, SelectorGUI>();
    public static void setTypeSelect(Player player, ItemStack item)
    {
        if (!select.containsKey(player.getUniqueId())) return;
        SelectorGUI s = select.get(player.getUniqueId());

        PipesManager.instance.clearChestSettingsFilterType(s.getConnection(), s.getGroup());
        for (int i : ContainerManager.getInventorySlots(s.getLocation(), s.getConnection())) {
            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                ItemStack filter = item.clone();
                filter.setAmount(1);

                PipeChestFilterType totalMatch = s.getPipeChestFilterType();
                PipesManager.instance.setChestSettingsFilterType(s.getConnection(), s.getGroup(), i, totalMatch);
                PipesManager.instance.setChestSettingsFilter(s.getConnection(), s.getGroup(), i, filter);
                select.remove(player.getUniqueId());
            }
        }
        AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, s.getLocation(), s.getConnection());
        advancedPipeGUI.open();

    }

    public static void onClickButtonEvent(Player player, Integer button, Location pipe, Location chest, UUID group, int slot) {
        Block block = pipe.getBlock();
        BlockState state = block.getState();
        if (button == 10)
        {
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 0)
        {

            PipeChestType chestSettingsType = PipesManager.instance.getChestSettingsType(chest, group);
            PipeChestType nextSetting = PipeChestType.getPipeChestType(chestSettingsType.getValue() + 1);
            PipesManager.instance.setChestSettingsType(chest, group, nextSetting);
            PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, pipe);
            pipeConnectionGUI.open();
            PipesManager.instance.rescanPipeOrientation(pipe);

        }
        if (button == 2 || button == 12)
        {
            PipesManager.instance.clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                ItemStack item = ContainerManager.getInventorySlot(pipe, chest, i);
                if (!TitanMachines.itemStackTool.isEmpty(item))
                {
                    ItemStack filter = item.clone();
                    filter.setAmount(1);
                    PipesManager.instance.setChestSettingsFilterType(chest, group, i, PipeChestFilterType.TOTAL_MATCH);
                    PipesManager.instance.setChestSettingsFilter(chest, group, i, filter);
                }
                else
                {
                    PipesManager.instance.setChestSettingsFilterType(chest, group, i, PipeChestFilterType.ALL);
                }
            }
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 3 || button == 13)
        {
            PipesManager.instance.clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                PipesManager.instance.setChestSettingsFilterType(chest, group, i, PipeChestFilterType.ALL);
            }
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 4 || button == 14)
        {
            PipesManager.instance.clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                PipesManager.instance.setChestSettingsFilterType(chest, group, i, PipeChestFilterType.DISABLED);
            }
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 5 || button == 15)
        {
            PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, pipe);
            SelectorGUI s = select.get(player.getUniqueId());
            if (s == null)
            {
                s = new SelectorGUI(pipe, chest, group);
            }
            if (s.getPipeChestFilterType() == null || s.getPipeChestFilterType() == PipeChestFilterType.MATERIAL_ONLY) s.setPipeChestFilterType(PipeChestFilterType.TOTAL_MATCH);
            else s.setPipeChestFilterType(PipeChestFilterType.MATERIAL_ONLY);
            s.setGetter(1);
            select.put(player.getUniqueId(), s);
            if (button == 15) select.remove(player.getUniqueId());
            pipeConnectionGUI.open();


        }

    }
}
