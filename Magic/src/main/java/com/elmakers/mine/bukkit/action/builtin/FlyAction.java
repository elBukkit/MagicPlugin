package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class FlyAction extends BaseSpellAction {
    private static final float defaultFlySpeed = 0.1f;

    private boolean infinite;
    private int duration;
    private Long targetTime;
    private int maxHeightAboveGround;
    private int maxHeight;
    private float flySpeed = 0;
    private boolean wasFlying = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        maxHeight = parameters.getInt("max_height", 0);
        maxHeightAboveGround = parameters.getInt("max_height_above_ground", -1);
        duration = parameters.getInt("duration", 1);
        infinite = parameters.getString("duration", "infinite").equals("infinite");
        flySpeed = (float)parameters.getDouble("speed", 0);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        targetTime = null;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        Entity entity = context.getTargetEntity();
        if (entity != null && entity instanceof Player) {
            Player player = (Player)entity;
            toggleFlight(player, false);
        }
    }

    private void toggleFlight(Player player, boolean flying) {
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setAllowFlight(flying);
        }

        // Stop falling!
        if (flying) {
            Vector velocity = player.getVelocity();
            if (velocity.getY() < 0) {
                velocity.setY(0);
                player.setVelocity(velocity);
            }
        }
        player.setFlying(flying);
        wasFlying = flying;
        if (flySpeed > 0) {
            if (flying) {
                player.setFlySpeed(flySpeed * defaultFlySpeed);
            } else {
                player.setFlySpeed(defaultFlySpeed);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }

        if (!(entity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Player player = (Player)entity;

        // Check for landing
        if (wasFlying && !player.isFlying()) {
            return SpellResult.CAST;
        }

        if (targetTime == null) {
            targetTime = System.currentTimeMillis() + duration;
        }
        if (infinite || System.currentTimeMillis() < targetTime) {

            Location currentLocation = player.getLocation();
            boolean aboveHeight = false;
            if (maxHeight > 0 && currentLocation.getY() >= maxHeight) {
                aboveHeight = true;
            } else if (maxHeightAboveGround >= 0) {
                double maxHeightThreshold = maxHeightAboveGround;
                Block block = currentLocation.getBlock();
                double heightAboveGround = currentLocation.getY() - (int)currentLocation.getY();
                while (heightAboveGround < maxHeightThreshold && context.isPassthrough(block)) {
                    block = block.getRelative(BlockFace.DOWN);
                    heightAboveGround++;
                }
                if (heightAboveGround >= maxHeightThreshold) {
                    aboveHeight = true;
                }
            }

            toggleFlight(player, !aboveHeight);
            return SpellResult.PENDING;
        }

        toggleFlight(player, false);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
        parameters.add("speed");
        parameters.add("max_height");
        parameters.add("max_height_above_ground");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("max_height") || parameterKey.equals("max_height_above_ground") || parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("duration")) {
            examples.add("infinite");
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }
}
