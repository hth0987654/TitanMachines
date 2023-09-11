package com.firesoftitan.play.titanbox.titanmachines.enums;

public enum PipeTypeEnum {
    COPPER(30000, "copper", 5000),
    IRON(40000, "iron", 2500),
    GOLD(50000, "gold", 1250),
    DIAMOND(60000, "diamond", 625);

    private final int horizontal;
    private final String caption;
    private final int delay;
    PipeTypeEnum(int horizontal, String caption, int delay) {
        this.horizontal = horizontal;
        this.caption = caption;
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public String getCaption() {
        return caption;
    }
}
