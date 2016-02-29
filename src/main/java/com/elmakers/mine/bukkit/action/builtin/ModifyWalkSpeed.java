package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyWalkSpeed extends BaseSpellAction implements Listener {

    private float intialSpeed = 0.2f;
    private float speed = 0.0f;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        if (parameters.contains("speed")) {
            speed = (float) parameters.getDouble("speed", 0.0);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Player player = context.getMage().getPlayer();

        if (player == null) {
            return SpellResult.FAIL;
        }

        intialSpeed = player.getWalkSpeed();
        player.setWalkSpeed(speed);
        return SpellResult.CAST;
    }

    @Override
    public void finish(CastContext context) {
        context.getMage().getPlayer().setWalkSpeed(intialSpeed);
    }
}
