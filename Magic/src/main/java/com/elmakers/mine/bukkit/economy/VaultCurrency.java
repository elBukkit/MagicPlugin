package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.integration.VaultController;

public class VaultCurrency extends BaseMagicCurrency {
    private final MageController controller;
    private final boolean customFormatting;

    public VaultCurrency(MageController controller, ConfigurationSection configuration) {
        super(controller, "currency", configuration);
        customFormatting = configuration.getBoolean("custom_formatting");
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

    @Override
    public String getName(Messages messages) {
        if (customFormatting) {
            return super.getName(messages);
        }
        return VaultController.getInstance().getCurrencyPlural();
    }

    @Override
    public String getShortName(Messages messages) {
        if (customFormatting) {
            return super.getShortName(messages);
        }
        return VaultController.getInstance().getCurrency();
    }

    @Override
    public String getSingularName(Messages messages) {
        if (customFormatting) {
            return super.getSingularName(messages);
        }
        return VaultController.getInstance().getCurrency();
    }

    @Override
    public String formatAmount(double amount, Messages messages) {
        if (customFormatting) {
            return super.formatAmount(amount, messages);
        }
        return VaultController.getInstance().format(amount);
    }
}
