package com.elmakers.mine.bukkit.api.magic;

/**
 * Specifies different types of property storage.
 *
 * <p>Properties in Magic can be configured to save in one of these locations.
 *
 * <p>Generally this is an inheritance chain, going like
 *
 * <p>Mage->Class->(SubClass...)->SubClass->Wand
 *
 * <p>But for most purposes properties are all merged together along this chain,
 * and generally speaking a property may not appear in more than one place.
 *
 * <p>The exception to this rule is subclasses, since there maybe many of those, the
 * ones further down the chain will take precedent.
 */
public enum MagicPropertyType {
    WAND, SUBCLASS, CLASS, MODIFIER, MAGE
}
