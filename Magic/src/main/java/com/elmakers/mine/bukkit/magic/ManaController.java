package com.elmakers.mine.bukkit.magic;

import org.bukkit.entity.Player;

public interface ManaController {
    double getMaxMana(Player player);
    double getManaRegen(Player player);
    double getMana(Player player);
    void removeMana(Player player, double amount);
    void setMana(Player player, double amount);
}
