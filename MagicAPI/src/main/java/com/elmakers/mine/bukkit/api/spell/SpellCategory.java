package com.elmakers.mine.bukkit.api.spell;

import org.bukkit.Color;

import java.util.Collection;

public interface SpellCategory extends Comparable<SpellCategory> {
    String getKey();
    String getName();
    String getDescription();
    Color getColor();
    void addCast();
    void addCasts(long castCount, long lastCastTime);
    long getCastCount();
    long getLastCast();
    Collection<SpellTemplate> getSpells();
}