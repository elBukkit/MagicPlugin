package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class OcelotSpell extends TargetingSpell
{
    private static int DEFAULT_MAX_OCELOTS = 30;

    protected List<Ocelot> ocelots = new ArrayList<>();

    @Nullable
    public Ocelot newOcelot(Target target) {
        Block targetBlock = target.getBlock();
        if (targetBlock == null)
        {
            return null;
        }
        targetBlock = targetBlock.getRelative(BlockFace.UP);
        if (target.hasEntity())
        {
            targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
        }

        Ocelot entity = (Ocelot)getWorld().spawnEntity(targetBlock.getLocation(), EntityType.OCELOT);
        if (entity == null)
        {
            return null;
        }
        return entity;
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        ArrayList<Ocelot> newocelots = new ArrayList<>();

        for (Ocelot ocelot : ocelots)
        {
            if (!ocelot.isDead())
            {
                newocelots.add(ocelot);
            }
        }

        ocelots = newocelots;

        int maxOcelots = parameters.getInt("max_ocelots", DEFAULT_MAX_OCELOTS);
        int scaledMaxOcelots = (int)(mage.getRadiusMultiplier() * maxOcelots);
        if (ocelots.size() >= scaledMaxOcelots)
        {
            Ocelot killOcelot = ocelots.remove(0);
            killOcelot.setHealth(0);
        }

        Ocelot ocelot = newOcelot(target);
        if (ocelot == null)
        {
            return SpellResult.FAIL;
        }

        ocelots.add(ocelot);

        Entity e = target.getEntity();
        if (e != null && e instanceof LivingEntity)
        {
            LivingEntity targetEntity = (LivingEntity)e;
            for (Ocelot w : ocelots)
            {
                w.setTarget(targetEntity);
            }
        }

        return SpellResult.CAST;
    }
}
