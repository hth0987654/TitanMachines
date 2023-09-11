package com.firesoftitan.play.titanbox.titanmachines.enums;

public enum PipeChestTypeEnum {
    NOT_CONNECTED(0, "Not connected"),
    CHEST_IN(1, "Send into chest"),
    CHEST_OUT(2, "Pull out off chest");
    private final int value;
    private final String caption;
    PipeChestTypeEnum(int value, String caption) {
        this.value = value;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public int getValue() {
        return value;
    }
    public static PipeChestTypeEnum getPipeChestType(int value)
    {
        switch (value)
        {
            case 0: return NOT_CONNECTED;
            case 1: return CHEST_IN;
            case 2: return CHEST_OUT;
        }
        return NOT_CONNECTED;
    }
}
