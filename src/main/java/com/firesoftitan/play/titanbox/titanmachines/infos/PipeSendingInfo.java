package com.firesoftitan.play.titanbox.titanmachines.infos;

import com.firesoftitan.play.titanbox.titanmachines.enums.PipeChestFilterTypeEnum;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PipeSendingInfo {

    private final PipeLookUpInfo lookUp;
    private final UUID group;

    public InOutBoundInfo inBound;
    public InOutBoundInfo outBound;
    private PipeChestFilterTypeEnum match;
    public PipeSendingInfo(UUID group, PipeLookUpInfo lookUp) {
        this.group = group;
        this.lookUp = lookUp;
        inBound = new InOutBoundInfo(this.group);
        outBound = new InOutBoundInfo(this.group);
    }

    public PipeLookUpInfo getLookUp() {
        return lookUp;
    }

    public UUID getGroup() {
        return group;
    }

    public PipeChestFilterTypeEnum getMatch() {
        return match;
    }

    public void setMatch(PipeChestFilterTypeEnum match) {
        this.match = match;
    }
}
