package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class FakeBlockAction extends BaseSpellAction {
    private double radius = 0;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        radius = parameters.getDouble("player_radius", 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        if (brush == null) {
            return SpellResult.FAIL;
        }

        Block block = context.getTargetBlock();
        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }

        Mage mage = context.getMage();
        brush.update(mage, context.getTargetSourceLocation());

        if (!brush.isDifferent(block)) {
            return SpellResult.NO_TARGET;
        }

        if (!brush.isReady()) {
            brush.prepare();
            return SpellResult.PENDING;
        }

        if (!brush.isValid()) {
            return SpellResult.FAIL;
        }

        List<WeakReference<Player>> targetPlayers = new ArrayList<>();
        if (mage.isPlayer()) {
            targetPlayers.add(new WeakReference<>(mage.getPlayer()));
        }
        if (radius > 0) {
            double radiusSquared = radius * radius;
            for (Player player : block.getWorld().getPlayers()) {
                if (player != mage.getPlayer() && player.getLocation().distanceSquared(block.getLocation()) <= radiusSquared) {
                    targetPlayers.add(new WeakReference<>(player));
                }
            }
        }
        context.registerFakeBlock(block, targetPlayers);

        for (WeakReference<Player> playerRef : targetPlayers) {
            Player player = playerRef.get();
            if (player == null) continue;
            byte data = 0;
            Short brushData = brush.getData();
            if (brushData != null) {
                data = (byte)(short)brushData;
            } else {
                data = DeprecatedUtils.getData(block);
            }
            DeprecatedUtils.sendBlockChange(player, block.getLocation(), brush.getMaterial(), data);
        }

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("player_radius");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("player_radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean usesBrush() {
        return true;
    }
}
