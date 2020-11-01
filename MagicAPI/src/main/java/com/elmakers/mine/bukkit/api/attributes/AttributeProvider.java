package com.elmakers.mine.bukkit.api.attributes;

import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicProvider;

public interface AttributeProvider extends MagicProvider {
    Set<String> getAllAttributes();

    @Nullable
    Double getAttributeValue(String attribute, Player player);
}
