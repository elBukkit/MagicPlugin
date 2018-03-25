package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyGameModeAction extends BaseSpellAction
{
    private GameMode gameMode;
    private GameMode originalMode;
    private WeakReference<Player> targetPlayer;

    private class GameModeUndoAction implements Runnable
    {
        public GameModeUndoAction() {
        }

        @Override
        public void run() {
            resetMode();
        }
    }

    private void resetMode() {
        if (targetPlayer == null) return;
        Player player = targetPlayer.get();
        if (player == null) return;
        player.setGameMode(originalMode);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(entity instanceof Player))
        {
            return SpellResult.PLAYER_REQUIRED;
        }

        Player player = (Player)entity;
        originalMode = player.getGameMode();
        if (originalMode == gameMode) {
            return SpellResult.NO_TARGET;
        }

        player.setGameMode(gameMode);
        targetPlayer = new WeakReference<>(player);
        context.registerForUndo(new GameModeUndoAction());
        return SpellResult.CAST;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        String gameModeString = parameters.getString("game_mode");
        try {
            gameMode = GameMode.valueOf(gameModeString.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().warning("Invalid game mode: " + gameModeString);
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("game_mode");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("game_mode")) {
            for (GameMode mode : GameMode.values()) {
                examples.add(mode.name().toLowerCase());
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }
}
