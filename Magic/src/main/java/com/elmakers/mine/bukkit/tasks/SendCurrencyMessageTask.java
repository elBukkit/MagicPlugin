package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.Mage;

public class SendCurrencyMessageTask implements Runnable {
    private final Mage mage;
    private final String currency;
    private final double amount;

    public SendCurrencyMessageTask(Mage mage, String currency, double amount) {
        this.mage = mage;
        this.currency = currency;
        this.amount = amount;
    }

    @Override
    public void run() {
        mage.sendCurrencyMessage(currency, amount);
    }
}
