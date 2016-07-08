package com.elmakers.mine.bukkit.api.item;

import org.bukkit.entity.Player;

public interface Economy {
    double getBalance(Player player, String currency);
    boolean has(Player player, String currency, double amount);
    boolean modify(Player player, String currency, double amount);
}
