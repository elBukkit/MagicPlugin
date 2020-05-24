package com.elmakers.mine.bukkit.magic;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

class EntityAttributeModifier {
    public EntityAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.base = null;
    }

    public EntityAttributeModifier(Attribute attribute, double base) {
        this.attribute = attribute;
        this.modifier = null;
        this.base = base;
    }

    public final AttributeModifier modifier;
    public final Attribute attribute;
    public final Double base;
    public Double previous;
}
