package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;

public class TNTAction extends BaseProjectileAction
{
    private int size;
    private int count;
    private int fuse;
    private boolean useFire;
    private boolean breakBlocks;
    private double velocity;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        track = true;
        super.prepare(context, parameters);
        size = parameters.getInt("size", 6);
        count = parameters.getInt("count", 1);
        fuse = parameters.getInt("fuse", 80);
        useFire = parameters.getBoolean("fire", false);
        breakBlocks = parameters.getBoolean("break_blocks", true);
        velocity = parameters.getDouble("tnt_velocity", 1.0);
    }

    @Override
    public SpellResult start(CastContext context) {
        Mage mage = context.getMage();
        LivingEntity living = mage.getLivingEntity();
        MageController controller = context.getController();
        int size = (int)(mage.getRadiusMultiplier() * this.size);

        Location loc = context.getEyeLocation();
        if (loc == null) {
            return SpellResult.LOCATION_REQUIRED;
        }
        if (!context.hasBreakPermission(loc.getBlock())) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        final Random rand = new Random();
        for (int i = 0; i < count; i++)
        {
            Location targetLoc = loc.clone();
            if (count > 1)
            {
                targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * count) - count);
                targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * count) - count);
            }
            TNTPrimed grenade = (TNTPrimed)context.getWorld().spawnEntity(targetLoc, EntityType.PRIMED_TNT);
            if (grenade == null) {
                return SpellResult.FAIL;
            }
            if (living != null) {
                CompatibilityUtils.setTNTSource(grenade, living);
            }
            if (velocity > 0) {
                Vector aim = context.getDirection();
                SafetyUtils.setVelocity(grenade, aim.multiply(velocity));
            }
            grenade.setYield(size);
            grenade.setFuseTicks(fuse);
            grenade.setIsIncendiary(useFire);
            if (!breakBlocks) {
                EntityMetadataUtils.instance().setBoolean(grenade, MagicMetaKeys.CANCEL_EXPLOSION_BLOCKS, true);
            }
            track(context, grenade);
        }

        return checkTracking(context);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("size");
        parameters.add("count");
        parameters.add("fuse");
        parameters.add("fire");
        parameters.add("break_blocks");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("fire") || parameterKey.equals("break_blocks")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("size") || parameterKey.equals("count") || parameterKey.equals("fuse")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresBuildPermission() {
        return useFire;
    }

    @Override
    public boolean requiresBreakPermission() {
        return breakBlocks;
    }
}
