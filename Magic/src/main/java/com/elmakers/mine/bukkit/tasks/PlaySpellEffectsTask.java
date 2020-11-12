package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class PlaySpellEffectsTask implements Runnable {
    private final Collection<EffectPlayer> effects;
    private final Spell spell;
    private final Mage mage;
    private final Location origin;

    public PlaySpellEffectsTask(Collection<EffectPlayer> effects, Location origin, Spell spell, Mage mage) {
        this.effects = effects;
        this.spell = spell;
        this.mage = mage;
        this.origin = origin;
    }

    @Override
    public void run() {
        for (com.elmakers.mine.bukkit.api.effect.EffectPlayer player : effects) {
            player.setMaterial(spell.getEffectMaterial());
            player.setColor(mage.getEffectColor());
            player.start(origin, null, spell.getLocation(), mage.getEntity());
        }
    }
}
