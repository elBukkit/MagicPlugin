package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import me.realized.tokenmanager.api.TokenManager;

public class TokenManagerController {
    private final TokenManager tokenManager;
    private final MageController controller;
    private boolean enabled;

    public TokenManagerController(MageController controller, Plugin tokenManagerPlugin) {
        this.controller = controller;
        tokenManager = tokenManagerPlugin instanceof TokenManager ? (TokenManager)tokenManagerPlugin : null;
    }

    public void load(ConfigurationSection configuration) {
        enabled = configuration.getBoolean("enabled", true);
        String statusString;

        if (!enabled) {
            statusString = " but is disabled in Magic's configs";
        } else if (tokenManager == null) {
            statusString = " but integration failed";
        } else {
            statusString = ", registering tokens currency";
        }
        controller.getLogger().info("TokenManager found" + statusString);
    }

    public void register(ConfigurationSection currencyConfiguration) {
        if (enabled && tokenManager != null) {
            ConfigurationSection configuration = currencyConfiguration.getConfigurationSection("tokens");
            if (configuration == null) {
                configuration = ConfigurationUtils.newConfigurationSection();
            }
            controller.register(new TokenManagerCurrency(controller, tokenManager, configuration));
        }
    }
}
