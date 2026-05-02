package com.elmakers.mine.bukkit.magic;

import org.bukkit.configuration.ConfigurationSection;

public class SpellBlock {
    private final String rightClickSpell;
    private final String rightClickSneakSpell;
    private final String leftClickSpell;
    private final String leftClickSneakSpell;
    private final boolean requiresWand;
    private final boolean requiresSpellProgression;
    private final boolean cancelClick;

    public SpellBlock(ConfigurationSection configuration) {
        rightClickSpell = configuration.getString("right_click");
        rightClickSneakSpell = configuration.getString("right_click_sneak");
        leftClickSpell = configuration.getString("left_click");
        leftClickSneakSpell = configuration.getString("left_click_sneak");
        requiresWand = configuration.getBoolean("requires_wand", false);
        requiresSpellProgression = configuration.getBoolean("requires_spell_progression", false);
        cancelClick = configuration.getBoolean("cancel_click", true);
    }

    public SpellBlock(String rightClick, String sneakClick, boolean requiresWand) {
        rightClickSpell = rightClick;
        rightClickSneakSpell = sneakClick;
        leftClickSpell = rightClick;
        leftClickSneakSpell = sneakClick;
        this.requiresWand = requiresWand;
        requiresSpellProgression = true;
        cancelClick = true;
    }

    public String getRightClickSpell() {
        return rightClickSpell;
    }

    public String getRightClickSneakSpell() {
        return rightClickSneakSpell;
    }


    public String getLeftClickSpell() {
        return leftClickSpell;
    }

    public String getLeftClickSneakSpell() {
        return leftClickSneakSpell;
    }

    public boolean requiresWand() {
        return requiresWand;
    }

    public boolean requiresSpellProgression() {
        return requiresSpellProgression;
    }

    public boolean isCancelClick() {
        return cancelClick;
    }
}
