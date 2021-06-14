package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class AreaOfEffectAction extends CompoundEntityAction
{
    protected int radius;
    protected int yRadius;
    protected int targetCount;
    protected boolean targetSource;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        radius = parameters.getInt("radius", 8);
        yRadius = parameters.getInt("y_radius", radius);
        targetCount = parameters.getInt("target_count", -1);
        targetSource = parameters.getBoolean("target_source", true);

        Mage mage = context.getMage();
        radius = (int)(mage.getRadiusMultiplier() * radius);

        super.prepare(context, parameters);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities)
    {
        context.addWork((int)Math.ceil(radius) + 10);
        Mage mage = context.getMage();
        Location sourceLocation = context.getTargetLocation();
        if (mage.getDebugLevel() > 8)
        {
            mage.sendDebugMessage(ChatColor.GREEN + "AOE Targeting from " + ChatColor.GRAY + sourceLocation.getBlockX()
                    + ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + sourceLocation.getBlockY()
                    + ChatColor.DARK_GRAY + "," + ChatColor.GRAY + sourceLocation.getBlockZ()
                    + ChatColor.DARK_GREEN + " with radius of " + ChatColor.GREEN + radius
                    + ChatColor.GRAY + " self? " + ChatColor.DARK_GRAY + context.getTargetsCaster(), 14
            );
        }
        List<Entity> candidates = CompatibilityUtils.getNearbyEntities(sourceLocation, radius, yRadius, radius);
        Entity targetEntity = context.getTargetEntity();
        if (targetCount > 0)
        {
            List<Target> targets = new ArrayList<>();
            for (Entity entity : candidates)
            {
                boolean canTarget = true;
                if (entity == targetEntity && !targetSource) canTarget = false;
                if (canTarget && context.canTarget(entity))
                {
                    Target target = new Target(sourceLocation, entity, radius, 0);
                    targets.add(target);
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType() + ChatColor.DARK_GREEN + ": " + ChatColor.YELLOW + target.getScore(), 12);
                }
                else if (mage.getDebugLevel() > 7)
                {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType(), 16);
                }
            }
            Collections.sort(targets);
            for (int i = 0; i < targetCount && i < targets.size(); i++)
            {
                Target target = targets.get(i);
                entities.add(new WeakReference<>(target.getEntity()));
            }
        }
        else
        {
            for (Entity entity : candidates)
            {
                boolean canTarget = true;
                if (entity == targetEntity && !targetSource) canTarget = false;
                if (canTarget && context.canTarget(entity))
                {
                    entities.add(new WeakReference<>(entity));
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType(), 12);
                }
                else if (mage.getDebugLevel() > 7)
                {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType(), 16);
                }
            }
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
        parameters.add("target_count");
        parameters.add("target_source");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_count") || parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
