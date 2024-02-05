package com.firesoftitan.play.titanbox.titanmachines;

import com.firesoftitan.play.titanbox.libs.TitanBoxLibs;
import com.firesoftitan.play.titanbox.libs.managers.TitanBlockManager;
import com.firesoftitan.play.titanbox.libs.tools.*;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.listeners.MainListener;
import com.firesoftitan.play.titanbox.titanmachines.listeners.TabCompleteListener;
import com.firesoftitan.play.titanbox.titanmachines.listeners.TitanMachineBlockListener;
import com.firesoftitan.play.titanbox.titanmachines.managers.*;
import com.firesoftitan.play.titanbox.titanmachines.runnables.*;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import com.firesoftitan.play.titanbox.titanmachines.support.SlimefunSupport;
import com.firesoftitan.play.titanbox.titanmachines.support.WildStackerSupport;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class TitanMachines extends JavaPlugin {

    public static MainListener mainListener;
    public static Tools tools;
    public static LibsHologramTool hologramTool;
    public static TitanMachines instants;
    public static  LibsNBTTool nbtTool;
    public static  LibsItemStackTool itemStackTool;
    public static LibsMessageTool messageTool;
    public static LibsLocationTool locationTool;
    public static LibsFormattingTool formattingTool;
    public static LibsSerializeTool serializeTool;
    public static VisualRunnable visualTask;
    public static boolean pipedEnabled = true;
    public static boolean sorterEnabled = true;
    public static boolean hopperEnabled = true;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instants = this;
        tools = new Tools(this, new SaveRunnable(), -1);
        nbtTool = tools.getNBTTool();
        locationTool = tools.getLocationTool();
        itemStackTool = tools.getItemStackTool();
        messageTool = tools.getMessageTool();
        hologramTool = tools.getHologramTool();
        formattingTool = tools.getFormattingTool();
        serializeTool = tools.getSerializeTool();
        new SensibleToolboxSupport();
        new SlimefunSupport();
        new WildStackerSupport();

        mainListener = new MainListener(this);
        mainListener.registerEvents();
        TitanBlockManager.registerListener(this, new TitanMachineBlockListener());

        visualTask = new VisualRunnable();
        visualTask.runTaskTimer(this, 10, 10);
        new RecipeManager();
        new BlockBreakerManager();
        new ItemSorterManager();
        new TrashBarrelManager();

        for(PipeTypeEnum typeEnum: PipeTypeEnum.values()) {
            new PipesManager(typeEnum);
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (String key: PipesManager.getInstant(typeEnum).getPipes())
                    {
                        Location location = tools.getSerializeTool().deserializeLocation(key);
                        PipesManager.getInstant(PipeTypeEnum.COPPER).rescanPipeOrientation(location);
                    }
                }
            }.runTaskLater(this, 20);

        }

        new LumberjackRunnable().runTaskTimer(this, 20, 20);
        new BlockBreakerRunnable().runTaskTimer(this, 20, 20);
        new HopperRunnable().runTaskTimer(this, 20, 20);
        new SorterRunnable().runTaskTimer(this, 20, 20);
        new TrashBarrelRunnable().runTaskTimer(this, 20, 20);
        new JunctionBoxRunnable().runTaskTimer(this,5, 5);
        new TPSMonitorRunnable().runTaskTimer(this, 20, 20);
        new PipeLoaderRunnable().runTaskLater(this, 1);
        new MobKillerRunnable().runTaskTimer(this, 5, 5);


        Objects.requireNonNull(this.getCommand("titanmachines")).setTabCompleter(new TabCompleteListener());
        Objects.requireNonNull(this.getCommand("tm")).setTabCompleter(new TabCompleteListener());


    }

    @Override
    public void onDisable() {
        visualTask.removeAll();
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (isAdmin(sender)) {
            if (label.equalsIgnoreCase("titanmachines") || label.equalsIgnoreCase("tm")) {
                if (args.length > 0) {
                    String name = args[0];
                    if (name.equalsIgnoreCase("check"))
                    {
                        PipesManager instant = PipesManager.getInstant(PipeTypeEnum.COPPER);
                        Location location = ((Player) sender).getLocation();
                        messageTool.sendMessagePlayer((Player) sender,  instant.isPipe(location) + "");
                        String key = TitanMachines.serializeTool.serializeLocation(location);
                        messageTool.sendMessagePlayer((Player) sender,  key );
                        return true;
                    }
                    if (name.equalsIgnoreCase("tps"))
                    {
                        messageTool.sendMessagePlayer((Player) sender, ChatColor.UNDERLINE + "" + ChatColor.DARK_PURPLE + "-------------------------------");
                        messageTool.sendMessagePlayer((Player) sender, ChatColor.AQUA + "Current TPS: " + ChatColor.WHITE + TPSMonitorRunnable.instance.getCurrentTick());
                        messageTool.sendMessagePlayer((Player) sender, ChatColor.GREEN + "Average TPS: " + ChatColor.WHITE + TPSMonitorRunnable.instance.getAverageMinute());
                        messageTool.sendMessagePlayer((Player) sender, ChatColor.GOLD + "Minimum TPS: " + ChatColor.WHITE + TPSMonitorRunnable.instance.getMinimumTick());
                        messageTool.sendMessagePlayer((Player) sender, ChatColor.UNDERLINE + "" + ChatColor.DARK_PURPLE + "-------------------------------");
                        return true;
                    }
                    if (name.equalsIgnoreCase("toggle"))
                    {
                        if (args.length > 1) {
                            String type = args[1];
                            if (type.equalsIgnoreCase("pipes") || type.equalsIgnoreCase("pipe")) {
                                pipedEnabled = !pipedEnabled;
                                if (pipedEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Pipes are now Enabled");
                                if (!pipedEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Pipes are now Disabled");
                                return true;
                            }
                            if (type.equalsIgnoreCase("sorters") || type.equalsIgnoreCase("sorter")) {
                                sorterEnabled = !sorterEnabled;
                                if (sorterEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Sorters are now Enabled");
                                if (!sorterEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Sorters are now Disabled");
                                return true;
                            }
                            if (type.equalsIgnoreCase("hoppers") || type.equalsIgnoreCase("hopper")) {
                                hopperEnabled = !hopperEnabled;
                                if (hopperEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Hoppers are now Enabled");
                                if (!hopperEnabled)
                                    messageTool.sendMessagePlayer((Player) sender, "Hoppers are now Disabled");
                                return true;
                            }
                        }

                    }
                    if (name.equalsIgnoreCase("give"))
                    {
                        int amount = 1;
                        if (args.length > 3)
                        {
                            amount = Integer.parseInt(args[3]);
                        }
                        if (args[2].equals("breaker")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getBlockBreaker();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("trash")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);

                                ItemStack itemStack = getTrashBarrel();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("junctionbox")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getJunctionBox();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("mobkiller")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getMobKiller();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("lumberjack")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getLumberjack();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("pipe")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getPipe();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("sorter")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getItemSorter();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("chunkhopper")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getChunkHopper();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (args[2].equals("areahopper")) {
                            try {
                                Player player = Bukkit.getPlayer(args[1]);
                                ItemStack itemStack = getAreaHopper();
                                itemStack.setAmount(amount);
                                player.getInventory().addItem(itemStack.clone());
                                return true;
                            } catch (IllegalArgumentException e) {

                            }
                        }
                    }
                }
            }
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                messageTool.sendMessagePlayer(player, "/tm reload - Reloads config files");
                messageTool.sendMessagePlayer(player, "/tm give <name> <pipe/sorter/hopper> #");
                messageTool.sendMessagePlayer(player, "/tm toggle <pipe/sorter/hopper>");
            }
            else
            {
                messageTool.sendMessageSystem("/tm reload - Reloads config files");
                messageTool.sendMessageSystem("/tm give <name> <pipe/sorter/chunkhopper/ect> #");
                messageTool.sendMessageSystem( "/tm toggle <pipe/sorter/hopper>");
            }

        }
        return true;
    }
    public ItemStack getPipe() {
        return getPipe(30030);
    }
    public ItemStack getPipe(int modelNumber) {
        ItemStack itemStack = new ItemStack(Material.BLACKSTONE);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Pipe");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Send items threw the pipe");
        itemStack = nbtTool.set(itemStack, "pipe", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "PIPE");
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(modelNumber);
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getItemSorter() {
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Item Sorter");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Sends items to chests,",
                ChatColor.AQUA + "based on what is already in them.",
                ChatColor.YELLOW + "Place item in chest, then Shift Right-Click with empty hand, to addItem chest.",
                ChatColor.DARK_RED + "Shift Right-Click chest with empty hand for more options.",
                ChatColor.GRAY + "*All rejected items go to the chest attached to the hopper.",
                ChatColor.GRAY + "*Only 1 item can be assigned to each chest.",
                ChatColor.GRAY + "*Supports: Chest, Trap Chest, Barrels, BigStorageUnits, HyperStorageUnits");
        itemStack = nbtTool.set(itemStack, "itemsorter", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "ITEM_SORTER");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getMobKiller() {
        ItemStack itemStack = new ItemStack(Material.GILDED_BLACKSTONE);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Mob Killer");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Kills all mobs with in 7 blocks", ChatColor.AQUA + "While holding weapon right click block damage will be set",  ChatColor.AQUA + "equal to that weapons and players combined damage");
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "MOB_KILLER_BLOCK");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getJunctionBox() {
        ItemStack itemStack = new ItemStack(Material.WAXED_COPPER_BLOCK);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Junction Box");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Connect pipes for advanced sorting");
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "JUNCTION_BOX");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getTrashBarrel() {
        ItemStack itemStack = new ItemStack(Material.BARREL);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Trash Barrel");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Removes all blocks in its inventory.");
        itemStack = nbtTool.set(itemStack, "trashbarrel", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "TRASH_BARREL");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getLumberjack() {
        ItemStack itemStack = new ItemStack(Material.DISPENSER);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Lumberjack");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Cuts down single tree when they grow, up to 11 blocks tall"
                , ChatColor.GRAY + "Right click for info"
                , ChatColor.GRAY + "Hold shift and right click to turn on/off"
                , ChatColor.GRAY + "Plant sapling directly in front of face and make sure machine is on"
                , ChatColor.GRAY + "Machine will auto gather saplings, but you can hold a sapling in hand and right click to addItem more");

        itemStack = nbtTool.set(itemStack, "lumberjack", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "LUMBERJACK");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getBlockBreaker() {
        ItemStack itemStack = new ItemStack(Material.DISPENSER);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Block Breaker");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Breaks Blocks in front of it.");
        itemStack = nbtTool.set(itemStack, "blockbreaker", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "BLOCK_BREAKER");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getChunkHopper() {
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Chunk Hopper");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Sucks dropped items from the chunk its placed in.");
        itemStack = nbtTool.set(itemStack, "chunkhopper", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "CHUNK_HOPPER");
        itemStack.setAmount(1);
        return itemStack;
    }
    public ItemStack getAreaHopper() {
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        itemStack = itemStackTool.changeName(itemStack, ChatColor.AQUA + "Area Hopper");
        itemStack = itemStackTool.addLore(itemStack, ChatColor.YELLOW + "Sucks dropped items from 5 blocks away.");
        itemStack = nbtTool.set(itemStack, "areahopper", true);
        itemStack = tools.getItemStackTool().setTitanItemID(itemStack, "AREA_HOPPER");
        itemStack.setAmount(1);
        return itemStack;
    }

    public static boolean isAdmin(CommandSender sender)
    {
        if (sender.isOp() || sender.hasPermission("titanbox.admin")) return true;
        return false;
    }
}
