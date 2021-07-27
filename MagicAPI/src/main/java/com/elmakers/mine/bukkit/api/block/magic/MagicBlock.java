package com.elmakers.mine.bukkit.api.block.magic;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.Locatable;
import com.elmakers.mine.bukkit.api.magic.Mage;

public interface MagicBlock extends Locatable {
    @Nonnull
    String getTemplateKey();
    @Nonnull
    Mage getMage();
    void pause();
    void resume();
}
