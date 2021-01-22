package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class TimeAction extends BaseSpellAction
{
    private String timeType = "day";
    private String timeSet = "day";
    private boolean cycleMoonPhase;

    private static class UndoTimeChange implements Runnable
    {
        private final World world;
        private final long time;

        public UndoTimeChange(World world)
        {
            this.world = world;
            this.time = world.getFullTime();
        }

        @Override
        public void run()
        {
            world.setFullTime(time);
        }
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        cycleMoonPhase = parameters.getBoolean("cycle_moon_phase", false);
        timeType = parameters.getString("time", "day");
    }

    @Override
    public SpellResult perform(CastContext context) {
        World world = context.getWorld();
        if (world == null) {
            return SpellResult.WORLD_REQUIRED;
        }

        long targetTime = 0;
        timeSet = timeType;
        if (timeType.equalsIgnoreCase("toggle")) {
            long currentTime = world.getTime();
            if (currentTime >= 13000) {
                timeSet = "day";
            } else {
                timeSet = "night";
                targetTime = 13000;
            }
        }
        else if (timeType.equalsIgnoreCase("night"))
        {
            targetTime = 13000;
        }
        else
        {
            try
            {
                targetTime = Long.parseLong(timeType);
                timeSet = "raw(" + targetTime + ")";
            }
            catch (NumberFormatException ex)
            {
                targetTime = 0;
            }
        }

        context.registerForUndo(new UndoTimeChange(world));
        if (cycleMoonPhase)
        {
            long currentTime = world.getFullTime();
            currentTime = ((currentTime % 24000) + 1) * 24000 + targetTime;
            world.setFullTime(currentTime);
            return SpellResult.CAST;
        }
        world.setTime(targetTime);
        return SpellResult.CAST;
    }

    @Override
    public String transformMessage(String message) {
        return message.replace("$newtime", timeSet);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("time");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("time")) {
            examples.add("day");
            examples.add("night");
            examples.add("toggle");
            examples.add("0");
            examples.add("130000");
        } else if (parameterKey.equals("cycle_moon_phase")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
