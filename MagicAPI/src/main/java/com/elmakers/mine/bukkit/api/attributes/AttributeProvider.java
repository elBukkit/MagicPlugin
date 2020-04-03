package com.elmakers.mine.bukkit.api.attributes;

import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

public interface AttributeProvider {
    Set<String> getAllAttributes();

    @Nullable
    Double getAttributeValue(String attribute, Player player);
}
