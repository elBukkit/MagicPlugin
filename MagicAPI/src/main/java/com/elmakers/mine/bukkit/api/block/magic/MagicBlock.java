package com.elmakers.mine.bukkit.api.block.magic;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.Locatable;
import com.elmakers.mine.bukkit.api.magic.Mage;

public interface MagicBlock extends Locatable {
    @Nonnull
    String getTemplateKey();
    @Nonnull
    Mage getMage();

    /**
     * Disable this automaton, it will not run again until re-enabled
     */
    void disable();
    void enable();

    /**
     * Temporarily pause this automaton, it will auto-resume on chink reload.
     */
    void pause();
    void resume();
}
