package com.firesoftitan.play.titanbox.titanmachines.guis;

import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.UUID;

public class SelectorGUI {
    private int getter;
    private PipeChestFilterTypeEnum pipeChestFilterType;
    private BlockFace blockFace;
    private BlockFace currentBlockFace;
    private final Location location;
    private final Location connection;
    private final UUID group;

    public SelectorGUI(Location location, Location connection, UUID group) {
        this.getter = 0;
        this.location = location;
        this.connection = connection;
        this.group = group;
    }

    public BlockFace getcurrentBlockFace() {
        return currentBlockFace;
    }

    public void setCurrentBlockFace(BlockFace currentBlockFace) {
        this.currentBlockFace = currentBlockFace;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public void setBlockFace(BlockFace blockFace) {
        this.blockFace = blockFace;
    }

    public void setPipeChestFilterType(PipeChestFilterTypeEnum pipeChestFilterType) {
        this.pipeChestFilterType = pipeChestFilterType;
    }

    public PipeChestFilterTypeEnum getPipeChestFilterType() {

        return pipeChestFilterType;
    }

    public void setGetter(int getter) {
        this.getter = getter;
    }

    public int getGetter() {
        return getter;
    }

    public Location getLocation() {
        return location;
    }

    public Location getConnection() {
        return connection;
    }

    public UUID getGroup() {
        return group;
    }

}
