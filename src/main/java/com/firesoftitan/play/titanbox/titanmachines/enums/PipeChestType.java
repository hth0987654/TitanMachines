package com.firesoftitan.play.titanbox.titanmachines.enums;

public enum PipeChestType {
    NOT_CONNECTED(0, "Not connected"),
    CHEST_IN(1, "Send into chest"),
    CHEST_OUT(2, "Pull out off chest"),
    OVERFLOW(3, "Overflow Chest");

    private int value;
    private String caption;
    PipeChestType(int value, String caption) {
        this.value = value;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public int getValue() {
        return value;
    }
    public static PipeChestType getPipeChestType(int value)
    {
        switch (value)
        {
            case 0: return NOT_CONNECTED;
            case 1: return CHEST_IN;
            case 2: return CHEST_OUT;
            case 3: return OVERFLOW;
        }
        return NOT_CONNECTED;
    }
}
