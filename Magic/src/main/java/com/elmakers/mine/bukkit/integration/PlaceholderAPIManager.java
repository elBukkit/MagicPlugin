package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlaceholderAPIManager extends PlaceholderHook {
    private final MageController controller;

    public PlaceholderAPIManager(MageController controller) {
        this.controller = controller;

        if (!PlaceholderAPI.registerPlaceholderHook("magic", this)) {
            controller.getLogger().warning("PlaceholderAPI integration failed");
        } else {
            controller.getLogger().info("PlaceholderAPI integration enabled. Available placeholders:");
            controller.getLogger().info("magic_path, magic_class, magic_wand, magic_spell");
        }
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
        }
        return "";
    }
}
