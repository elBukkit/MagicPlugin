package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class WandCastTask implements Runnable {
    private final Wand wand;
    private final Spell spell;

    public WandCastTask(Wand wand, Spell spell) {
        this.spell = spell;
        this.wand = wand;
    }

    @Override
    public void run() {
        wand.cast(spell);
    }
}
