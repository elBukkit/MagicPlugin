package com.elmakers.mine.bukkit.action.builtin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.google.common.collect.Iterators;

public class ModifyWalkSpeedAction extends BaseSpellAction implements Listener {
    private static final String INITIAL_MOVEMENT_SPEED_META = "InitialMovementSpeed";

    private static final class SpeedDataStack {
        /**
         * Original walk speed.
         */
        private float initialSpeed = 0.2f;

        /**
         * Stack of speed modifications. Note that this is linked map so it can
         * be used as a stack.
         */
        private Map<ModifyWalkSpeedAction, Float> map = new LinkedHashMap<>();
    }

    /**
     * The speed to be applied with this action.
     */
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
            return SpellResult.PLAYER_REQUIRED;
        }

        List<MetadataValue> meta = player
                .getMetadata(INITIAL_MOVEMENT_SPEED_META);

        final SpeedDataStack stack;

        if (meta.isEmpty()) {
            stack = new SpeedDataStack();
            stack.initialSpeed = player.getWalkSpeed();
            player.setMetadata(INITIAL_MOVEMENT_SPEED_META,
                    new FixedMetadataValue(context.getPlugin(), stack));
        } else {
            stack = (SpeedDataStack) meta.get(0).value();
        }

        stack.map.put(this, speed);
        player.setWalkSpeed(speed);

        return SpellResult.CAST;
    }

    @Override
    public void finish(CastContext context) {
        Player player = context.getMage().getPlayer();
        List<MetadataValue> meta = player
                .getMetadata(INITIAL_MOVEMENT_SPEED_META);

        if (meta.isEmpty()) {
            return;
        }

        SpeedDataStack stack = (SpeedDataStack) meta.get(0).value();

        stack.map.remove(this);

        final float oldSpeed;
        if (stack.map.isEmpty()) {
            oldSpeed = stack.initialSpeed;
            player.removeMetadata(INITIAL_MOVEMENT_SPEED_META,
                    context.getPlugin());
        } else {
            oldSpeed = Iterators.getLast(stack.map.values().iterator());
        }

        player.setWalkSpeed(oldSpeed);
    }
}
