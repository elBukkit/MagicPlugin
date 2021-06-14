package com.elmakers.mine.bukkit.utility;

public class CurrencyAmount {
    private final String type;
    private int amount;

    public CurrencyAmount(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void scale(int scale) {
        this.amount *= scale;
    }
}
