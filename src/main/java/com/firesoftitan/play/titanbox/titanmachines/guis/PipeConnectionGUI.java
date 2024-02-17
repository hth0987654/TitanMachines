package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import com.firesoftitan.play.titanbox.titanmachines.runnables.PipeRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
        this.group = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroup(this.location);
        this.connections = PipesManager.getInstant(PipeTypeEnum.COPPER).getConnections(this.location);
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
        ItemStack item;

        updateSignInfo();

        for(int i = 1; i < connections.size() + 1; i++)
        {
            Location location = connections.get(i - 1);
            item = new ItemStack(Material.BARRIER);
            if (PipesManager.getInstant(PipeTypeEnum.COPPER).isPipe(location))
            {
                item = TitanMachines.instants.getPipe();
                item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Connected to pipe" + ChatColor.YELLOW +" @ " + ChatColor.WHITE + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                item = TitanMachines.itemStackTool.addLore(item,  "No Settings");
                item = TitanMachines.nbtTool.set(item, "button", i);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", location);
                item = TitanMachines.nbtTool.set(item, "group", group);
                this.inventory.setItem(i, item.clone());
            }else if (PipesManager.getInstant(PipeTypeEnum.COPPER).isChestConnected(location))
            {
                Material material = location.getBlock().getType();
                if (material == Material.PLAYER_WALL_HEAD) material = Material.PLAYER_HEAD;
                item = new ItemStack(material);
                if (material == Material.PLAYER_HEAD)
                {
                    String skullTexture = TitanMachines.tools.getSkullTool().getSkullTexture(location.getBlock());
                    item = TitanMachines.tools.getSkullTool().getSkull(skullTexture);

                }
                if (TitanMachines.itemStackTool.isEmpty(item)) item = new ItemStack(Material.BARRIER);
                item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Connected to container" + ChatColor.YELLOW +" @ " + ChatColor.WHITE + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                PipeChestTypeEnum chestSettingsType = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsType(location, group);
                item = TitanMachines.itemStackTool.addLore(item,  ChatColor.YELLOW + "Type: " + ChatColor.GREEN + chestSettingsType.getCaption(),ChatColor.AQUA + "Left-Click: " + ChatColor.WHITE + "Change connection type",ChatColor.AQUA + "Right-Click: " + ChatColor.WHITE + "Open Advanced Inventory Settings");
                item = TitanMachines.nbtTool.set(item, "button", 0);
                item = TitanMachines.nbtTool.set(item, "location", this.location);
                item = TitanMachines.nbtTool.set(item, "connection", location);
                item = TitanMachines.nbtTool.set(item, "group", group);
                this.inventory.setItem(i, item.clone());

                if (chestSettingsType == PipeChestTypeEnum.CHEST_IN || chestSettingsType == PipeChestTypeEnum.CHEST_OUT) {

                    if (chestSettingsType == PipeChestTypeEnum.CHEST_IN) {
                        item = new ItemStack(Material.BOOK);
                        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Scan Container");
                        item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "This will scan container for filter");
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
                            if (s.getPipeChestFilterType() == PipeChestFilterTypeEnum.MATERIAL_ONLY) item = new ItemStack(Material.COMPASS);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Click Item To set");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.YELLOW + "Type: " + ChatColor.GREEN + s.getPipeChestFilterType().getCaption(), ChatColor.WHITE + "Click Item in your inventory, now!", ChatColor.WHITE + "Click here to change type", ChatColor.WHITE + "Right Click to cancel");
                        }
                        else {
                            item = new ItemStack(Material.ANVIL);
                            item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Maker All, Item");
                            item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "This will all slots to the next item you click in your inventory.");
                        }

                        item = TitanMachines.nbtTool.set(item, "button", 5);
                        item = TitanMachines.nbtTool.set(item, "location", this.location);
                        item = TitanMachines.nbtTool.set(item, "connection", location);
                        item = TitanMachines.nbtTool.set(item, "group", group);

                        this.inventory.setItem(i + 9*4, item.clone());
                    }

                    item = new ItemStack(Material.NETHER_STAR);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Maker All, No Filter");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "Set All Slots: " + ChatColor.YELLOW + "Filter: " + ChatColor.GREEN + "No Filter");
                    item = TitanMachines.nbtTool.set(item, "button", 3);
                    item = TitanMachines.nbtTool.set(item, "location", this.location);
                    item = TitanMachines.nbtTool.set(item, "connection", location);
                    item = TitanMachines.nbtTool.set(item, "group", group);
                    this.inventory.setItem(i + 9*2, item.clone());

                    item = new ItemStack(Material.BARRIER);
                    item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Maker All, Disabled");
                    item = TitanMachines.itemStackTool.addLore(item, ChatColor.WHITE + "Set All Slots: " + ChatColor.YELLOW + "Filter: " + ChatColor.GREEN + "Disabled");
                    item = TitanMachines.nbtTool.set(item, "button", 4);
                    item = TitanMachines.nbtTool.set(item, "location", this.location);
                    item = TitanMachines.nbtTool.set(item, "connection", location);
                    item = TitanMachines.nbtTool.set(item, "group", group);
                    this.inventory.setItem(i + 9*3, item.clone());
                }

            }





        }

    }

    public void updateSignInfo() {
        ItemStack item;
        item = new ItemStack(Material.MANGROVE_SIGN);
        item = TitanMachines.itemStackTool.changeName(item, ChatColor.AQUA + "Pipe Info");
        List<String> lore = new ArrayList<String>();
        int threadMax = PipeRunnable.instance.getMax();
        lore.add(ChatColor.AQUA + "Pipe Line ID: " + ChatColor.WHITE + this.group);
        lore.add(ChatColor.AQUA + "Pipe Line Segments: " + ChatColor.WHITE + PipesManager.getInstant(PipeTypeEnum.COPPER).getGroupSize(this.group));
        for(Location location: connections)
        {
            if (PipesManager.getInstant(PipeTypeEnum.COPPER).isPipe(location))
            {
                lore.add(ChatColor.AQUA + "Connected to pipe" + ChatColor.YELLOW +" @ " + ChatColor.WHITE + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
            else if (PipesManager.getInstant(PipeTypeEnum.COPPER).isChestConnected(location))
            {
                lore.add(ChatColor.AQUA + "Connected to container" + ChatColor.YELLOW +" @ " + ChatColor.WHITE + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
            else
            {
                lore.add(ChatColor.AQUA + "Connected to Unknown" + ChatColor.YELLOW +" @ " + ChatColor.WHITE + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
        }

        item = TitanMachines.itemStackTool.addLore(item,  lore);
        item = TitanMachines.nbtTool.set(item, "button", -1);
        item = TitanMachines.nbtTool.set(item, "location", this.location);
        item = TitanMachines.nbtTool.set(item, "group", group);
        this.inventory.setItem(0, item.clone());
        lore.clear();
        item = new ItemStack(Material.BAMBOO_SIGN);
        item = TitanMachines.itemStackTool.changeName(item, ChatColor.GREEN + "Network Info");
        lore.add(ChatColor.GREEN + "Pipe Segments: " + ChatColor.WHITE + PipeRunnable.instance.getNumberGroups());
        String tpsData = TitanMachines.tools.getFormattingTool().formatCommas(PipeRunnable.instance.getTPS());
        if (!PipeRunnable.instance.isTPSReady()) tpsData = "calculating...";
        lore.add(ChatColor.GREEN + "TPS: " + ChatColor.WHITE + tpsData);
        long time = PipeRunnable.instance.getTime(group);
        if (time == 0)
        {
            lore.add(ChatColor.GREEN + "Last Ran: " + ChatColor.WHITE + "never");
        }
        else
        {
            String number = TitanMachines.tools.getFormattingTool().formatTimeFromNow(time);
            lore.add(ChatColor.GREEN + "Last Ran: " + ChatColor.WHITE + number);
        }
        long ltime = PipeRunnable.instance.getLastTimeRan(group);
        if (ltime == 0)
        {
            lore.add(ChatColor.GREEN + "Runs every: " + ChatColor.WHITE + "calculating...");
        }
        else
        {
            ltime /= 1000L;
            lore.add(ChatColor.GREEN + "Runs every: " + ChatColor.WHITE + ltime + " Seconds");
        }
        lore.add(ChatColor.GREEN + "Running Threads: " + ChatColor.WHITE + threadMax + ChatColor.GREEN + "/" +  ChatColor.WHITE + PipeRunnable.instance.getMaxThreads());

        item = TitanMachines.itemStackTool.addLore(item,  lore);
        item = TitanMachines.nbtTool.set(item, "button", -1);
        item = TitanMachines.nbtTool.set(item, "location", this.location);
        item = TitanMachines.nbtTool.set(item, "group", group);
        this.inventory.setItem(9, item.clone());
    }


    public void open()
    {
        this.render();
        this.player.openInventory(this.inventory);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.getOpenInventory().getTitle().equals(PipeConnectionGUI.name))
                {
                    this.cancel();
                    return;
                }
                updateSignInfo();
            }
        }.runTaskTimer(TitanMachines.instants, 20, 1);
    }

    private static final HashMap<UUID, SelectorGUI> select = new HashMap<UUID, SelectorGUI>();
    public static void setTypeSelect(Player player, ItemStack item)
    {
        if (!select.containsKey(player.getUniqueId())) return;
        SelectorGUI s = select.get(player.getUniqueId());

        PipesManager.getInstant(PipeTypeEnum.COPPER).clearChestSettingsFilterType(s.getConnection(), s.getGroup());
        for (int i : ContainerManager.getInventorySlots(s.getLocation(), s.getConnection())) {
            if (!TitanMachines.itemStackTool.isEmpty(item)) {
                ItemStack filter = item.clone();
                filter.setAmount(1);

                PipeChestFilterTypeEnum totalMatch = s.getPipeChestFilterType();
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(s.getConnection(), s.getGroup(), i, totalMatch);
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilter(s.getConnection(), s.getGroup(), i, filter);
                select.remove(player.getUniqueId());
            }
        }
        PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(s.getGroup());
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

            PipeChestTypeEnum chestSettingsType = PipesManager.getInstant(PipeTypeEnum.COPPER).getChestSettingsType(chest, group);
            PipeChestTypeEnum nextSetting = PipeChestTypeEnum.getPipeChestType(chestSettingsType.getValue() + 1);
            PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsType(chest, group, nextSetting);
            PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, pipe);
            pipeConnectionGUI.open();
            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
            PipesManager.getInstant(PipeTypeEnum.COPPER).rescanPipeOrientation(pipe);

        }
        if (button == 2 || button == 12)
        {
            PipesManager.getInstant(PipeTypeEnum.COPPER).clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                ItemStack item = ContainerManager.getInventorySlot(pipe, chest, i);
                if (!TitanMachines.itemStackTool.isEmpty(item))
                {
                    ItemStack filter = item.clone();
                    filter.setAmount(1);
                    PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(chest, group, i, PipeChestFilterTypeEnum.TOTAL_MATCH);
                    PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilter(chest, group, i, filter);
                }
                else
                {
                    PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(chest, group, i, PipeChestFilterTypeEnum.ALL);
                }
            }
            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 3 || button == 13)
        {
            PipesManager.getInstant(PipeTypeEnum.COPPER).clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(chest, group, i, PipeChestFilterTypeEnum.ALL);
            }
            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
            AdvancedPipeGUI advancedPipeGUI = new AdvancedPipeGUI(player, pipe, chest);
            advancedPipeGUI.open();
        }
        if (button == 4 || button == 14)
        {
            PipesManager.getInstant(PipeTypeEnum.COPPER).clearChestSettingsFilterType(chest, group);
            for(int i: ContainerManager.getInventorySlots(pipe, chest))
            {
                PipesManager.getInstant(PipeTypeEnum.COPPER).setChestSettingsFilterType(chest, group, i, PipeChestFilterTypeEnum.DISABLED);
            }
            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
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
            if (s.getPipeChestFilterType() == null || s.getPipeChestFilterType() == PipeChestFilterTypeEnum.MATERIAL_ONLY) s.setPipeChestFilterType(PipeChestFilterTypeEnum.TOTAL_MATCH);
            else s.setPipeChestFilterType(PipeChestFilterTypeEnum.MATERIAL_ONLY);
            s.setGetter(1);
            select.put(player.getUniqueId(), s);
            if (button == 15) select.remove(player.getUniqueId());
            PipesManager.getInstant(PipeTypeEnum.COPPER).reScanLookupGroup(group);
            pipeConnectionGUI.open();
        }

    }
}
