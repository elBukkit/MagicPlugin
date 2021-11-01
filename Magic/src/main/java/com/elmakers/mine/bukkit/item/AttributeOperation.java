package com.elmakers.mine.bukkit.item;

public enum AttributeOperation {
    // Copied from Bukkit to make compatibility easier
    ADD_NUMBER,
    // There is a "multiply" alias for this one
    ADD_SCALAR,
    MULTIPLY_SCALAR_1,

    MAXIMUM,
    DEFAULT;

    public static AttributeOperation parse(String key) {
        if (key.equalsIgnoreCase("multiply")) {
            return ADD_SCALAR;
        }
        return valueOf(key.toUpperCase());
    }
}
