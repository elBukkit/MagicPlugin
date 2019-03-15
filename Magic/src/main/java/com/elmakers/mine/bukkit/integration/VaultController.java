package com.elmakers.mine.bukkit.integration;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class VaultController {
    private static VaultController instance;
    private Economy economy;
    private NumberFormat formatter = new DecimalFormat("#0.00");

    public static VaultController getInstance() {
        return instance;
    }

    public static boolean initialize(Plugin callingPlugin, Plugin vaultPlugin) {
        try {
            RegisteredServiceProvider<Economy> ecoProvider = vaultPlugin.getServer().getServicesManager().getRegistration(Economy.class);
            Economy economy = ecoProvider == null ? null : ecoProvider.getProvider();
            if (economy == null) {
                callingPlugin.getLogger().info("Vault found, but no economy found");
            } else {
                callingPlugin.getLogger().info("Vault found, 'currency' cost types available");
            }
            instance = new VaultController(callingPlugin, economy);
        } catch (Exception ex) {
            ex.printStackTrace();
            instance = null;
            return false;
        }

        return true;
    }

    public static boolean hasEconomy() {
        if (instance == null) {
            return false;
        }

        return instance.economy != null;
    }

    private VaultController(Plugin owner, Economy economy) {
        this.economy = economy;
    }

    public double getBalance(Player player) {
        if (economy == null || player == null) {
            return 0;
        }

        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (economy == null || player == null) {
            return false;
        }

        return economy.has(player, amount);
    }

    public String format(double amount) {
        if (economy == null) {
            return formatter.format(amount);
        }

        return economy.format(amount);
    }

    public String getCurrency() {
        return economy == null ? "" : economy.currencyNameSingular();
    }

    public String getCurrencyPlural() {
        return economy == null ? "" : economy.currencyNamePlural();
    }

    public boolean withdrawPlayer(Player player, double amount) {
        if (economy == null || player == null) {
            return false;
        }
        EconomyResponse response  = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean depositPlayer(Player player, double amount) {
        if (economy == null || player == null) {
            return false;
        }
        EconomyResponse response  = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
}
