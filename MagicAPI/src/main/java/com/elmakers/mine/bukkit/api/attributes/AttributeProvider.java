package com.elmakers.mine.bukkit.api.attributes;

import java.util.Set;

import org.bukkit.entity.Player;

public interface AttributeProvider {
    Set<String> getAllAttributes();
    Double getAttributeValue(String attribute, Player player);
}
