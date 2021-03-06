package com.elmakers.mine.bukkit.economy;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.VaultController;

public class VaultCurrency extends BaseMagicCurrency {
    private final MageController controller;

    public VaultCurrency(MageController controller) {
        super(controller, "currency", 1);
        this.controller = controller;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        double balance = 0;
        VaultController vault = VaultController.getInstance();
        if (vault != null) {
            balance = vault.getBalance(mage.getPlayer());
        }
        return balance;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        VaultController vault = VaultController.getInstance();
        return vault != null && vault.has(mage.getPlayer(), amount);
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        VaultController vault = VaultController.getInstance();
        if (vault != null) {
            vault.withdrawPlayer(mage.getPlayer(), amount);
        }
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        VaultController vault = VaultController.getInstance();
        if (vault != null) {
            vault.depositPlayer(mage.getPlayer(), amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return controller.isVaultCurrencyEnabled();
    }

    @Override
    protected boolean hasDecimals() {
        return true;
    }
}
