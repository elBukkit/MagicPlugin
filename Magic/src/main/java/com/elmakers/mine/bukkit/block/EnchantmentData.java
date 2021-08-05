package com.elmakers.mine.bukkit.block;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentData extends MaterialExtraData {
    protected final Map<Enchantment, Integer> enchantments = new HashMap<>();

    public EnchantmentData(Map<Enchantment, Integer> enchantments) {
        this.enchantments.putAll(enchantments);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    @Override
    public MaterialExtraData clone() {
        return new EnchantmentData(enchantments);
    }
}
