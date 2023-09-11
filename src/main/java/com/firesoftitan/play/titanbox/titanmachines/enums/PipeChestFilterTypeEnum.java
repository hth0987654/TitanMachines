package com.firesoftitan.play.titanbox.titanmachines.enums;

public enum PipeChestFilterTypeEnum {
    DISABLED(-1, "Disabled"),
    ALL(0, "No filter"),
    TOTAL_MATCH(1, "100% match"),
    MATERIAL_ONLY(2, "Material match only");

    private final int value;
    private final String caption;
    PipeChestFilterTypeEnum(int value, String caption) {
        this.value = value;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public int getValue() {
        return value;
    }
    public static PipeChestFilterTypeEnum getPipeChestType(int value)
    {
        switch (value)
        {
            case -1: return DISABLED;
            case 0: return ALL;
            case 1: return TOTAL_MATCH;
            case 2: return MATERIAL_ONLY;
        }
        return DISABLED;
    }
}
