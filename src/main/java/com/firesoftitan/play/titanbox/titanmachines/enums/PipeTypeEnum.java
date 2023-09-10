package com.firesoftitan.play.titanbox.titanmachines.enums;

public enum PipeTypeEnum {
    COPPER(30000, "copper"),
    IRON(40000, "iron"),
    GOLD(50000, "gold"),
    DIAMOND(60000, "diamond");

    private final int horizontal;
    private final String caption;
    PipeTypeEnum(int horizontal, String caption) {
        this.horizontal = horizontal;
        this.caption = caption;
    }


    public int getHorizontal() {
        return horizontal;
    }

    public String getCaption() {
        return caption;
    }
}
