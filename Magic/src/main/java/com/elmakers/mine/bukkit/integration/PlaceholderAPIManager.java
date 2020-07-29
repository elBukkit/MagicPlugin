package com.elmakers.mine.bukkit.integration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;

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
        MageClass activeClass = mage.getActiveClass();
        Wand wand = mage.getActiveWand();
        Spell spell = wand == null ? null : wand.getActiveSpell();
        if (spell == null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            String spellKey = controller.getSpell(item);
            if (spellKey != null) {
                spell = mage.getSpell(spellKey);
            }
        }
        if (spell == null) {
            ItemStack item = player.getInventory().getItemInOffHand();
            String spellKey = controller.getSpell(item);
            if (spellKey != null) {
                spell = mage.getSpell(spellKey);
            }
        }
        CasterProperties casterProperties = mage.getActiveProperties();
        switch (placeholder) {
            case "path":
                ProgressionPath path = casterProperties.getPath();
                return path == null ? "" : path.getName();
            case "class":
                return activeClass == null ? "" : activeClass.getName();
            case "wand":
                return wand == null ? "" : wand.getName();
            case "spell":
                return spell == null ? "" : spell.getName();
            case "mana":
                return Integer.toString((int)casterProperties.getMana());
            case "mana_max":
                return Integer.toString(casterProperties.getManaMax());
            case "sp":
                return Integer.toString(mage.getSkillPoints());
            case "spell_count":
                return Integer.toString(casterProperties.getSpells().size());
        }
        return "";
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
