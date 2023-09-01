package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.managers.ContainerVisualManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.ItemSorterManager;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SorterGUI {
    private final Inventory inventory;
    public static String name = "Sorter Chest Config";
    private final int size = 27;
    private final Location location;
    private final String key;
    private final Player player;
    public SorterGUI(Player player, Location chest) {
        this.location = chest.clone();
        this.player = player;
        this.key = ItemSorterManager.instance.getKey(this.location);
        this.inventory = Bukkit.createInventory(null, size, SorterGUI.name);
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getKey() {
        return key;
    }

    public Player getPlayer() {
        return player;
    }

    public void render()
    {
        boolean sorting = false;
        ItemStack borderItem = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        for(int i = 0; i < size; i++)
        {
            this.inventory.setItem(i, borderItem.clone());
        }

        int settingsSortingType = ItemSorterManager.instance.getSettingsSortingType(this.player, this.location);
        BlockFace blockFace = ItemSorterManager.instance.getSettingsSortingFacing(this.player, this.location);
        ItemStack sortingItem = new ItemStack(Material.BARRIER);
        sortingItem = TitanMachines.itemStackTool.changeName(sortingItem, "Sorting not set");
        if (settingsSortingType > 0) {
            sorting = true;
            sortingItem = ItemSorterManager.instance.getSortingItem(this.player, this.location);
        } else if (settingsSortingType == 0)
        {
            sorting = true;
        }
        sortingItem.setAmount(1);
        sortingItem = TitanMachines.itemStackTool.addLore(sortingItem, ChatColor.GRAY + "" + ChatColor.UNDERLINE + "___________________", ChatColor.AQUA + "Click to Rescan Chest");
        sortingItem = TitanMachines.nbtTool.set(sortingItem, "button", 10);
        sortingItem = TitanMachines.nbtTool.set(sortingItem, "location", this.location);
        this.inventory.setItem(10, sortingItem);
        if (sorting) {
            ItemStack item = borderItem.clone();
            switch (settingsSortingType) {
                case 0:
                    item = new ItemStack(Material.BARRIER);
                    item = TitanMachines.itemStackTool.changeName(item,  "Sorting: " + ChatColor.DARK_RED + "Disabled");
                    break;
                case 1:
                    item = new ItemStack(Material.NETHERITE_SWORD);
                    item = TitanMachines.itemStackTool.changeName(item, "Sorting: " + ChatColor.AQUA + "100% Match Item");
                    break;
                case 2:
                    item = new ItemStack(Material.GRASS_BLOCK);
                    item = TitanMachines.itemStackTool.changeName(item, "Sorting: " + ChatColor.YELLOW + "Material Only");
                    break;
            }
            item = TitanMachines.itemStackTool.addLore(item,  ChatColor.AQUA + "   100% Match", ChatColor.YELLOW + "   Material Only", ChatColor.DARK_RED + "   Disable");
            item = TitanMachines.nbtTool.set(item, "button", 13);
            item = TitanMachines.nbtTool.set(item, "location", this.location);
            this.inventory.setItem(13, item.clone());



            item = new ItemStack(Material.BARRIER);
            item = TitanMachines.itemStackTool.changeName(item, "Remove chest from sorting");
            item = TitanMachines.itemStackTool.addLore(item,  ChatColor.AQUA + "Open/Close chest, with item in it, to add again");
            item = TitanMachines.nbtTool.set(item, "button", 16);
            item = TitanMachines.nbtTool.set(item, "location", this.location);
            this.inventory.setItem(16, item.clone());


            item = new ItemStack(Material.ITEM_FRAME);
            item = TitanMachines.itemStackTool.changeName(item, "Icon Side");
            item = TitanMachines.itemStackTool.addLore(item,  ChatColor.AQUA + "" + blockFace.name());
            item = TitanMachines.nbtTool.set(item, "button", 1);
            item = TitanMachines.nbtTool.set(item, "location", this.location);
            this.inventory.setItem(1, item.clone());

        }


    }
    public void open()
    {
        this.render();
        this.player.openInventory(this.inventory);
    }
    public static void onClickButtonEvent(Player player, Integer button, Location location, Location sorting) {
        Block block = location.getBlock();
        BlockState state = block.getState();
        switch (button)
        {
            case 1:
                if(ItemSorterManager.instance.hasSorter(player)) {
                    if (ItemSorterManager.instance.getSortingItem(player, location) != null) {
                        BlockFace settingsSortingFacing = ItemSorterManager.instance.getSettingsSortingFacing(player, location);
                        switch (settingsSortingFacing)
                        {
                            case NORTH -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.SOUTH);
                            case SOUTH -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.EAST);
                            case EAST -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.WEST);
                            case WEST -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.UP);
                            case UP -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.DOWN);
                            case DOWN -> ItemSorterManager.instance.setSettingsSortingFacing(player, location, BlockFace.NORTH);
                        }
                        SorterGUI sorterGUI = new SorterGUI(player, location);
                        sorterGUI.open();
                    }
                }
                break;

            case 10:
                if (state instanceof Container container)
                {
                    Inventory inventory = container.getInventory();
                    if (ItemSorterManager.instance.hasSorter(player)) {
                        if (ItemSorterManager.isSortingContainer(location)) {
                            BlockFace settingsSortingFacing = ItemSorterManager.instance.getSettingsSortingFacing(player, location);
                            ItemStack added = ItemSorterManager.instance.addChest(player, location, inventory);
                            ItemSorterManager.instance.setSettingsSortingFacing(player, location, settingsSortingFacing);
                            if (!TitanMachines.itemStackTool.isEmpty(added)) {
                                String name = TitanMachines.itemStackTool.getName(added);
                                TitanMachines.messageTool.sendMessagePlayer(player, "Chest added to Sorting Hopper, Item: " + ChatColor.WHITE + name);
                                SorterGUI sorterGUI = new SorterGUI(player, location);
                                sorterGUI.open();
                            }
                        }
                    }
                }
                else {
                    if (ItemSorterManager.instance.hasSorter(player)) {
                        if (ItemSorterManager.isSortingContainer(location)) {
                            BlockFace settingsSortingFacing = ItemSorterManager.instance.getSettingsSortingFacing(player, location);
                            ItemStack added = ItemSorterManager.instance.addChest(player, location, SensibleToolboxSupport.instance.getOutputItem(location));
                            ItemSorterManager.instance.setSettingsSortingFacing(player, location, settingsSortingFacing);
                            ItemSorterManager.instance.setSettingsSortingLock(player, location);
                            if (!TitanMachines.itemStackTool.isEmpty(added)) {
                                String name = TitanMachines.itemStackTool.getName(added);
                                TitanMachines.messageTool.sendMessagePlayer(player, "Chest added to Sorting Hopper, Item: " + ChatColor.WHITE + name);
                                SorterGUI sorterGUI = new SorterGUI(player, location);
                                sorterGUI.open();
                            }
                        }
                    }

                }
                break;
            case 13:
                int settingsSortingType = ItemSorterManager.instance.getSettingsSortingType(player, location);
                settingsSortingType++;
                if (settingsSortingType > 2) settingsSortingType = 0;
                ItemSorterManager.instance.setSettingsSortingType(player, location, settingsSortingType);
                SorterGUI sorterGUI = new SorterGUI(player, location);
                sorterGUI.open();
                break;
            case 16:
                if(ItemSorterManager.instance.hasSorter(player)) {
                    if (ItemSorterManager.instance.getSortingItem(player, location) != null) {
                        ItemSorterManager.instance.removeContainer(player, location);
                        if (location.getBlock().getState() instanceof Container container) location = container.getInventory().getLocation();
                        ContainerVisualManager.removeManager(location);
                        player.closeInventory();
                    }
                }
                break;
        }
    }

}
