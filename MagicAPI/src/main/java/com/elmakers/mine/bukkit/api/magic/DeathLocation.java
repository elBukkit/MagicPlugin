package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DeathLocation {
    private final Location location;
    private final ItemStack[] items;
    private final int experiencePoints;

    public DeathLocation(Location location, ItemStack[] items, int experiencePoints) {
        this.location = location;
        this.items = items;
        this.experiencePoints = experiencePoints;
    }

    @Nonnull
    public Location getLocation() {
        return location;
    }

    @Nullable
    public ItemStack[] getItems() {
        return items;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }
}
