package com.elmakers.mine.bukkit.api.attributes;

import org.bukkit.entity.Player;

import java.util.Set;

public interface AttributeProvider {
    Set<String> getAllAttributes();
    Double getAttributeValue(String attribute, Player player);
    boolean hasClass(Player player, String name);
    public boolean hasSkill(Player player, String name);
}
