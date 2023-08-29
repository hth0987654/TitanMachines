package com.firesoftitan.play.titanbox.titanmachines.listeners;

import com.firesoftitan.play.titanbox.libs.tools.LibsItemStackTool;
import com.firesoftitan.play.titanbox.libs.tools.LibsNBTTool;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.guis.AdvancedPipeGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.JunctionBoxGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.PipeConnectionGUI;
import com.firesoftitan.play.titanbox.titanmachines.guis.SorterGUI;
import com.firesoftitan.play.titanbox.titanmachines.managers.*;
import com.firesoftitan.play.titanbox.titanmachines.runnables.PickUpItemRunnable;
import com.firesoftitan.play.titanbox.titanmachines.support.SensibleToolboxSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.UUID;

public class MainListener implements Listener {
    private final JavaPlugin plugin;
    private final LibsNBTTool nbtTool = TitanMachines.tools.getNBTTool();
    private final LibsItemStackTool itemStackTool = TitanMachines.tools.getItemStackTool();

    public MainListener(JavaPlugin plugin){
        this.plugin = plugin;
    }
    public void registerEvents(){
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPowered(Block block)
    {
        if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) return true;
        for(BlockFace blockFace: BlockFace.values())
        {
            if (block.isBlockFacePowered(blockFace)) return true;
        }
        return false;
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerMoveEvent(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        if (ItemSorterManager.instance.hasSorter(player)) {
            Block targetBlock = getTargetBlock(player, 7);
            if (targetBlock != null) {
                boolean sortingContainer = ItemSorterManager.isSortingContainer(targetBlock.getLocation());
                if (sortingContainer) {
                    ItemStack sortingItem = ItemSorterManager.instance.getSortingItem(player, targetBlock.getLocation());
                    if (!TitanMachines.itemStackTool.isEmpty(sortingItem)) {
                        if (sortingItem.hasItemMeta() && sortingItem.getItemMeta().hasDisplayName()) {
                            player.sendTitle("", sortingItem.getItemMeta().getDisplayName(), 0, 5, 0);
                        }
                    }
                }
            }
        }

    }
    public final Block getTargetBlock(Player player, int range) {
        try {
            BlockIterator iter = new BlockIterator(player, range);
            Block lastBlock = iter.next();
            while (iter.hasNext()) {
                lastBlock = iter.next();
                if (lastBlock.getType() == Material.AIR) {
                    continue;
                }
                break;
            }
            return lastBlock;
        } catch (Exception e) {
            return null;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(EntitySpawnEvent event)
    {
        if (event.getEntityType() == EntityType.DROPPED_ITEM)
        {
            Location location = event.getLocation();
            if (ChunkHopperManager.hasHopper(location)) {
                String key = ChunkHopperManager.getKey(location);
                Location hoppersLocation = ChunkHopperManager.getLocation(key);
                if (hoppersLocation.getChunk().isLoaded()) {
                    Block block = hoppersLocation.getBlock();
                    if (block.getType() != Material.HOPPER) {
                        block.setType(Material.HOPPER);
                    }
                    if (!isPowered(block)) {
                        Entity e = event.getEntity();
                        if (!e.isDead()) {
                            new PickUpItemRunnable(block, e).runTaskTimer(TitanMachines.instants, 1, 20);
                        }
                    }
                }
            }
            String key = AreaHopperManager.getHopperKey(location);
            if (key != null) {
                Location hoppersLocation = AreaHopperManager.getLocation(key);
                if (hoppersLocation.getChunk().isLoaded()) {
                    Block block = hoppersLocation.getBlock();
                    if (block.getType() != Material.HOPPER) {
                        block.setType(Material.HOPPER);
                    }
                    if (!isPowered(block)) {
                        Entity e = event.getEntity();
                        if (!e.isDead()) {
                            new PickUpItemRunnable(block, e).runTaskTimer(TitanMachines.instants, 1, 20);
                        }
                    }
                }
            }
        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
/*        Inventory inventory = event.getInventory();
        Location location = inventory.getLocation();
        HumanEntity player = event.getPlayer();
        if (location != null)
        {
            if (ItemSorterManager.instance.hasSorter((Player) player))
            {
                ItemStack sortingItem = ItemSorterManager.instance.getSortingItem((Player) player, location);
                if (itemStackTool.isEmpty(sortingItem))
                {
                    if (ItemSorterManager.isSortingContainer(location)) {
                        ItemStack added = ItemSorterManager.instance.addChest((Player)player, location, inventory);
                        if (!TitanMachines.itemStackTool.isEmpty(added)) {
                            if (!ItemSorterManager.instance.getSettingsSortingLock((Player)player, location)) {
                                ItemSorterManager.instance.setSettingsSortingFacing((Player)player, location, player.getFacing().getOppositeFace());
                                ItemSorterManager.instance.setSettingsSortingLock((Player)player, location);
                            }

                            String name = TitanMachines.itemStackTool.getName(added);
                            TitanMachines.messageTool.sendMessagePlayer((Player) player, "Chest added to Sorting Hopper, Item: " + ChatColor.WHITE + name);
                        }
                    }
                }
                else
                {
                    if (!ItemSorterManager.instance.getSettingsSortingLock((Player)player, location)) {
                        ItemSorterManager.instance.setSettingsSortingFacing((Player)player, location, player.getFacing().getOppositeFace());
                        ItemSorterManager.instance.setSettingsSortingLock((Player)player, location);
                    }
                }
            }
        }*/

    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        InventoryView openInventory = whoClicked.getOpenInventory();
        Inventory clickedInventory = event.getClickedInventory();
        if (openInventory.getTitle().equals(AdvancedPipeGUI.name)) {
            event.setCancelled(true);
            if (event.getSlot() > -1 && event.getSlot() < clickedInventory.getSize()) {
                ItemStack clicked = clickedInventory.getItem(event.getSlot());
                if (!TitanMachines.itemStackTool.isEmpty(clicked)) {
                    if (TitanMachines.nbtTool.containsKey(clicked, "button") && TitanMachines.nbtTool.containsKey(clicked, "group")) {
                        Integer button = TitanMachines.nbtTool.getInteger(clicked, "button");
                        if (event.getClick() == ClickType.RIGHT) button = button + 10;
                        Integer slot = TitanMachines.nbtTool.getInteger(clicked, "slot");
                        Location location = TitanMachines.nbtTool.getLocation(clicked, "location");
                        Location connection = TitanMachines.nbtTool.getLocation(clicked, "connection");
                        UUID group = TitanMachines.nbtTool.getUUID(clicked, "group");

                        AdvancedPipeGUI.onClickButtonEvent((Player) whoClicked, button, location, connection, group, slot);
                    }
                    else {
                        AdvancedPipeGUI.setTypeSelect((Player) whoClicked, clicked);
                    }
                }
            }
        }
        if (openInventory.getTitle().equals(PipeConnectionGUI.name)) {
            event.setCancelled(true);
            if (event.getSlot() > -1 && event.getSlot() < clickedInventory.getSize()) {
                ItemStack clicked = clickedInventory.getItem(event.getSlot());
                if (!TitanMachines.itemStackTool.isEmpty(clicked)) {
                    if (TitanMachines.nbtTool.containsKey(clicked, "button") && TitanMachines.nbtTool.containsKey(clicked, "group")) {
                        Integer button = TitanMachines.nbtTool.getInteger(clicked, "button");
                        if (event.getClick() == ClickType.RIGHT) button = button + 10;
                        Integer slot = TitanMachines.nbtTool.getInteger(clicked, "slot");
                        Location location = TitanMachines.nbtTool.getLocation(clicked, "location");
                        Location connection = TitanMachines.nbtTool.getLocation(clicked, "connection");
                        UUID group = TitanMachines.nbtTool.getUUID(clicked, "group");

                        PipeConnectionGUI.onClickButtonEvent((Player) whoClicked, button, location, connection, group, slot);
                    }
                    else {
                        PipeConnectionGUI.setTypeSelect((Player) whoClicked, clicked);
                    }
                }
            }
        }
        if (openInventory.getTitle().equals(SorterGUI.name))
        {
            event.setCancelled(true);
            if (event.getSlot() > -1 && event.getSlot() < clickedInventory.getSize()) {
                ItemStack clicked = clickedInventory.getItem(event.getSlot());
                if (!TitanMachines.itemStackTool.isEmpty(clicked)) {
                    if (TitanMachines.nbtTool.containsKey(clicked, "button")) {
                        Integer button = TitanMachines.nbtTool.getInteger(clicked, "button");
                        Location location = TitanMachines.nbtTool.getLocation(clicked, "location");
                        Location sorting = TitanMachines.nbtTool.getLocation(clicked, "sorting");
                        Block block = location.getBlock();
                        SorterGUI.onClickButtonEvent((Player) whoClicked, button, location, sorting);
                    }
                }
            }
        }
        if (openInventory.getTitle().equals(JunctionBoxGUI.name))
        {
            event.setCancelled(true);
            if (event.getSlot() > -1 && event.getSlot() < clickedInventory.getSize()) {
                ItemStack clicked = clickedInventory.getItem(event.getSlot());
                if (!TitanMachines.itemStackTool.isEmpty(clicked)) {
                    if (TitanMachines.nbtTool.containsKey(clicked, "button")) {
                        Integer button = TitanMachines.nbtTool.getInteger(clicked, "button");
                        Location location = TitanMachines.nbtTool.getLocation(clicked, "location");
                        Location sorting = TitanMachines.nbtTool.getLocation(clicked, "sorting");
                        BlockFace currentFace = BlockFace.valueOf(TitanMachines.nbtTool.getString(clicked, "current_face"));
                        JunctionBoxGUI.onClickButtonEvent((Player) whoClicked, button, location, sorting, currentFace);
                    }
                    else {
                        JunctionBoxGUI.setTypeSelect((Player) whoClicked, clicked);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        TitanMachines.visualTask.removeChunk(player);

    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void  onPlayerInteractEvent(PlayerInteractEvent event)
    {
        Action action = event.getAction();
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (PipesManager.instance.isPipe(clickedBlock.getLocation())) {
            PipesManager.instance.rescanPipeOrientation(clickedBlock.getLocation());
        }
        if (action == Action.LEFT_CLICK_BLOCK && !TitanMachines.itemStackTool.isEmpty(itemStack))
        {
            Location clone = clickedBlock.getLocation().clone();
            if (itemStack.getType() == Material.DIAMOND_PICKAXE || itemStack.getType() == Material.NETHERITE_PICKAXE
                    || itemStack.getType() == Material.GOLDEN_PICKAXE  || itemStack.getType() == Material.IRON_PICKAXE
                    || itemStack.getType() == Material.STONE_PICKAXE  || itemStack.getType() == Material.WOODEN_PICKAXE)
            {

                if (PipesManager.instance.isPipe(clone)) {
                    PipesManager.instance.remove(clone);
                    ItemStack itemSorter = TitanMachines.instants.getPipe();
                    clone.getWorld().dropItem(clone, itemSorter.clone());
                    clone.getBlock().setType(Material.AIR);

/*                    Location add = clone.clone().add(0.5f, 0, 0.5f);
                    TitanMachines.hologramTool.removeHologram(add);*/
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PipesManager.instance.checkSurroundings(clone);
                        }
                    }.runTaskLater(TitanMachines.instants, 1);

                }
            }
        }
        if (action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking())
        {
            if (BlockBreakerManager.instance.isBreaker(clickedBlock.getLocation()))
            {
                event.setCancelled(true);
                boolean powered = BlockBreakerManager.instance.isPowered(clickedBlock.getLocation());
                BlockBreakerManager.instance.setPower(clickedBlock.getLocation(), !powered);
                if (powered) TitanMachines.messageTool.sendMessagePlayer((Player) player, "Block breaker has been turned " + ChatColor.RED + "OFF");else TitanMachines.messageTool.sendMessagePlayer((Player) player, "Block breaker has been turned " + ChatColor.GREEN + "ON");
            }
            if (LumberjackManager.instance.isLumberjack(clickedBlock.getLocation()))
            {
                event.setCancelled(true);
                boolean powered = LumberjackManager.instance.isPowered(clickedBlock.getLocation());
                LumberjackManager.instance.setPower(clickedBlock.getLocation(), !powered);
                Material saplingMaterial = LumberjackManager.instance.getSaplingMaterial(clickedBlock.getLocation());
                int saplingCount = LumberjackManager.instance.getSaplingCount(clickedBlock.getLocation());
                if (powered) TitanMachines.messageTool.sendMessagePlayer((Player) player, "Lumberjack has been turned " + ChatColor.RED + "OFF");else TitanMachines.messageTool.sendMessagePlayer((Player) player, "Lumberjack has been turned " + ChatColor.GREEN + "ON");
                if (saplingMaterial != null)TitanMachines.messageTool.sendMessagePlayer((Player) player, "Lumberjack has: " + ChatColor.AQUA + TitanMachines.tools.getFormattingTool().fixCapitalization(saplingMaterial.name()) + ChatColor.WHITE + " " +  ChatColor.GREEN + saplingCount + ChatColor.WHITE + "/" +  ChatColor.GREEN + "8" );

            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK && itemStackTool.isEmpty(itemStack) && !player.isSneaking()) {
            if (PipesManager.instance.isPipe(clickedBlock.getLocation()))
            {
                PipeConnectionGUI pipeConnectionGUI = new PipeConnectionGUI(player, clickedBlock.getLocation());
                pipeConnectionGUI.open();
            }
        }
        if (action == Action.RIGHT_CLICK_BLOCK && itemStackTool.isEmpty(itemStack) && player.isSneaking()) {
            if (SensibleToolboxSupport.instance.isSupported(clickedBlock.getLocation()) || clickedBlock.getState() instanceof Container) {
                Location location = clickedBlock.getLocation();
                //if (ItemSorterManager.instance.hasSorter(location)) {
                if (ItemSorterManager.instance.hasSorter(player)) {
                    event.setCancelled(true);
                    ItemStack sortingItem = ItemSorterManager.instance.getSortingItem(player, location);
                    if (!itemStackTool.isEmpty(sortingItem)) {
                        if (ItemSorterManager.isSortingContainer(location)) {
                            if (!ItemSorterManager.instance.getSettingsSortingLock(player, location)) {
                                ItemSorterManager.instance.setSettingsSortingFacing(player, location, event.getBlockFace());
                                ItemSorterManager.instance.setSettingsSortingLock(player, location);
                            }
                            SorterGUI sorterGUI = new SorterGUI((Player) player, location);
                            sorterGUI.open();
                        }
                    }
                    else
                    {
                        ItemStack sortingItemA = ItemSorterManager.instance.getSortingItem(player, location);
                        if (itemStackTool.isEmpty(sortingItemA)) {
                            if (ItemSorterManager.isSortingContainer(location)) {
                                ItemStack added = ItemSorterManager.instance.addChest(player, location, SensibleToolboxSupport.instance.getOutputItem(location));
                                if (clickedBlock.getState() instanceof Container container) added = ItemSorterManager.instance.addChest(player, location, container.getInventory());
                                if (!TitanMachines.itemStackTool.isEmpty(added)) {
                                    if (!ItemSorterManager.instance.getSettingsSortingLock(player, location)) {
                                        ItemSorterManager.instance.setSettingsSortingFacing(player, location, player.getFacing().getOppositeFace());
                                        ItemSorterManager.instance.setSettingsSortingLock(player, location);
                                    }

                                    String name = TitanMachines.itemStackTool.getName(added);
                                    TitanMachines.messageTool.sendMessagePlayer((Player) player, "Chest added to Sorting Hopper, Item: " + ChatColor.WHITE + name);
                                    return;
                                }
                            }
                        }
                        TitanMachines.messageTool.sendMessagePlayer((Player) player, "This chest hasn't been set for sorting");
                        TitanMachines.messageTool.sendMessagePlayer((Player) player, "Place item in chest, to add it.");
                    }
                }

            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void  onBlockBreakEvent(BlockBreakEvent event) {

        Block block = event.getBlock();
        Location clone = block.getLocation().clone();

        if (ChunkHopperManager.isHopper(clone))
        {
            String key = ChunkHopperManager.getKey(clone);
            ChunkHopperManager.delete(key);
            ItemStack chunkHopper = TitanMachines.instants.getChunkHopper();
            event.setDropItems(false);
            if (clone.getWorld() != null) clone.getWorld().dropItem(clone, chunkHopper.clone());
        }
        if (AreaHopperManager.isHopper(clone))
        {
            String key = AreaHopperManager.getKey(clone);
            AreaHopperManager.delete(key);
            ItemStack chunkHopper = TitanMachines.instants.getAreaHopper();
            event.setDropItems(false);
            if (clone.getWorld() != null) clone.getWorld().dropItem(clone, chunkHopper.clone());
        }
        if (PipesManager.instance.isPipe(clone)) {
            event.setCancelled(true);
        }
        if (TrashBarrelManager.instance.isTrashBarrel(clone))
        {
            TrashBarrelManager.instance.remove(clone);
            ItemStack itemSorter = TitanMachines.instants.getTrashBarrel();
            event.setDropItems(false);
            if (clone.getWorld() != null) clone.getWorld().dropItem(clone, itemSorter.clone());
        }
        if (LumberjackManager.instance.isLumberjack(clone))
        {
            LumberjackManager.instance.remove(clone);
            ItemStack itemSorter = TitanMachines.instants.getLumberjack();
            event.setDropItems(false);
            if (clone.getWorld() != null) clone.getWorld().dropItem(clone, itemSorter.clone());
        }
        if (BlockBreakerManager.instance.isBreaker(clone))
        {
            BlockBreakerManager.instance.remove(clone);
            ItemStack itemSorter = TitanMachines.instants.getBlockBreaker();
            event.setDropItems(false);
            if (clone.getWorld() != null)  clone.getWorld().dropItem(clone, itemSorter.clone());
        }
        if (ItemSorterManager.instance.isSorter(clone))
        {
            event.setDropItems(false);
            if (!ItemSorterManager.instance.isOwner(event.getPlayer(), clone))
            {
                UUID owner = ItemSorterManager.instance.getOwner(clone);
                if (owner == null)
                {
                    if (!ItemSorterManager.instance.hasSorter(event.getPlayer())) {
                        ItemSorterManager.instance.setSelector(event.getPlayer(), clone);
                        TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), ChatColor.GREEN + "You are now the owner of this shorter");
                    }
                    else {
                        removeSorter(event, clone);
                    }
                }
                else {
                    if (TitanMachines.isAdmin(event.getPlayer()))
                    {
                        removeSorter(event, clone);
                        return;
                    }
                    String name  = Bukkit.getOfflinePlayer(owner).getName();
                    TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), ChatColor.RED + "Owner: " + ChatColor.WHITE + name);
                    TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), ChatColor.RED + "Only the person that placed this can remove it.");
                }
                return;
            }
            removeSorter(event, clone);
        }
        if (block.getState() instanceof Container state)
        {
            Location location = state.getInventory().getLocation();
            if (PipesManager.instance.isChestConnected(location))
            {
                PipesManager.instance.removeChest(location);
            }
            if(ItemSorterManager.instance.hasSorter(event.getPlayer())) {
                if (ItemSorterManager.instance.getSortingItem(event.getPlayer(), location) != null) {
                    ContainerVisualManager.removeManager(location);
                    ItemSorterManager.instance.removeContainer(event.getPlayer(), location);
                }
            }
        }
        if (SensibleToolboxSupport.instance.isSupported(block.getLocation()))
        {
            Location location = block.getLocation();
            if (PipesManager.instance.isChestConnected(location))
            {
                PipesManager.instance.removeChest(location);
            }
            if(ItemSorterManager.instance.hasSorter(event.getPlayer())) {
                if (ItemSorterManager.instance.getSortingItem(event.getPlayer(), location) != null) {
                    ContainerVisualManager.removeManager(location);
                    ItemSorterManager.instance.removeContainer(event.getPlayer(), location);
                }
            }
        }
    }

    private static void removeSorter(BlockBreakEvent event, Location clone) {
        ItemSorterManager.instance.removeSorter(event.getPlayer(), clone);
        ItemStack itemSorter = TitanMachines.instants.getItemSorter();
        if (clone.getWorld() != null) clone.getWorld().dropItem(clone, itemSorter.clone());
    }

    @EventHandler()
    public void  onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Location clone = source.getLocation().clone();
        if (ItemSorterManager.instance.isSorter(clone)) event.setCancelled(true);

    }
    @EventHandler()
    public void  onInventoryPickupItemEvent(InventoryPickupItemEvent event) {


    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void  onBlockPlaceEvent(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (TitanMachines.itemStackTool.isContainer(itemInHand))
        {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PipesManager.instance.scanPlacedChest(location);
                }
            }.runTaskLater(TitanMachines.instants, 3);

        }
        if (SensibleToolboxSupport.instance.isSupported(itemInHand))
        {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PipesManager.instance.scanPlacedChest(location);
                }
            }.runTaskLater(TitanMachines.instants, 3);
        }
        if (nbtTool.getBoolean(itemInHand, "pipe") )
        {
            Location clone = location.clone();
            PipesManager.instance.add(clone);
            new BukkitRunnable() {
                @Override
                public void run() {
                    clone.getBlock().setType(Material.BARRIER);
                    PipesManager.instance.checkSurroundings(clone);
                }
            }.runTaskLater(TitanMachines.instants, 1);


        }
        if (nbtTool.getBoolean(itemInHand, "trashbarrel") )
        {
            Location clone = location.clone();
            TrashBarrelManager.instance.add(clone);
            TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), "Trash Barrel ready to remove blocks from the game :)");
        }
        if (nbtTool.getBoolean(itemInHand, "lumberjack") )
        {
            Location clone = location.clone();
            LumberjackManager.instance.add(clone);
            TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), "Lumberjack read to cut down some trees :)");
        }
        if (nbtTool.getBoolean(itemInHand, "blockbreaker") )
        {
            Location clone = location.clone();
            BlockBreakerManager.instance.add(clone);
            TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), "Block Breaker read to break some blocks :)");
        }
        if (nbtTool.getBoolean(itemInHand, "itemsorter")) {
            if (!ItemSorterManager.instance.hasSorter(event.getPlayer())) {
                Location clone = location.clone();
                ItemSorterManager.instance.add(event.getPlayer(), clone);
                TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), "Shift click Chest, and other storage with an empty had to add them to sorting.");
            }
            else {
                TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), ChatColor.RED + "You already have a sorting hopper at: " + ChatColor.WHITE + TitanMachines.formattingTool.formatLocation(ItemSorterManager.instance.getSelector(event.getPlayer())));
                event.setCancelled(true);
            }
        }
        if (nbtTool.getBoolean(itemInHand, "areahopper") || nbtTool.getBoolean(itemInHand, "chunkhopper"))
        {
            Location clone = location.clone();
            if  (AreaHopperManager.getHopperKey(clone) != null || ChunkHopperManager.hasHopper(clone))
            {
                TitanMachines.messageTool.sendMessagePlayer(event.getPlayer(), "Only one hopper per chunk.");
                event.setCancelled(true);
            } else {
                if (nbtTool.getBoolean(itemInHand, "areahopper")) {
                    String key = AreaHopperManager.getKey(clone);
                    AreaHopperManager.setLocation(key, location);
                }
                if (nbtTool.getBoolean(itemInHand, "chunkhopper")) {
                    String key = ChunkHopperManager.getKey(clone);
                    ChunkHopperManager.setLocation(key, location);
                }
            }

        }

    }
    @EventHandler()
    public void onBlockExplodeEvent(BlockExplodeEvent event)
    {
        for(Block block: event.blockList())
        {
            if (ChunkHopperManager.isHopper(block.getLocation()) || ItemSorterManager.instance.isSorter(block.getLocation())) event.setCancelled(true);
        }
    }



}
