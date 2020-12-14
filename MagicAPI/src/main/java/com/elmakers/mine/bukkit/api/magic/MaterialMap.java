package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nullable;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

/**
 * A map of materials, mapping one material to another.
 *
 * <p>This also behaves as a MaterialSet via its keys.
 */
public interface MaterialMap extends MaterialSet {
    @Nullable
    MaterialAndData get(Material key);
}
