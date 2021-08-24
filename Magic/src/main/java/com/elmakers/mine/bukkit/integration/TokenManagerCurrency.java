package com.elmakers.mine.bukkit.integration;

import java.util.OptionalLong;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

import me.realized.tokenmanager.api.TokenManager;

public class TokenManagerCurrency extends BaseMagicCurrency {
    private final TokenManager tokenManager;

    public TokenManagerCurrency(MageController controller, TokenManager tokenManager, ConfigurationSection configuration) {
        super(controller, "tokens", configuration, "Tokens");
        this.tokenManager = tokenManager;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        if (!mage.isPlayer()) {
            return 0;
        }
        OptionalLong tokens = tokenManager.getTokens(mage.getPlayer());
        return tokens.isPresent() ? tokens.getAsLong() : 0;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        if (!mage.isPlayer()) {
            return false;
        }
        return getBalance(mage) >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        tokenManager.removeTokens(mage.getPlayer(), (long)amount);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        if (!mage.isPlayer()) {
            return false;
        }
        return tokenManager.addTokens(mage.getPlayer(), (long)amount);
    }
}
