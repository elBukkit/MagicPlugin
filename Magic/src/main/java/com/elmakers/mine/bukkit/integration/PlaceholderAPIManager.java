package com.elmakers.mine.bukkit.integration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.Replacer;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIManager extends PlaceholderExpansion {
    private final MageController controller;

    public PlaceholderAPIManager(MageController controller) {
        this.controller = controller;

        if (!register()) {
            controller.getLogger().warning("PlaceholderAPI integration failed");
        } else {
            controller.getLogger().info("PlaceholderAPI integration enabled. Available placeholders:");
            controller.getLogger().info("magic_path, magic_class, magic_wand, magic_spell, magic_mana, magic_mana_max, magic_sp, magic_spell_count");
            controller.getLogger().info("All magic attributes also available with a magic_ prefix");
            controller.getLogger().info("Add to messages/placeholders to add custom placeholders");
        }
    }

    @Nullable
    public String getPlaceholder(Player player, String namespace, String variable) {
        PlaceholderHook hook = PlaceholderAPI.getPlaceholders().get(namespace);
        return hook == null ? null : hook.onPlaceholderRequest(player, variable);
    }

    @Nonnull
    public String setPlaceholders(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        Mage mage = controller.getMage(player);
        String replacement = controller.getMessages().getIfSet("placeholders." + placeholder);
        if (replacement == null && mage instanceof Replacer) {
            replacement = ((Replacer)mage).getReplacement(placeholder, true);
        } else if (replacement != null) {
            replacement = mage.parameterize(replacement);
        }
        return replacement == null ? "" : replacement;
    }

    @Override
    public String getIdentifier() {
        return "magic";
    }

    @Override
    public String getPlugin() {
        return "Magic";
    }

    @Override
    public String getAuthor() {
        return "NathanWolf";
    }

    @Override
    public String getVersion() {
        return controller.getPlugin().getDescription().getVersion();
    }
}
