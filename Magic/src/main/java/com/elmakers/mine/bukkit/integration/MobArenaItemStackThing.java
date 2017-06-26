package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.garbagemule.MobArena.things.Thing;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MobArenaItemStackThing implements Thing {
    private final ItemStack stack;
    private final String itemKey;

    public MobArenaItemStackThing(String itemKey, ItemStack stack) {
        this.itemKey = itemKey;
        this.stack = stack;
    }

    public boolean giveTo(Player player) {
        org.bukkit.Bukkit.getConsoleSender().sendMessage(org.bukkit.ChatColor.RED + "  GIVING :" + stack);
        return player.getInventory().addItem(NMSUtils.getCopy(stack)).isEmpty();
    }

    public boolean takeFrom(Player player) {
        return player.getInventory().removeItem(stack).isEmpty();
    }

    public boolean heldBy(Player player) {
        return player.getInventory().containsAtLeast(this.stack, this.stack.getAmount());
    }

    public String toString() {
        return itemKey;
    }
}
