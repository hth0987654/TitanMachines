package com.firesoftitan.play.titanbox.titanmachines.blocks;

import com.firesoftitan.play.titanbox.libs.blocks.TitanBlock;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.titanmachines.TitanMachines;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.enums.PipeTypeEnum;
import com.firesoftitan.play.titanbox.titanmachines.guis.SorterGUI;
import com.firesoftitan.play.titanbox.titanmachines.managers.PipesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class JunctionBoxBlock extends TitanBlock {

    public static final BlockFace[] blockFaces = {BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST};
    public static final String titanID = "JUNCTION_BOX";

    public static JunctionBoxBlock convert(TitanBlock titanBlock)
    {
        if (titanBlock.getTitanID() == null) return null;
        if (!titanBlock.getTitanID().equals(JunctionBoxBlock.titanID)) return null;
        return new JunctionBoxBlock(titanBlock.getSaveManager());
    }
    public final HashMap<BlockFace, Inventory> sides = new HashMap<BlockFace, Inventory>();
    public JunctionBoxBlock(String titanID, ItemStack itemStack, Location location) {
        super(titanID, itemStack, location);

    }

    public JunctionBoxBlock(SaveManager saveManager) {
        super(saveManager);
        for(BlockFace blockFace: blockFaces)
        {
            Inventory inventory = Bukkit.createInventory(null, 18, SorterGUI.name + " " + blockFace.name());
            sides.put(blockFace, inventory);

        }
    }
    public PipeChestFilterTypeEnum getFilterType(BlockFace blockFace, ItemStack itemStack)
    {
        String name = TitanMachines.itemStackTool.getName(itemStack);
        String yamlValid = name.replaceAll("[^a-zA-Z0-9_.-]", "");
        int value = saveManager.getInt(blockFace.name() + ".filter.type." + yamlValid);
        return PipeChestFilterTypeEnum.getPipeChestType(value);
    }
    public BlockFace getFilter(ItemStack itemStack)
    {
        for (BlockFace blockFace: blockFaces)
        {
            if (isInFilterList(blockFace, itemStack)) return blockFace;
        }
        return null;
    }

    public Boolean isInFilterList(BlockFace blockFace, ItemStack itemStack)
    {
        List<ItemStack> itemList = saveManager.getItemList(blockFace.name() + ".filter.items");
        for(ItemStack itemStack1: itemList)
        {
            if (getFilterType(blockFace, itemStack) == PipeChestFilterTypeEnum.TOTAL_MATCH)
                if (TitanMachines.itemStackTool.isItemEqual(itemStack1, itemStack)) return true;
            if (getFilterType(blockFace, itemStack) == PipeChestFilterTypeEnum.MATERIAL_ONLY)
                if (itemStack1.getType().equals(itemStack.getType())) return true;
        }
        return false;
    }
    public List<ItemStack> getFilterList(BlockFace blockFace)
    {
        return saveManager.getItemList(blockFace.name() + ".filter.items");
    }
    public void setFilterType(BlockFace blockFace, ItemStack itemStack, PipeChestFilterTypeEnum pipe)
    {
        String name = TitanMachines.itemStackTool.getName(itemStack);
        String yamlValid = name.replaceAll("[^a-zA-Z0-9_.-]", "");
        saveManager.set(blockFace.name() + ".filter.type." + yamlValid, pipe.getValue());
        TitanBlock.updateBlock(this);
    }
    public void setFilterList(BlockFace blockFace, List<ItemStack> filter)
    {
        Set<String> seenNames = new HashSet<>();

        for (Iterator<ItemStack> it = filter.iterator(); it.hasNext(); ) {
            ItemStack item = it.next();
            String name = TitanMachines.itemStackTool.getName(item);

            if (seenNames.contains(name)) {
                // Duplicate - remove it
                it.remove();
            } else {
                // First time seeing this name - addItem it
                if (getFilterType(blockFace, item) != PipeChestFilterTypeEnum.DISABLED) seenNames.add(name);
                else it.remove();
            }
        }
        List<ItemStack> filteredItems = new ArrayList<>(filter);

        saveManager.set(blockFace.name() + ".filter.items", filteredItems);
        TitanBlock.updateBlock(this);
    }

    public Inventory getInventory(BlockFace blockFace)
    {
        List<ItemStack> itemList = saveManager.getItemList(blockFace.name() + ".inventory");
        ItemStack[] itemStackArray = itemList.toArray(new ItemStack[0]);
        Inventory inventory = sides.get(blockFace);
        inventory.setContents(itemStackArray);
        return inventory;
    }
    public void setInventory(BlockFace blockFace, Inventory inventory)
    {
        sides.put(blockFace, inventory);
        List<ItemStack> saves = new ArrayList<ItemStack>(Arrays.asList(inventory.getContents()));
        if (saves.get(0) == null) saves.set(0, new ItemStack(Material.AIR));
        saveManager.set(blockFace.name() + ".inventory", saves);
        TitanBlock.updateBlock(this);
    }
    public BlockFace getNoFilter()
    {
        for (BlockFace face: JunctionBoxBlock.blockFaces) {
            if (saveManager.getBoolean(face.name() + ".default")) return face;
        }
        return BlockFace.NORTH;
    }
    public void setNoFilter(BlockFace blockFace)
    {
        for (BlockFace face: JunctionBoxBlock.blockFaces) {
            saveManager.set(face.name() + ".default", false);
        }
        saveManager.set(blockFace.name() + ".default", true);
        TitanBlock.updateBlock(this);
    }
    public BlockFace getOverflow()
    {
        for (BlockFace face: JunctionBoxBlock.blockFaces) {
            if (saveManager.getBoolean(face.name() + ".overflow")) return face;
        }
        return BlockFace.NORTH;
    }
    public void setOverflow(BlockFace blockFace)
    {
        for (BlockFace face: JunctionBoxBlock.blockFaces) {
            saveManager.set(face.name() + ".overflow", false);
        }
        saveManager.set(blockFace.name() + ".overflow", true);
        TitanBlock.updateBlock(this);
    }
    public static Location getPipeLocation(UUID uuid, Location box)
    {
        Location location = box.clone();
        for (BlockFace blockFace: blockFaces) {

            Location pipe = location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            if (PipesManager.getInstant(PipeTypeEnum.COPPER).isPipe(pipe)) {
                UUID group = PipesManager.getInstant(PipeTypeEnum.COPPER).getGroup(pipe);
                if (group.equals(uuid)) return pipe;
            }
        }
        return null;
    }


}
