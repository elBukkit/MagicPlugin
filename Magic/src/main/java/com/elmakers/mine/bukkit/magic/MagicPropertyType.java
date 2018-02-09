package com.elmakers.mine.bukkit.magic;

/**
 * Specifies different types of property storage.
 *
 * Properties in Magic can be configured to save in one of these locations.
 *
 * Generally this is an inheritance chain, going like
 *
 * Mage->Class->(SubClass...)->SubClass->Wand
 *
 * But for most purposes properties are all merged together along this chain,
 * and generally speaking a property may not appear in more than one place.
 *
 * The exception to this rule is subclasses, since there maybe many of those, the
 * ones further down the chain will take precedent.
 */
public enum MagicPropertyType {
    WAND, SUBCLASS, CLASS, MAGE
}
