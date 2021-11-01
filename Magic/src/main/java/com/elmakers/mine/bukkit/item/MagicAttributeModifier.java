package com.elmakers.mine.bukkit.item;

import org.bukkit.configuration.ConfigurationSection;

public class MagicAttributeModifier {
    private final String attribute;
    private final AttributeOperation operation;
    private final double value;
    private final InventorySlot slot;

    public MagicAttributeModifier(String attributeKey, double value) {
        this.attribute = attributeKey;
        this.value = value;
        this.slot = InventorySlot.FREE;
        this.operation = AttributeOperation.DEFAULT;
    }

    public MagicAttributeModifier(String attributeKey, ConfigurationSection config, double defaultValue) {
        attribute = config.getString("attribute", attributeKey);
        value = config.getDouble("value", defaultValue);
        String operationString = config.getString("operation");
        if (operationString != null && !operationString.isEmpty()) {
            operation = AttributeOperation.parse(operationString);
        } else {
            operation = AttributeOperation.DEFAULT;
        }
        String slotString = config.getString("slot");
        if (slotString != null && !slotString.isEmpty()) {
            slot = InventorySlot.parse(slotString);
        } else {
            slot = InventorySlot.FREE;
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public AttributeOperation getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    public InventorySlot getSlot() {
        return slot;
    }
}
